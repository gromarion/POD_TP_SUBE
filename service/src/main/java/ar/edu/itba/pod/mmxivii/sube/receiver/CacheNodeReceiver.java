package ar.edu.itba.pod.mmxivii.sube.receiver;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.getFirst;

import java.rmi.RemoteException;
import java.rmi.server.UID;

import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

import ar.edu.itba.pod.mmxivii.jgroups.ClusterNode;
import ar.edu.itba.pod.mmxivii.sube.common.CardRegistry;
import ar.edu.itba.pod.mmxivii.sube.common.CardService;
import ar.edu.itba.pod.mmxivii.sube.entity.CardCachedData;
import ar.edu.itba.pod.mmxivii.sube.entity.LeaderNodeData;
import ar.edu.itba.pod.mmxivii.sube.message.CacheUpdateRequest;
import ar.edu.itba.pod.mmxivii.sube.message.UIDOperationLockRequest;
import ar.edu.itba.pod.mmxivii.sube.message.UIDOperationLockRequest.OperationRequestType;
import ar.edu.itba.pod.mmxivii.sube.message.UIDOperationLockRequestResponse;
import ar.edu.itba.pod.mmxivii.util.Threads;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class CacheNodeReceiver extends ReceiverAdapter implements CardService {

	private final ClusterNode _node;
	private CardRegistry _server;
	private boolean _leader;
	private Address _leaderAdress;
	private final LeaderNodeData _leaderData;
	private final CardCachedData _cachedData;

	private boolean _requestSent;
	private boolean _responseReceived;
	private UIDOperationLockRequestResponse _response;

	public CacheNodeReceiver(ClusterNode node, CardRegistry server) {
		_node = checkNotNull(node);
		_server = checkNotNull(server);
		_leaderData = new LeaderNodeData();
		_cachedData = new CardCachedData();
	}

	public final ClusterNode node() {
		return _node;
	}

	public final CardRegistry server() {
		return _server;
	}

	public final boolean isLeader() {
		return _leader;
	}

	@Override
	public void viewAccepted(View view) {
		_leaderAdress = getFirst(view, null);
		if (_leaderAdress.equals(node().address())) {
			_leader = true;
			System.out.println("Leader: " + node().name());
		}
	}

	@Override
	public void receive(Message msg) {
		if (isLeader()) {
			leaderReceive(msg);
		} else {
			slaveReceive(msg);
		}
	}

	private void leaderReceive(Message msg) {
		Object object = msg.getObject();
		if (object instanceof UIDOperationLockRequest) {
			UIDOperationLockRequest request = (UIDOperationLockRequest) object;
			if (request.type().equals(OperationRequestType.LOCK)) {
				boolean lockAccepted = _leaderData.lock(request.id());
				node().sendObject(msg.getSrc(), new UIDOperationLockRequestResponse(lockAccepted));
				// TODO: mensaje broadcast para avisar a todos los nodos que
				// tienen que poner en su _leaderData que request.id() esta
				// ahora BLOQUEADO
			} else {
				_leaderData.release(request.id());
				// TODO: mensaje broadcast para avisar a todos los nodos que
				// tienen que poner en su _leaderData que request.id() esta
				// ahora DESBLOQUEADO
			}
		}
	}

	private void slaveReceive(Message msg) {
		_response = null;
		Object object = msg.getObject();
		if (object instanceof UIDOperationLockRequestResponse) {
			_response = (UIDOperationLockRequestResponse) object;
		} else if (object instanceof CacheUpdateRequest) {
			CacheUpdateRequest cacheUpdateRequest = (CacheUpdateRequest) object;
			_cachedData.setCardBalance(cacheUpdateRequest.id(), cacheUpdateRequest.balance());
		}
		_responseReceived = true;
	}

	@Override
	public double getCardBalance(UID id) throws RemoteException {
		sendLockRequestAndBlock(id);
		Optional<Double> balance = _cachedData.getCardBalance(id);
		if (!balance.isPresent()) {
			double balanceFromServer = server().getCardBalance(id);
			// XXX: notificar a todos los nodos el nuevo balance del usuario
			node().sendObject(new CacheUpdateRequest(id, balanceFromServer));
			balance = Optional.of(balanceFromServer);
		}
		sendUnlockRequest(id);
		return balance.get();
	}

	@Override
	public double travel(UID id, String description, double amount) throws RemoteException {
		// TODO: terminar esta logica
		return server().addCardOperation(id, description, amount);
	}

	@Override
	public double recharge(UID id, String description, double amount) throws RemoteException {
		// TODO: terminar esta logica
		return server().addCardOperation(id, description, amount);
	}

	private void sendLockRequestAndBlock(UID id) {
		Preconditions.checkState(!_requestSent);
		boolean lockAccepted = false;
		_requestSent = true;
		do {
			_responseReceived = false;
			node().sendObject(_leaderAdress, new UIDOperationLockRequest(id, OperationRequestType.LOCK));
			while (!_responseReceived) {
				// Wait for the leader to allow the lock
				Threads.sleep(5L);
			}
			lockAccepted = _response.isLockAccepted();
		} while (!lockAccepted);
		_requestSent = false;
	}

	private void sendUnlockRequest(UID id) {
		node().sendObject(_leaderAdress, new UIDOperationLockRequest(id, OperationRequestType.UNLOCK));
	}
}

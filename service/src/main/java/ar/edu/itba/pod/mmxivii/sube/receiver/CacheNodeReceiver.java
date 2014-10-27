package ar.edu.itba.pod.mmxivii.sube.receiver;

import static ar.edu.itba.pod.mmxivii.sube.common.Utils.CARD_REGISTRY_BIND;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.and;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UID;

import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.joda.time.LocalDateTime;

import ar.edu.itba.pod.mmxivii.jgroups.ClusterNode;
import ar.edu.itba.pod.mmxivii.sube.common.CardRegistry;
import ar.edu.itba.pod.mmxivii.sube.common.CardService;
import ar.edu.itba.pod.mmxivii.sube.common.CardServiceRegistry;
import ar.edu.itba.pod.mmxivii.sube.common.Utils;
import ar.edu.itba.pod.mmxivii.sube.entity.CachedData;
import ar.edu.itba.pod.mmxivii.sube.entity.Operation;
import ar.edu.itba.pod.mmxivii.sube.entity.Operation.OperationType;
import ar.edu.itba.pod.mmxivii.sube.entity.UserData;
import ar.edu.itba.pod.mmxivii.sube.predicate.OnlyDigitsAndLetters;
import ar.edu.itba.pod.mmxivii.sube.predicate.PositiveDouble;
import ar.edu.itba.pod.mmxivii.sube.predicate.TwoDecimalPlacesAndLessThan100;
import ar.edu.itba.pod.mmxivii.sube.service.CardServiceImpl;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class CacheNodeReceiver extends ReceiverAdapter implements CardService {

	private static final String numberRegex = "[0-9]*(\\.[0-9]?[0-9]?)?";

	public static double trimAfter2Zeros(double d) {
		String s = d + "";
		if (s.matches(numberRegex)) {
			return Double.parseDouble(String.format("%.2f", d));
		}
		return d;
	}

	private final Predicate<Double> _amountValidator = and(new TwoDecimalPlacesAndLessThan100(), new PositiveDouble());
	private final Predicate<String> _descValidator = new OnlyDigitsAndLetters();
	private final ClusterNode _node;
	
	private CardRegistry _server;
	private boolean _serverIsDown;
	private CardServiceRegistry _balancerRegistry;
	private final CachedData _cachedData = new CachedData();
	private boolean _syncronized = false;

	public CacheNodeReceiver(ClusterNode node, CardServiceRegistry balancerRegistry) {
		_node = checkNotNull(node);
		_balancerRegistry = checkNotNull(balancerRegistry);
		_serverIsDown = true;
	}

	public final ClusterNode node() {
		return _node;
	}

	public final CardRegistry server() {
		if (_serverIsDown) {
			try {
				_server = Utils.lookupObject(CARD_REGISTRY_BIND);
				_serverIsDown = false;
			} catch (NotBoundException e) {
				_serverIsDown = true;
			}
		}
		return _server;
	}

	@Override
	public void viewAccepted(View view) {
		if (!_syncronized) {
			if (view.getMembers().size() > 1) {
				System.out.println(node().name() + " enviando request de sync [Broadcast]");
				node().sendObject(CacheSyncRequest.newSyncRequest());
			} else {
				_syncronized = true;
				registerWithBalancer();
			}
		}
	}

	private void registerWithBalancer() {
		checkArgument(_syncronized, "El nodo debe estan syncronizado");
		try {
			CardServiceImpl cardService = new CardServiceImpl(this);
			_balancerRegistry.registerService(cardService);
			System.out.println(node().name() + " dado de alta frente al balancer");
		} catch (RemoteException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void receive(Message msg) {
		if (msg.getSrc().equals(node().address())) {
			// XXX: ignore self requests
			return;
		}
		Object object = msg.getObject();
		if (object instanceof CacheUpdateRequest) {
			CacheUpdateRequest cacheUpdateRequest = (CacheUpdateRequest) object;
			UID uid = cacheUpdateRequest.uid();
			switch (cacheUpdateRequest.type()) {
			case BALANCE:
				setCardBalance(uid, cacheUpdateRequest.balance().get());
				break;
			case OPERATION:
				addOperation(uid, cacheUpdateRequest.operation().get());
				break;
			default:
				throw new IllegalStateException("Unknown type: " + cacheUpdateRequest.type());
			}
		} else if (object instanceof CacheSyncRequest) {
			CacheSyncRequest cacheFirstSync = (CacheSyncRequest) object;
			switch (cacheFirstSync.status()) {
			case REQUEST:
				if (_syncronized) {
					System.out.println(node().name() + " respondiendo pedido de syn a " + msg.getSrc());
					node().sendObject(msg.getSrc(), CacheSyncRequest.newSyncResponse(_cachedData));
				}
				break;
			case RESPONSE:
				if (!_syncronized) {
					_cachedData.setTo(cacheFirstSync.data());
					System.out.println(node().name() + " syncronizado");
					_syncronized = true;
					registerWithBalancer();
				}
				break;
			case UPDATE:
				System.out.println("Eliminando datos a pedido de " + msg.getSrc());
				_cachedData.clearAll(cacheFirstSync.data());
				break;
			default:
				throw new IllegalStateException("Unknown status: " + cacheFirstSync.status());
			}
		} else if (object instanceof Integer) {
			// FIXME: vi peliculas de terror que me asustaron menos que esto
			Integer receiverValue = (Integer) object;
			if (receiverValue == -2) {
				node().sendObject(msg.getSrc(), CacheNodeReceiver.class);
			}
		}
	}

    @Override
    public boolean ping() throws RemoteException {
        return true;
    }

    @Override
	public double getCardBalance(UID id) throws RemoteException {
		Optional<UserData> userdata = _cachedData.tryGet(id);
		if (userdata.isPresent()) {
			return userdata.get().balance();
		}
		userdata = getDataFromServer(id);
		if (!userdata.isPresent()) {
			return CardRegistry.COMMUNICATIONS_FAILURE;
		}
		return userdata.get().balance();
	}

	private Optional<UserData> getDataFromServer(UID uid) throws RemoteException {
		try {
			double balanceFromServer = server().getCardBalance(uid);
			node().sendObject(CacheUpdateRequest.newBalance(uid, balanceFromServer));
			return Optional.of(setCardBalance(uid, balanceFromServer));
		} catch (Exception e) {
			_serverIsDown = true;
			return Optional.absent();
		}
	}

	private UserData setCardBalance(UID uid, double balance) {
		UserData userdata = new UserData();
		_cachedData.put(uid, userdata.setBalance(balance));
		return userdata;
	}

	@Override
	public double travel(UID id, String description, double amount) throws RemoteException {
		if (!_amountValidator.apply(amount) || !_descValidator.apply(description)) {
			System.out.println(String.format("Valores invalidos: [%f, %s]", amount, description));
			return CardRegistry.CANNOT_PROCESS_REQUEST;
		}
		Optional<UserData> userdata = _cachedData.tryGet(id);
		if (!userdata.isPresent()) {
			userdata = getDataFromServer(id);
			if (!userdata.isPresent()) {
				return CardRegistry.COMMUNICATIONS_FAILURE;
			}
		}
		if (userdata.get().balance() < amount) {
			return CardRegistry.OPERATION_NOT_PERMITTED_BY_BALANCE;
		}
		Operation operation = new Operation(OperationType.TRAVEL, description, amount, LocalDateTime.now());
		addOperation(id, operation);
		node().sendObject(CacheUpdateRequest.newOperation(id, operation));
		return userdata.get().balance();
	}

	@Override
	public double recharge(UID id, String description, double amount) throws RemoteException {
		if (!_amountValidator.apply(amount) || !_descValidator.apply(description)) {
			System.out.println(String.format("Valores invalidos: [%f, %s]", amount, description));
			return CardRegistry.CANNOT_PROCESS_REQUEST;
		}
		Optional<UserData> userdata = _cachedData.tryGet(id);
		if (!userdata.isPresent()) {
			userdata = getDataFromServer(id);
			if (!userdata.isPresent()) {
				return CardRegistry.COMMUNICATIONS_FAILURE;
			}
		}
		if (userdata.get().balance() + amount > CardRegistry.MAX_BALANCE) {
			return CardRegistry.OPERATION_NOT_PERMITTED_BY_BALANCE;
		}
		Operation operation = new Operation(OperationType.RECHARGE, description, amount, LocalDateTime.now());
		addOperation(id, operation);
		node().sendObject(CacheUpdateRequest.newOperation(id, operation));
		return userdata.get().balance();
	}

	private void addOperation(UID uid, Operation operation) {
		_cachedData.get(uid).addOperation(operation);
		System.out.println(node().name() + " => " + uid.toString() + " > " + _cachedData.get(uid).balance());
	}

}

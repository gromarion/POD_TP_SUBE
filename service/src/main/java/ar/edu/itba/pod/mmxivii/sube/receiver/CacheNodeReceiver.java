package ar.edu.itba.pod.mmxivii.sube.receiver;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.find;

import java.rmi.RemoteException;
import java.rmi.server.UID;

import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

import ar.edu.itba.pod.mmxivii.jgroups.ClusterNode;
import ar.edu.itba.pod.mmxivii.sube.common.CardRegistry;
import ar.edu.itba.pod.mmxivii.sube.common.CardService;
import ar.edu.itba.pod.mmxivii.sube.common.CardServiceRegistry;
import ar.edu.itba.pod.mmxivii.sube.entity.CachedData;
import ar.edu.itba.pod.mmxivii.sube.entity.UserData;
import ar.edu.itba.pod.mmxivii.sube.predicate.OnlyDigitsAndLetters;
import ar.edu.itba.pod.mmxivii.sube.predicate.PositiveDouble;
import ar.edu.itba.pod.mmxivii.sube.predicate.TwoDecimalPlacesAndLessThan100;
import ar.edu.itba.pod.mmxivii.sube.service.CardServiceImpl;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class CacheNodeReceiver extends ReceiverAdapter implements CardService {

	private final Predicate<Double> _amountValidator = and(new TwoDecimalPlacesAndLessThan100(), new PositiveDouble());
	private final Predicate<String> _descValidator = new OnlyDigitsAndLetters();
	private final ClusterNode _node;
	private CardRegistry _server;
	private CardServiceRegistry _balancerRegistry;
	private final CachedData _cachedData = new CachedData();
	private boolean _syncronized = false;

	public CacheNodeReceiver(ClusterNode node, CardRegistry server, CardServiceRegistry balancerRegistry) {
		_node = checkNotNull(node);
		_server = checkNotNull(server);
		_balancerRegistry = checkNotNull(balancerRegistry);
	}

	public final ClusterNode node() {
		return _node;
	}

	public final CardRegistry server() {
		return _server;
	}

	@Override
	public void viewAccepted(View view) {
		if (!_syncronized) {
			if (view.getMembers().size() > 1) {
				Address syncAddres = find(view.getMembers(), not(equalTo(node().address())));
				System.out.println(node().name() + " enviando request de sync a " + syncAddres);
				node().sendObject(syncAddres, CacheSync.newSyncRequest());
			} else {
				_syncronized = true;
				registerWithBalancer();
			}
		}
	}

	private void registerWithBalancer() {
		checkArgument(_syncronized, "El nodo debe estan syncronizado");
		try {
			CardServiceImpl cardService = new CardServiceImpl(_server, this);
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
			double balance = cacheUpdateRequest.balance();
			switch (cacheUpdateRequest.type()) {
			case BALANCE:
				setCardBalance(uid, balance);
				break;
			case TRAVEL:
				addTravel(uid, cacheUpdateRequest.description(), balance, _cachedData.get(uid));
				break;
			case RECHARGE:
				addrecharge(uid, cacheUpdateRequest.description(), balance, _cachedData.get(uid));
				break;
			default:
				throw new IllegalStateException("Unknown type: " + cacheUpdateRequest.type());
			}
		} else if (object instanceof CacheSync) {
			CacheSync cacheFirstSync = (CacheSync) object;
			switch (cacheFirstSync.status()) {
			case REQUEST:
				if (_syncronized) {
					System.out.println(node().name() + " respondiendo pedido de syn a " + msg.getSrc());
					node().sendObject(msg.getSrc(), CacheSync.newSyncResponse(_cachedData));
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
			default:
				throw new IllegalStateException("Unknown status: " + cacheFirstSync.status());
			}
		}
	}

	@Override
	public double getCardBalance(UID id) throws RemoteException {
		Optional<UserData> userdata = _cachedData.tryGet(id);
		if (userdata.isPresent()) {
			return userdata.get().balance();
		}
		return getDataFromServer(id).balance();
	}

	private UserData getDataFromServer(UID uid) throws RemoteException {
		double balanceFromServer = server().getCardBalance(uid);
		node().sendObject(CacheUpdateRequest.newBalance(uid, balanceFromServer));
		return setCardBalance(uid, balanceFromServer);
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
			userdata = Optional.of(getDataFromServer(id));
		}
		if (userdata.get().balance() < amount) {
			return CardRegistry.OPERATION_NOT_PERMITTED_BY_BALANCE;
		}
		addTravel(id, description, amount, userdata.get());
		node().sendObject(CacheUpdateRequest.newTravel(id, amount, description));
		return userdata.get().balance();
	}

	private void addTravel(UID id, String description, double amount, UserData userdata) {
		userdata.substractBalance(description, amount);
	}

	@Override
	public double recharge(UID id, String description, double amount) throws RemoteException {
		if (!_amountValidator.apply(amount) || !_descValidator.apply(description)) {
			System.out.println(String.format("Valores invalidos: [%f, %s]", amount, description));
			return CardRegistry.CANNOT_PROCESS_REQUEST;
		}
		Optional<UserData> userdata = _cachedData.tryGet(id);
		if (!userdata.isPresent()) {
			userdata = Optional.of(getDataFromServer(id));
		}
		if (userdata.get().balance() + amount > CardRegistry.MAX_BALANCE) {
			return CardRegistry.OPERATION_NOT_PERMITTED_BY_BALANCE;
		}
		addrecharge(id, description, amount, userdata.get());
		node().sendObject(CacheUpdateRequest.newRecharge(id, amount, description));
		return userdata.get().balance();
	}

	private void addrecharge(UID id, String description, double amount, UserData userdata) {
		userdata.addBalance(description, amount);
	}
}

package ar.edu.itba.pod.mmxivii.sube.receiver;

import ar.edu.itba.pod.mmxivii.jgroups.ClusterNode;
import ar.edu.itba.pod.mmxivii.sube.common.CardRegistry;
import ar.edu.itba.pod.mmxivii.sube.common.CardService;
import ar.edu.itba.pod.mmxivii.sube.entity.CachedData;
import ar.edu.itba.pod.mmxivii.sube.entity.Operation;
import ar.edu.itba.pod.mmxivii.sube.entity.Operation.OperationType;
import ar.edu.itba.pod.mmxivii.sube.entity.UserData;
import ar.edu.itba.pod.mmxivii.sube.predicate.OnlyDigitsAndLetters;
import ar.edu.itba.pod.mmxivii.sube.predicate.PositiveDouble;
import ar.edu.itba.pod.mmxivii.sube.predicate.TwoDecimalPlacesAndLessThan100;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;

import java.rmi.RemoteException;
import java.rmi.server.UID;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.and;

public class CacheNodeReceiver extends ReceiverAdapter implements CardService {

	private final Predicate<Double> _amountValidator = and(new TwoDecimalPlacesAndLessThan100(), new PositiveDouble());
	private final Predicate<String> _descValidator = new OnlyDigitsAndLetters();
	private final ClusterNode _node;
	private CardRegistry _server;
	private final CachedData _cachedData = new CachedData();
    private boolean _online = false;

	public CacheNodeReceiver(ClusterNode node, CardRegistry server) {
		_node = checkNotNull(node);
		_server = checkNotNull(server);

        List<Address> members = _node.channel().getView().getMembers();
        //si no soy en único
        if(members.size() > 1){
            node().sendObject(CacheSync.newSyncRequest());
        }
	}

	public final ClusterNode node() {
		return _node;
	}

	public final CardRegistry server() {
		return _server;
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
		}else if(object instanceof CacheSync){
            CacheSync cacheFirstSync = (CacheSync) object;
            switch (cacheFirstSync.status()){
                case REQUEST:
                    if(_online) {
                        node().sendObject(CacheSync.newSyncResponse(this._cachedData));
                    }
                    break;
                case RESPONSE:
                    if(!_online) {
                        _cachedData.syncDataFrom(cacheFirstSync.cachedData());
                        //TODO: acá habría que registrarse con el balancer.
                        _online = true;
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
		userdata.substractBalance(amount);
		userdata.operations().add(new Operation(OperationType.TRAVEL, description, -amount));
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
		node().sendObject(CacheUpdateRequest.newTravel(id, amount, description));
		return userdata.get().balance();
	}

	private void addrecharge(UID id, String description, double amount, UserData userdata) {
		userdata.addBalance(amount);
		userdata.operations().add(new Operation(OperationType.RECHARGE, description, amount));
	}

}

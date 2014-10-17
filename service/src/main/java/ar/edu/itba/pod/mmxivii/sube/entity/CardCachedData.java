package ar.edu.itba.pod.mmxivii.sube.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import java.rmi.RemoteException;
import java.rmi.server.UID;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

public class CardCachedData {

	private final Map<UID, UserData> _balances = Maps.newHashMap();

	public void setCardBalance(UID id, double balance) {
		UserData data = _balances.get(id);
		if (data == null) {
			_balances.put(id, data = new UserData());
		}
		data.setBalance(balance);
	}

	public Optional<Double> getCardBalance(UID id) throws RemoteException {
		UserData data = _balances.get(id);
		if (data == null) {
			return Optional.absent();
		}
		return Optional.of(data.balance());
	}

	public Double travel(UID id, String description, double amount) throws RemoteException {
		UserData data = _balances.get(id);
		checkNotNull(data);
		data.substractAmount(amount);
		return data.balance();
	}

	public Double recharge(UID id, String description, double amount) throws RemoteException {
		UserData data = _balances.get(id);
		checkNotNull(data);
		data.addAmount(amount);
		return data.balance();
	}

}

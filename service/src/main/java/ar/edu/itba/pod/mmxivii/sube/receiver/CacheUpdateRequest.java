package ar.edu.itba.pod.mmxivii.sube.receiver;

import java.io.Serializable;
import java.rmi.server.UID;

public class CacheUpdateRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	public static enum RequestOperationType {
		TRAVEL, RECHARGE, BALANCE
	};

	public static CacheUpdateRequest newBalance(UID id, double balance) {
		return new CacheUpdateRequest(id, RequestOperationType.BALANCE, balance);
	}

	public static CacheUpdateRequest newRecharge(UID id, double balance, String description) {
		return new CacheUpdateRequest(id, RequestOperationType.RECHARGE, balance).setDescription(description);
	}

	public static CacheUpdateRequest newTravel(UID id, double balance, String description) {
		return new CacheUpdateRequest(id, RequestOperationType.TRAVEL, balance).setDescription(description);
	}

	private final UID _id;
	private final RequestOperationType _type;
	private final double _balance;
	private String _description;

	public CacheUpdateRequest(UID id, RequestOperationType type, double balance) {
		_id = id;
		_type = type;
		_balance = balance;
	}

	public UID uid() {
		return _id;
	}

	public RequestOperationType type() {
		return _type;
	}

	public double balance() {
		return _balance;
	}

	public CacheUpdateRequest setDescription(String description) {
		_description = description;
		return this;
	}

	public String description() {
		return _description;
	}

}

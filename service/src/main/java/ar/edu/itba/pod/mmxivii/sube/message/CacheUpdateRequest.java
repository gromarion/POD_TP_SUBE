package ar.edu.itba.pod.mmxivii.sube.message;

import java.rmi.server.UID;

public class CacheUpdateRequest {

	private final UID _id;
	private final double _balance;

	public CacheUpdateRequest(UID id, double balance) {
		_id = id;
		_balance = balance;
	}

	public double balance() {
		return _balance;
	}

	public UID id() {
		return _id;
	}
}

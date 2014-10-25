package ar.edu.itba.pod.mmxivii.sube.receiver;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;
import java.rmi.server.UID;

import ar.edu.itba.pod.mmxivii.sube.entity.Operation;

import com.google.common.base.Optional;

public class CacheUpdateRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	public static enum RequestOperationType {
		OPERATION, BALANCE
	};

	public static CacheUpdateRequest newBalance(UID id, double balance) {
		return new CacheUpdateRequest(id, balance);
	}

	public static CacheUpdateRequest newOperation(UID id, Operation operation) {
		checkArgument(operation.amount() > 0);
		return new CacheUpdateRequest(id, operation);
	}

	private final UID _id;
	private final RequestOperationType _type;
	private final Optional<Double> _balance;
	private final Optional<Operation> _operation;

	public CacheUpdateRequest() {
		// Serialization required constructor
		_id = null;
		_balance = Optional.absent();
		_type = null;
		_operation = Optional.absent();
	}

	public CacheUpdateRequest(UID id, double balance) {
		_id = id;
		_balance = Optional.of(balance);
		_operation = Optional.absent();
		_type = RequestOperationType.BALANCE;
	}

	public CacheUpdateRequest(UID id, Operation operation) {
		_id = id;
		_balance = Optional.absent();
		_operation = Optional.of(operation);
		_type = RequestOperationType.OPERATION;
	}

	public UID uid() {
		return _id;
	}

	public RequestOperationType type() {
		return _type;
	}

	public Optional<Double> balance() {
		return _balance;
	}

	public Optional<Operation> operation() {
		return _operation;
	}

}

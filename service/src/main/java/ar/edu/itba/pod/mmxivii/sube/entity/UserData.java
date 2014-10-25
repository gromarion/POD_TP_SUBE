package ar.edu.itba.pod.mmxivii.sube.entity;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;
import java.util.Set;

import com.google.common.collect.Sets;

public class UserData implements Serializable {

	private static final long serialVersionUID = 1L;

	private double _balance;
	private final Set<Operation> _operations = Sets.newHashSet();

	public UserData() {
		// Serialization interface
	}

	public double balance() {
		return _balance;
	}

	public UserData setBalance(double balance) {
		_balance = balance;
		return this;
	}

	public Set<Operation> operations() {
		return _operations;
	}

	public UserData addOperation(Operation operation) {
		checkArgument(operation.amount() > 0);
		switch (operation.type()) {
		case RECHARGE:
			setBalance(balance() + operation.amount());
			break;
		case TRAVEL:
			setBalance(balance() - operation.amount());
			break;
		default:
			throw new IllegalStateException("Unknown operation: " + operation.type());
		}
		operations().add(operation);
		return this;
	}

	@Override
	public String toString() {
		return String.format("B: %f || Ops: %s", balance(), operations().toString());
	}
}

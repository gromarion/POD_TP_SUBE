package ar.edu.itba.pod.mmxivii.sube.entity;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import ar.edu.itba.pod.mmxivii.sube.entity.Operation.OperationType;

public class UserData implements Serializable {

	private static final long serialVersionUID = 1L;

	private double _balance;
	private final Set<Operation> _operations = new TreeSet<Operation>();

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

	public UserData addBalance(String description, double amount) {
		synchronized (_operations) {
			_operations.add(new Operation(OperationType.RECHARGE, description, amount));
			return setBalance(balance() + amount);
		}
	}

	public UserData substractBalance(String description, double amount) {
		synchronized (_operations) {
			_operations.add(new Operation(OperationType.TRAVEL, description, -amount));
			return setBalance(balance() - amount);
		}
	}

	public void syncOperation(Operation operation) {
		synchronized (_operations) {
			_operations.add(operation);
			switch (operation.type()) {
			case TRAVEL:
				setBalance(balance() + operation.amount());
				break;
			case RECHARGE:
				setBalance(balance() - operation.amount());
				break;
			default:
				throw new IllegalStateException("Unknown operation: " + operation.type());
			}
		}
	}
	
	@Override
	public String toString() {
		return String.format("B: %f || Ops: %s", balance(), operations().toString());
	}
}

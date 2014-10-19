package ar.edu.itba.pod.mmxivii.sube.entity;

import java.util.Set;
import java.util.TreeSet;

public class UserData {

	private double _balance;
	private final Set<Operation> _operations = new TreeSet<Operation>();

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

	public UserData addBalance(double amount) {
		return setBalance(balance() + amount);
	}

	public UserData substractBalance(double amount) {
		return setBalance(balance() - amount);
	}
}

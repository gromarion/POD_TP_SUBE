package ar.edu.itba.pod.mmxivii.sube.entity;

import java.util.List;

import com.google.common.collect.Lists;

public class UserData {

	private double _balance;
	private final List<Operation> _operations = Lists.newLinkedList();

	public double balance() {
		return _balance;
	}

	public UserData setBalance(double balance) {
		_balance = balance;
		return this;
	}

	public List<Operation> operations() {
		return _operations;
	}

	public UserData addBalance(double amount) {
		return setBalance(balance() + amount);
	}

	public UserData substractBalance(double amount) {
		return setBalance(balance() - amount);
	}

}

package ar.edu.itba.pod.mmxivii.sube.server;

import java.rmi.server.UID;

public class UserData {

	private UID id;
	private double balance;

	public UserData(UID id, double balance) {
		this.id = id;
		this.balance = balance;
	}

	public UID getId() {
		return id;
	}

	public double getBalance() {
		return balance;
	}

	public boolean substractAmount(double amount) {
		if (this.balance > amount) {
			this.balance -= amount;
			return true;
		}
		return false;
	}

	public boolean addAmount(double amount) {
		this.balance += amount;
		return true; // TODO: Remember to validate that 'amount' should only
						// have 2 decimals (argentinian cents)!!!
	}
}

package ar.edu.itba.pod.mmxivii.sube.entity;

import java.io.Serializable;
import java.rmi.server.UID;

public class UserData implements Serializable {

	private static final long serialVersionUID = 1L;
	private UID id;
	private double balance;
	private boolean updated;

	public UserData(UID id, double balance) {
		this.id = id;
		this.balance = balance;
		this.updated = true;
	}

	public UID userId() {
		return id;
	}

	public double balance() {
		return balance;
	}
	
	public boolean isUpdated() {
		return updated;
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

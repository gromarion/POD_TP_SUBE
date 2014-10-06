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
}

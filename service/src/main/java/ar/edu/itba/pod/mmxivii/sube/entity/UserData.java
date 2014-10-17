package ar.edu.itba.pod.mmxivii.sube.entity;

public class UserData {

	private double balance;

	public UserData() {
		this(0f);
	}

	public UserData(double balance) {
		this.balance = balance;
	}

	public double balance() {
		return balance;
	}

	public UserData setBalance(double balance) {
		this.balance = balance;
		return this;
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

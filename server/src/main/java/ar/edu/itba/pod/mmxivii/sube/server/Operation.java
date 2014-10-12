package ar.edu.itba.pod.mmxivii.sube.server;

import java.rmi.server.UID;
import java.util.Date;

import org.jgroups.Address;

public class Operation {
	private Date date;
	private UID user_id;
	private Address address;
	private Type type;
	private boolean completed;

	public Operation(Type type, UID user_id, Address address, boolean completed) {
		this.type = type;
		this.user_id = user_id;
		this.address = address;
		this.date = new Date();
		this.completed = completed;
	}

	public UID userId() {
		return this.user_id;
	}

	public Type type() {
		return this.type;
	}

	public Date date() {
		return this.date;
	}

	public boolean isCompleted() {
		return completed;
	}
	
	public Address address() {
		return address;
	}
}

package ar.edu.itba.pod.mmxivii.sube.entity;

import java.rmi.server.UID;
import java.util.Date;

import org.jgroups.Address;

public class Operation {
	private Date date;
	private UID user_id;
	private Address address;
	private OperationType type;
	private boolean completed;

	public Operation(OperationType type, UID user_id, Address address, boolean completed) {
		this.type = type;
		this.user_id = user_id;
		this.address = address;
		this.date = new Date();
		this.completed = completed;
	}

	public UID userId() {
		return this.user_id;
	}

	public OperationType type() {
		return this.type;
	}

	public Date date() {
		return this.date;
	}

	public boolean isCompleted() {
		return this.completed;
	}
	
	public void complete() {
		this.completed = true;
	}
	
	public Address address() {
		return this.address;
	}
}

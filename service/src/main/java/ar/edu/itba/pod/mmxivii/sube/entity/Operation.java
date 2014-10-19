package ar.edu.itba.pod.mmxivii.sube.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;

public class Operation implements Comparable<Operation> {

	public static enum OperationType {
		TRAVEL, RECHARGE
	};

	private OperationType _type;
	private String _description;
	private double _amount;
	private Date _timestamp;

	public Operation(OperationType type, String description, double amount) {
		_type = checkNotNull(type);
		_description = checkNotNull(description);
		_amount = amount;
		_timestamp = new Date();
	}

	public double amount() {
		return _amount;
	}

	public String description() {
		return _description;
	}

	public OperationType type() {
		return _type;
	}

	@Override
	public int compareTo(Operation o) {
		return _timestamp.compareTo(o._timestamp);
	}
}

package ar.edu.itba.pod.mmxivii.sube.entity;

import static com.google.common.base.Preconditions.checkNotNull;

public class Operation {

	public static enum OperationType {
		TRAVEL, RECHARGE
	};

	private OperationType _type;
	private String _description;
	private double _amount;

	public Operation(OperationType type, String description, double amount) {
		_type = checkNotNull(type);
		_description = checkNotNull(description);
		_amount = amount;
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
}

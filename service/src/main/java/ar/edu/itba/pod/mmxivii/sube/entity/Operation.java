package ar.edu.itba.pod.mmxivii.sube.entity;

import java.io.Serializable;
import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

public class Operation implements Comparable<Operation>, Serializable {

	private static final long serialVersionUID = 1L;

	public static enum OperationType {
		TRAVEL, RECHARGE
	};

	private OperationType _type;
	private String _description;
	private double _amount;
	private Date _timestamp;

	public Operation() {
		// Serialization interface
	}

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Operation)) return false;

        Operation operation = (Operation) o;

        if (_timestamp != null ? !_timestamp.equals(operation._timestamp) : operation._timestamp != null) return false;
        if (_type != operation._type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = _type != null ? _type.hashCode() : 0;
        result = 31 * result + (_timestamp != null ? _timestamp.hashCode() : 0);
        return result;
    }
}

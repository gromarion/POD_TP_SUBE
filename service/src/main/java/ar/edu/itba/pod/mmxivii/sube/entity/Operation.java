package ar.edu.itba.pod.mmxivii.sube.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.joda.time.LocalDateTime;

public class Operation implements Serializable {

	private static final long serialVersionUID = 1L;

	public static enum OperationType {
		TRAVEL, RECHARGE
	};

	private OperationType _type;
	private String _description;
	private double _amount;
	private LocalDateTime _timestamp;

	public Operation() {
		// Serialization interface
	}

	public Operation(OperationType type, String description, double amount, LocalDateTime timestamp) {
		_type = checkNotNull(type);
		_description = checkNotNull(description);
		_amount = amount;
		_timestamp = timestamp;
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

	public LocalDateTime timestamp() {
		return _timestamp;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Operation)) {
			return false;
		}
		Operation other = (Operation) o;
		return new EqualsBuilder().append(description(), other.description()).append(type(), other.type()).append(timestamp(), other.timestamp()).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(description()).append(type()).append(timestamp()).build();
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}

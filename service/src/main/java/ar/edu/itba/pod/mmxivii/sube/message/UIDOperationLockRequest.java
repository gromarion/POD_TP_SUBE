package ar.edu.itba.pod.mmxivii.sube.message;

import java.io.Serializable;
import java.rmi.server.UID;

@SuppressWarnings("serial")
public class UIDOperationLockRequest implements Serializable {

	public static enum OperationRequestType {
		LOCK, UNLOCK
	};

	private final UID _id;
	private final OperationRequestType _type;

	public UIDOperationLockRequest(UID id, OperationRequestType type) {
		_id = id;
		_type = type;
	}

	public UID id() {
		return _id;
	}

	public OperationRequestType type() {
		return _type;
	}
}

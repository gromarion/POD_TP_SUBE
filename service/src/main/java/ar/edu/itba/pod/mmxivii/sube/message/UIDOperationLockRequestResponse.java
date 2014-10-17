package ar.edu.itba.pod.mmxivii.sube.message;

import java.io.Serializable;

@SuppressWarnings("serial")
public class UIDOperationLockRequestResponse implements Serializable {

	private final boolean _lockAccepted;

	public UIDOperationLockRequestResponse(boolean lockAccepted) {
		_lockAccepted = lockAccepted;
	}

	public boolean isLockAccepted() {
		return _lockAccepted;
	}
}

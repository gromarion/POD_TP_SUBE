package ar.edu.itba.pod.mmxivii.sube.receiver;

import ar.edu.itba.pod.mmxivii.sube.entity.CachedData;

import java.io.Serializable;

public class CacheSyncRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	public static enum SyncStatus {
		REQUEST, RESPONSE, UPDATE
	};

	public static CacheSyncRequest newSyncRequest() {
		return new CacheSyncRequest(null, SyncStatus.REQUEST);
	}

	public static CacheSyncRequest newSyncResponse(CachedData cachedData) {
		return new CacheSyncRequest(cachedData, SyncStatus.RESPONSE);
	}

	public static CacheSyncRequest newSyncUpdate(CachedData cachedData) {
		return new CacheSyncRequest(cachedData, SyncStatus.UPDATE);
	}

	private final CachedData _cachedData;
	private final SyncStatus _status;

	public CacheSyncRequest() {
		// Serialization required constructor
		_cachedData = null;
		_status = null;
	}

	public CacheSyncRequest(CachedData cachedData, SyncStatus status) {
		_cachedData = cachedData;
		_status = status;
	}

	public CachedData data() {
		return _cachedData;
	}

	public SyncStatus status() {
		return _status;
	}
}

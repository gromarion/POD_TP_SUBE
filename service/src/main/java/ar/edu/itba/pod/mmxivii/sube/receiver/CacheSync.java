package ar.edu.itba.pod.mmxivii.sube.receiver;

import java.io.Serializable;

import ar.edu.itba.pod.mmxivii.sube.entity.CachedData;

public class CacheSync implements Serializable {

	private static final long serialVersionUID = 1L;

	public static enum SyncStatus {
		REQUEST, RESPONSE, UPDATE
	};

	public static CacheSync newSyncRequest() {
		return new CacheSync(null, SyncStatus.REQUEST);
	}

	public static CacheSync newSyncResponse(CachedData cachedData) {
		return new CacheSync(cachedData, SyncStatus.RESPONSE);
	}

	// FIXME: este es necesario?
	public static CacheSync newSyncUpdate(CachedData cachedData) {
		return new CacheSync(cachedData, SyncStatus.UPDATE);
	}

	private final CachedData _cachedData;
	private final SyncStatus _status;

	public CacheSync() {
		// Serialization required constructor
		_cachedData = null;
		_status = null;
	}

	public CacheSync(CachedData cachedData, SyncStatus status) {
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

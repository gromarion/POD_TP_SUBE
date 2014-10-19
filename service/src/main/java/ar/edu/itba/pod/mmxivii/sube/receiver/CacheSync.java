package ar.edu.itba.pod.mmxivii.sube.receiver;

import ar.edu.itba.pod.mmxivii.sube.entity.CachedData;

import java.io.Serializable;

public class CacheSync implements Serializable{

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
    
    public static CacheSync newSyncUpdate(CachedData cachedData) {
    	return new CacheSync(cachedData, SyncStatus.UPDATE);
    }

    public CacheSync(CachedData cachedData, SyncStatus status) {
        this._cachedData = cachedData;
        this._status = status;
    }

    private final CachedData _cachedData;
    private final SyncStatus _status;

    public CachedData cachedData() {
        return _cachedData;
    }

    public SyncStatus status() {
        return _status;
    }
}

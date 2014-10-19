package ar.edu.itba.pod.mmxivii.sube.receiver;

import ar.edu.itba.pod.mmxivii.sube.entity.CachedData;

import java.io.Serializable;

public class CacheFirstSync implements Serializable{

    private static final long serialVersionUID = 1L;

    public static enum SyncStatus {
        REQUEST, RESPONSE
    };

    public static CacheFirstSync newSyncRequest(){
        return new CacheFirstSync(null, SyncStatus.REQUEST);
    }

    public static CacheFirstSync newSyncResponse(CachedData cachedData){
        return new CacheFirstSync(cachedData, SyncStatus.RESPONSE);
    }

    public CacheFirstSync(CachedData cachedData, SyncStatus status){
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

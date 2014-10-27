package ar.edu.itba.pod.mmxivii.sube.entity;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.Serializable;
import java.rmi.server.UID;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

public class CachedData implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Map<UID, UserData> _userdatas = Maps.newHashMap();

	public Set<UID> getUsers() {
		return _userdatas.keySet();
	}

	public UserData get(UID uid) {
		synchronized (_userdatas) {
			return checkNotNull(_userdatas.get(uid));
		}
	}

	public Optional<UserData> tryGet(UID uid) {
		synchronized (_userdatas) {
			return Optional.fromNullable(_userdatas.get(uid));
		}
	}

	public CachedData put(UID uid, UserData userdata) {
		synchronized (_userdatas) {
			UserData previous = _userdatas.put(uid, userdata);
			checkArgument(previous == null);
			return this;
		}
	}

	public void setTo(CachedData other) {
		synchronized (_userdatas) {
			checkState(_userdatas.isEmpty());
			_userdatas.putAll(other._userdatas);
		}
	}

	public void clearAll(CachedData data) {
		synchronized (_userdatas) {
			for (Entry<UID, UserData> entry : data._userdatas.entrySet()) {
				_userdatas.get(entry.getKey()).operations().removeAll(entry.getValue().operations());
			}
		}
	}
}

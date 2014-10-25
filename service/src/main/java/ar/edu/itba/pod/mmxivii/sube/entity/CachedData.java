package ar.edu.itba.pod.mmxivii.sube.entity;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

import java.io.Serializable;
import java.rmi.server.UID;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.*;

public class CachedData implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Map<UID, UserData> _userdatas = Maps.newHashMap();

	public Set<UID> getUsers() {
		return _userdatas.keySet();
	}

	public UserData get(UID uid) {
		return checkNotNull(_userdatas.get(uid));
	}

	public Optional<UserData> tryGet(UID uid) {
		return Optional.fromNullable(_userdatas.get(uid));
	}

	public CachedData put(UID uid, UserData userdata) {
		UserData previous = _userdatas.put(uid, userdata);
		checkArgument(previous == null);
		return this;
	}

	public void setTo(CachedData other) {
		checkState(_userdatas.isEmpty());
		_userdatas.putAll(other._userdatas);
	}

    public void clear(CachedData data) {
        for (UID user : data.getUsers()) {
            for (Operation operation : data.get(user).operations()) {
                data.get(user).operations().remove(operation);
            }
        }
    }
}

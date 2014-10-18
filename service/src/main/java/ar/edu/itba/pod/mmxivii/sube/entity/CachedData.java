package ar.edu.itba.pod.mmxivii.sube.entity;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.rmi.server.UID;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

public class CachedData {

	private final Map<UID, UserData> _userdatas = Maps.newHashMap();

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

}

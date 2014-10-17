package ar.edu.itba.pod.mmxivii.sube.entity;

import java.rmi.server.UID;
import java.util.Set;

import com.google.common.collect.Sets;

public class LeaderNodeData {

	private final Set<UID> _locks = Sets.newHashSet();

	public boolean lock(UID id) {
		return _locks.add(id);
	}

	public void release(UID id) {
		_locks.remove(id);
	}

}

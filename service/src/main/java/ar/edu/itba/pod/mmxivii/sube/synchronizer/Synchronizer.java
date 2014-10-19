package ar.edu.itba.pod.mmxivii.sube.synchronizer;

import static com.google.common.base.Preconditions.checkNotNull;

import java.rmi.RemoteException;
import java.rmi.server.UID;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;

import ar.edu.itba.pod.mmxivii.jgroups.ClusterNode;
import ar.edu.itba.pod.mmxivii.sube.common.CardRegistry;
import ar.edu.itba.pod.mmxivii.sube.entity.CachedData;
import ar.edu.itba.pod.mmxivii.sube.entity.Operation;
import ar.edu.itba.pod.mmxivii.sube.receiver.CacheNodeReceiver;
import ar.edu.itba.pod.mmxivii.sube.receiver.CacheSync;

public class Synchronizer extends ReceiverAdapter {

	private Map<Integer, Address> _votes;
	private double _selected_number;
	private Date _last_vote;
	private ClusterNode _node;
	private CardRegistry _server;
	private static final int START_VOTATION = -1;
	private static final int GET_NODE_TYPE = -2;

	public Synchronizer(ClusterNode node, CardRegistry server) {
		_votes = new HashMap<Integer, Address>();
		_node = checkNotNull(node);
		_server = server;
	}

	public void vote(boolean this_started_to_vote) {
		_last_vote = new Date();
		_selected_number = new Random().nextInt(Integer.MAX_VALUE);
		if (this_started_to_vote)
			node().sendObject(-1);
		node().sendObject(_selected_number);
	}

	@Override
	public void receive(Message msg) {
		if (msg.getSrc().equals(node().address()))
			return;
		if (msg.getObject() instanceof Integer) {
			if ((Integer) msg.getObject() == START_VOTATION)
				vote(false);
			else if ((Integer) msg.getObject() == GET_NODE_TYPE)
				node().sendObject(msg.getSrc(), getClass());
			else if (!addVote(msg))
				node().sendObject(-1);
			else
				if (_votes.keySet().size() == node().members().size() - 1)
					askDataToUpdateIfCoordinator();
		} else if (msg.getObject() instanceof CacheNodeReceiver)
			getDataFromNode(msg.getSrc());
		else if (msg.getObject() instanceof CachedData)
			updateServer((CachedData) msg.getObject());
	}

	public final ClusterNode node() {
		return _node;
	}

	private boolean addVote(Message msg) {
		if (_votes.containsKey((Double) msg.getObject()))
			return false;
		else
			_votes.put((Integer) msg.getObject(), msg.getSrc());
		return true;
	}

	private void askDataToUpdateIfCoordinator() {
		double max_number = 0;
		for (Integer vote : _votes.keySet())
			max_number = vote > max_number ? vote : max_number;
		if (_votes.get(max_number).equals(node().address()))
			askEveryOneTheirType();
	}

	private void askEveryOneTheirType() {
		node().sendObject(GET_NODE_TYPE);
	}

	private void getDataFromNode(Address node_address) {
		node().sendObject(node_address, CacheSync.newSyncRequest());
	}

	private void updateServer(CachedData cached_data) {
		for (UID uid : cached_data.getUsers()) {
			for (Operation operation : cached_data.get(uid).operations()) {
				try {
					_server.addCardOperation(uid, operation.type().toString(),
							operation.amount());
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
		node().sendObject(CacheSync.newSyncUpdate(cached_data));
	}
}

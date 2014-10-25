package ar.edu.itba.pod.mmxivii.sube.receiver;

import static com.google.common.base.Preconditions.checkNotNull;

import java.rmi.RemoteException;
import java.rmi.server.UID;
import java.util.Calendar;
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

public class Synchronizer extends ReceiverAdapter {

	private Map<Integer, Address> _votes;
	private double _selected_number;
	private int _last_vote;
	private ClusterNode _node;
	private CardRegistry _server;
	private static final int START_VOTATION = -1;
	private static final int GET_NODE_TYPE = -2;
	private static final int MAX_SECONDS_WITHOUT_VOTING = 15;

	public Synchronizer(ClusterNode node, CardRegistry server) {
		_votes = new HashMap<Integer, Address>();
		_node = checkNotNull(node);
		_server = server;
	}

	public void run() {
		while (true)
			if (mustVoteAgain())
				vote(true);
	}

	public void vote(boolean this_started_to_vote) {
		System.out.println(_node.address() + " is BOATING!");
		_last_vote = Calendar.getInstance().get(Calendar.SECOND);
		_selected_number = new Random().nextInt(Integer.MAX_VALUE);
		if (this_started_to_vote)
			_node.sendObject(-1);
		_node.sendObject(_selected_number);
	}

	@Override
	public void receive(Message msg) {
		if (msg.getSrc().equals(_node.address()))
			return;
		if (msg.getObject() instanceof Integer) {
			if ((Integer) msg.getObject() == START_VOTATION)
				vote(false);
			else if ((Integer) msg.getObject() == GET_NODE_TYPE)
				_node.sendObject(msg.getSrc(), getClass());
			else if (!addVote(msg))
				_node.sendObject(-1);
			else if (_votes.keySet().size() == _node.members().size() - 1) {
				System.out.println("Finished voting!");
				askDataToUpdateIfCoordinator();
			}
		} else if (msg.getObject().equals(CacheNodeReceiver.class)) {
			System.out.println("Asking " + msg.getSrc() + " for data");
			getDataFromNode(msg.getSrc());
		} else if (msg.getObject() instanceof CachedData) {
			System.out.println("Received data from " + msg.getSrc());
			updateServer((CachedData) msg.getObject());
		}
	}

	public boolean mustVoteAgain() {
		return _last_vote == 0
				|| Calendar.getInstance().get(Calendar.SECOND) - _last_vote < MAX_SECONDS_WITHOUT_VOTING;
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
		if (_votes.get(max_number).equals(_node.address())) {
			System.out.println(_node.address() + " is the leader!");
			askEveryOneTheirType();
		}
	}

	private void askEveryOneTheirType() {
		_node.sendObject(GET_NODE_TYPE);
	}

	private void getDataFromNode(Address node_address) {
		_node.sendObject(node_address, CacheSync.newSyncRequest());
	}

	private void updateServer(CachedData cached_data) {
		System.out.println("Updating server");
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
		_node.sendObject(CacheSync.newSyncUpdate(cached_data));
	}
}

package ar.edu.itba.pod.mmxivii.sube.receiver;

import static ar.edu.itba.pod.mmxivii.sube.common.Utils.CARD_REGISTRY_BIND;
import static com.google.common.base.Preconditions.checkNotNull;

import java.rmi.NotBoundException;
import java.rmi.server.UID;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.joda.time.LocalDateTime;

import ar.edu.itba.pod.mmxivii.jgroups.ClusterNode;
import ar.edu.itba.pod.mmxivii.sube.common.CardRegistry;
import ar.edu.itba.pod.mmxivii.sube.common.Utils;
import ar.edu.itba.pod.mmxivii.sube.entity.Operation;
import ar.edu.itba.pod.mmxivii.util.Threads;

public class SynchronizerReceiver extends ReceiverAdapter implements Runnable {

	private static final int BOATING_TIME_TIMEOUT = 10;
	private static final int START_ELECTION = -1;
	private static final int GET_NODE_TYPE = -2;
	private static final int MAX_SECONDS_WITHOUT_VOTING = 15;

	private Map<Address, Integer> _votes;
	private boolean _boating = false;
	private boolean _is_asking_cache_for_info;
	private int _selected_number;
	private LocalDateTime _last_vote;
	private ClusterNode _node;

	private boolean _serverIsDown;
	private CardRegistry _server;

	public SynchronizerReceiver(ClusterNode node) {
		_votes = new HashMap<Address, Integer>();
		_node = checkNotNull(node);
		_serverIsDown = true;
	}

	public final CardRegistry server() {
		if (_serverIsDown) {
			try {
				_server = Utils.lookupObject(CARD_REGISTRY_BIND);
				_serverIsDown = false;
			} catch (NotBoundException e) {
				_serverIsDown = true;
			}
		}
		return _server;
	}

	@Override
	public void run() {
		while (true) {
			if (mustVoteAgain()) {
				vote(true);
			}
			if (_boating && LocalDateTime.now().minusSeconds(_last_vote.getSecondOfMinute()).getSecondOfMinute() > BOATING_TIME_TIMEOUT) {
				_boating = false;
				askDataToUpdateIfCoordinator();
			}
            Threads.sleep(1, TimeUnit.SECONDS);
		}
	}

	public void vote(boolean this_started_to_vote) {
//		System.out.println(_node.address() + " is BOATING!");
		_last_vote = LocalDateTime.now();
		_selected_number = new Random().nextInt(Integer.MAX_VALUE);
		_boating = true;
		if (this_started_to_vote)
			_node.sendObject(START_ELECTION);
		_node.sendObject(_selected_number);
	}

	@Override
	public void receive(Message msg) {
		if (msg.getSrc().equals(_node.address()))
			return;
		if (msg.getObject() instanceof Integer) {
			int received_value = (Integer) msg.getObject();
			if (received_value == START_ELECTION) {
//				System.out.println(_node.address() + " must vote!");
				vote(false);
			} else if (received_value == GET_NODE_TYPE)
				_node.sendObject(msg.getSrc(), getClass());
			else {
				if (_boating) {
					if (!addVote(msg)) {
						vote(true);
					}
				}
			}
		} else if (msg.getObject().equals(CacheNodeReceiver.class) && !_is_asking_cache_for_info) {
			_is_asking_cache_for_info = true;
			System.out.println("Asking " + msg.getSrc() + " for data");
			getDataFromNode(msg.getSrc());
		} else if (msg.getObject() instanceof CacheSyncRequest && _is_asking_cache_for_info) {
			System.out.println("Received data from " + msg.getSrc());
			updateServer((CacheSyncRequest) msg.getObject());
		}
	}

	public boolean mustVoteAgain() {
		return _last_vote == null || LocalDateTime.now().minusSeconds(_last_vote.getSecondOfMinute()).getSecondOfMinute() > MAX_SECONDS_WITHOUT_VOTING;
	}

	private boolean addVote(Message msg) {
		if (_votes.containsValue(msg.getObject()))
			return false;
		else
			_votes.put(msg.getSrc(), (Integer) msg.getObject());
		return true;
	}

	private void askDataToUpdateIfCoordinator() {
		int max_number = 0;
		for (Integer vote : _votes.values())
			max_number = vote > max_number ? vote : max_number;
		if (_selected_number > max_number) {
			System.out.println(_node.address() + " is the leader!");
			askEveryOneTheirType();
		}
		_votes.clear();
	}

	private void askEveryOneTheirType() {
		_node.sendObject(GET_NODE_TYPE);
	}

	private void getDataFromNode(Address node_address) {
		_node.sendObject(node_address, CacheSyncRequest.newSyncRequest());
	}

	private void updateServer(CacheSyncRequest cache_sync) {
		System.out.println(_node.name() + " Updating server");
		try {
			for (UID uid : cache_sync.data().getUsers()) {
				List<Operation> ops = new LinkedList<>(cache_sync.data().get(uid).operations());
				Collections.sort(ops, new TimestampDesc());
				for (Operation operation : ops) {
					server().addCardOperation(uid, operation.type().toString(), operation.amount());
				}
			}
			_is_asking_cache_for_info = false;
			_node.sendObject(CacheSyncRequest.newSyncUpdate(cache_sync.data()));
		} catch (Exception e) {
			e.printStackTrace();
			_serverIsDown = true;
			System.out.println("Server is down, can not syncronize!!");
			
		}
	}
	
	private static final class TimestampDesc implements Comparator<Operation> {

		@Override
		public int compare(Operation arg0, Operation arg1) {
			return arg0.timestamp().compareTo(arg1.timestamp());
		}
		
	}
}

package ar.edu.itba.pod.mmxivii.sube.receiver;

import ar.edu.itba.pod.mmxivii.jgroups.ClusterNode;
import ar.edu.itba.pod.mmxivii.sube.common.CardRegistry;
import ar.edu.itba.pod.mmxivii.sube.entity.Operation;
import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.joda.time.LocalDateTime;

import java.rmi.RemoteException;
import java.rmi.server.UID;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.google.common.base.Preconditions.checkNotNull;

public class Synchronizer extends ReceiverAdapter implements Runnable {

	private Map<Address, Integer> _votes;
    private static final int BOATING_TIME_TIMEOUT = 3;
    private boolean _boating = false;
    private boolean _is_asking_cache_for_info;
	private int _selected_number;
	private LocalDateTime _last_vote;
	private ClusterNode _node;
	private CardRegistry _server;
	private static final int START_ELECTION = -1;
	private static final int GET_NODE_TYPE = -2;
	private static final int MAX_SECONDS_WITHOUT_VOTING = 15;

	public Synchronizer(ClusterNode node, CardRegistry server) {
		_votes = new HashMap<Address, Integer>();
		_node = checkNotNull(node);
		_server = server;
	}

    @Override
	public void run() {
		while (true) {
            if (mustVoteAgain()) {
                vote(true);
            }
            if(_boating &&
                    LocalDateTime.now().minusSeconds(_last_vote.getSecondOfMinute()).getSecondOfMinute() > BOATING_TIME_TIMEOUT
                    ) {

                _boating = false;
                askDataToUpdateIfCoordinator();
            }
        }
	}

	public void vote(boolean this_started_to_vote) {
		System.out.println(_node.address() + " is BOATING!");
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
				System.out.println("I must vote!");
				vote(false);
			}
			else if (received_value == GET_NODE_TYPE)
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
		} else if (msg.getObject() instanceof CacheSync && _is_asking_cache_for_info) {
			System.out.println("Received data from " + msg.getSrc());
			updateServer((CacheSync) msg.getObject());
		}
	}

	public boolean mustVoteAgain() {
		return _last_vote == null
				|| LocalDateTime.now().minusSeconds(_last_vote.getSecondOfMinute()).getSecondOfMinute() > MAX_SECONDS_WITHOUT_VOTING;
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
		_node.sendObject(node_address, CacheSync.newSyncRequest());
	}

	private void updateServer(CacheSync cache_sync) {
		System.out.println("Updating server");
		for (UID uid : cache_sync.data().getUsers()) {
			for (Operation operation : cache_sync.data().get(uid).operations()) {
				try {
					_server.addCardOperation(uid, operation.type().toString(),
							operation.amount());
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
        _is_asking_cache_for_info = false;
		_node.sendObject(CacheSync.newSyncUpdate(cache_sync.data()));
	}
}

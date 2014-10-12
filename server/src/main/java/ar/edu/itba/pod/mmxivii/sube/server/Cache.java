package ar.edu.itba.pod.mmxivii.sube.server;

import java.rmi.RemoteException;
import java.rmi.server.UID;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

import ar.edu.itba.pod.mmxivii.sube.common.CardService;

public class Cache extends ReceiverAdapter implements CardService {

	private JChannel channel;
	private Map<UID, UserData> user_data;
	private Map<Address, Map<UID, Queue<Operation>>> pending_operations;
	private Address controlled_cache;

	public Cache(String cluster_name, String service_name) throws Exception {
		this.channel = new JChannel();
		this.channel.setReceiver(this);
		this.channel.connect(cluster_name);
		this.user_data = new HashMap<UID, UserData>();
		this.pending_operations = new HashMap<Address, Map<UID, Queue<Operation>>>();
		this.controlled_cache = addressToControl(true);
	}

	public static void main(String[] args) throws Exception {
		Cache cl = new Cache("cacota", args[0]);
		System.out.println(cl.channel.getView().getMembers().get(0)
				+ " is the leader.");
		while (true) {
			if (cl.controlledCacheIsDead()) {
				System.out.println(cl.controlled_cache + " has died!");
				cl.controlled_cache = cl.addressToControl(false);
			}
			System.out.println("Now controlling " + cl.controlled_cache);
		}
	}

	/**
	 * There are two options: the message from the channel is an incoming
	 * operation, completed or not, or it is updated data from a particular
	 * user. If it is an incomplete operation announced from another cache, then
	 * the operation is stored in a queue and marked as pending. If it is a
	 * completed operation, it is removed from the pending operations queue. If
	 * it is an updated data from a certain user, it is added to a map UID,
	 * UserData.
	 * 
	 * @param msg
	 *            A message received from the channel.
	 */
	public void receive(Message msg) {
		if (!msg.getSrc().equals(channel.getAddress())) {
			if (msg.getObject() instanceof Operation) {
				handleOperation((Operation) msg.getObject());
			} else if (msg.getObject() instanceof UserData) {
				updateUserData((UserData) msg.getObject());
			}
		}
	}

	/**
	 * Returns the amount of money left in the card of a given user. Before
	 * handling the operation, the cache informs every other cache in the
	 * channel that he is about to make this operation. When he finishes, he
	 * informs the other caches that the operation has been completed. This is
	 * done in case this cache stops working, any other cache could take care of
	 * the pending operation.
	 * 
	 * @param id
	 *            The UID of the user.
	 * @return The balance of the card for the specified user.
	 */
	@Override
	public double getCardBalance(UID id) throws RemoteException {
		UserData data = prepareForOperation(id, OperationType.BALANCE);
		if (data != null) {
			return replicateOperation(data, OperationType.BALANCE);
		} else {
			return 0; // Communication with server should be here.
		}
	}

	@Override
	public double travel(UID id, String description, double amount)
			throws RemoteException {
		UserData data = prepareForOperation(id, OperationType.TRAVEL);
		if (data != null) {
			if (data.substractAmount(amount))
				return replicateOperation(data, OperationType.TRAVEL);
			else {
				// Notify balancer about error (throw exception???)
			}
		} else {

		}
		return 0;
	}

	@Override
	public double recharge(UID id, String description, double amount)
			throws RemoteException {
		UserData data = prepareForOperation(id, OperationType.RECHARGE);
		if (data != null) {
			if (data.addAmount(amount))
				return replicateOperation(data, OperationType.RECHARGE);
			else {
				// Notify balancer about error (throw exception???)
			}
		} else {
			// Leader must get info and send it back.
		}
		return 0;
	}

	/**
	 * When a new cache is connected to the channel, it controls the first of
	 * the caches in the member list, and the one that was doing it before will
	 * control the newly connected.
	 * 
	 * @param new_view
	 *            The view that is about to connect.
	 */
	public void viewAccepted(View new_view) {
		List<Address> members = new_view.getMembers();
		if (controlled_cache == null)
			controlled_cache = members.get(members.size() - 1);
	}

	/**
	 * Every cache controls some other to see if it is still alive or not. This
	 * method returns weather the controlled cache is still alive or not.
	 * 
	 * @return If the controlled cache is dead or not.
	 */
	public boolean controlledCacheIsDead() {
		return !this.channel.getView().getMembers().contains(controlled_cache);
	}

	// PRIVATE METHODS

	private UserData prepareForOperation(UID id, OperationType operation_type) {
		Operation operation = new Operation(operation_type, id,
				this.channel.getAddress(), false);
		informEveryoneAboutPendingOperation(operation);
		return user_data.get(id);
	}

	private double replicateOperation(UserData data,
			OperationType operation_type) {
		informEveryoneOfCompletedOperation(new Operation(operation_type,
				data.userId(), this.channel.getAddress(), true));
		sendDataToEveryone(data);
		return data.balance();
	}

	private void handleOperation(Operation operation) {
		if (operation.isCompleted())
			removePendingOperation(operation);
		else
			addPendingOperation(operation);
	}

	private void updateUserData(UserData new_data) {
		user_data.put(new_data.userId(), new_data);
	}

	private Address addressToControl(boolean first_connection) {
		List<Address> members = this.channel.getView().getMembers();
		if (first_connection) {
			if (members.size() != 1)
				return members.get(0);
			else
				return null;
		} else {
			int index_of_next_one_in_line = nextOneInLine(members);
			if (members.get(index_of_next_one_in_line).equals(
					this.channel.getAddress()))
				return null;
			return members.get(index_of_next_one_in_line);
		}
	}

	private int nextOneInLine(List<Address> members) {
		boolean next_one_in_line = false;
		int i = 0;
		for (Address address : members) {
			if (next_one_in_line) {
				if (isLastInLine(members, address))
					return 0;
				else
					return i;
			} else if (address.equals(this.channel.getAddress()))
				next_one_in_line = true;
			i++;
		}
		return 0;
	}

	private boolean isLastInLine(List<Address> members, Address address) {
		return members.get(members.size() - 1).equals(address);
	}

	private void informEveryoneAboutPendingOperation(Operation operation) {
		sendDataToEveryone(operation);
	}

	private void informEveryoneOfCompletedOperation(Operation operation) {
		operation.complete();
		sendDataToEveryone(operation);
	}

	private void addPendingOperation(Operation operation) {
		if (pending_operations.containsKey(operation.address())) {
			if (pending_operations.get(operation.address()).containsKey(
					operation.userId()))
				pending_operations.get(operation.address())
						.get(operation.userId()).add(operation);
			else {
				LinkedList<Operation> queue = new LinkedList<Operation>();
				queue.add(operation);
				pending_operations.get(operation.address()).put(
						operation.userId(), queue);
			}
		} else {
			LinkedList<Operation> queue = new LinkedList<Operation>(); // TODO:
																		// Verificar
																		// que
																		// esto
																		// sea
																		// ordenado.
																		// CLAVE
			queue.add(operation);
			Map<UID, Queue<Operation>> map = new HashMap<UID, Queue<Operation>>();
			map.put(operation.userId(), queue);
			pending_operations.put(operation.address(), map);
		}
	}

	private void removePendingOperation(Operation operation) {
		Queue<Operation> address_pending_operations = pending_operations.get(
				operation.address()).get(operation.userId());
		if (address_pending_operations.peek().equals(operation))
			address_pending_operations.poll();
	}

	private void sendDataToEveryone(Object o) {
		try {
			channel.send(new Message(null, null, o));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

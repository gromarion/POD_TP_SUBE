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
	private Address controlling;

	public Cache(String cluster_name, String service_name) throws Exception {
		this.channel = new JChannel();
		this.channel.setReceiver(this);
		this.channel.connect(cluster_name);
		this.user_data = new HashMap<UID, UserData>();
		this.pending_operations = new HashMap<Address, Map<UID, Queue<Operation>>>();
		this.controlling = addressToControl(true);
	}

	public static void main(String[] args) throws Exception {
		Cache cl = new Cache("cacota", args[0]);
		System.out.println(cl.channel.getView().getMembers().get(0)
				+ " is the leader.");
		while (true) {
			if (cl.controllingIsDead()) {
				System.out.println(cl.controlling + " has died!");
				cl.controlling = cl.addressToControl(false);
			}
			System.out.println("Now controlling " + cl.controlling);
		}
	}

	public void receive(Message msg) {
		if (!msg.getSrc().equals(channel.getAddress())) {
			if (msg.getObject() instanceof Operation) {
				Operation operation = (Operation) msg.getObject();
				if (operation.isCompleted())
					removePendingOperation(operation);
				else
					addPendingOperation(operation);
			} else if (msg.getObject() instanceof UserData) {
				UserData new_data = (UserData) msg.getObject();
				user_data.put(new_data.userId(), new_data);
			}
		}
	}

	@Override
	public double getCardBalance(UID id) throws RemoteException {
		Operation operation = new Operation(Type.GET, id,
				this.channel.getAddress(), false);
		informEveryoneAboutPendingOperation(operation);
		UserData data = user_data.get(id);
		if (data != null) {
			informEveryoneOfCompletedOperation(operation);
			sendDataToEveryone(data);
			return data.balance();
		} else {
			return 0;
		}
	}

	@Override
	public double travel(UID id, String description, double amount)
			throws RemoteException {
		UserData data = this.user_data.get(id);
		if (data != null) {
			if (data.substractAmount(amount)) {
				return data.balance();
			} else {
				// Notify balancer about error (throw exception???)
			}
		} else {

		}
		return 0;
	}

	@Override
	public double recharge(UID id, String description, double amount)
			throws RemoteException {
		UserData data = this.user_data.get(id);
		if (data != null) {
			if (data.addAmount(amount)) {
				return data.balance();
			} else {
				// Notify balancer about error (throw exception???)
			}
		} else {
			// Leader must get info and send it back.
		}
		return 0;
	}

	public void viewAccepted(View new_view) {
		List<Address> members = new_view.getMembers();
		if (controlling == null)
			controlling = members.get(members.size() - 1);
	}

	public boolean controllingIsDead() {
		return !this.channel.getView().getMembers().contains(controlling);
	}

	// PRIVATE METHODS

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
			} else if (address.equals(this.channel.getAddress())) {
				next_one_in_line = true;
			}
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
		Queue<Operation> address_pending_operations = pending_operations.get(operation.address()).get(operation.userId());
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

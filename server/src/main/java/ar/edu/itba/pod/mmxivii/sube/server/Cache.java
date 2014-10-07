package ar.edu.itba.pod.mmxivii.sube.server;

import java.rmi.RemoteException;
import java.rmi.server.UID;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;

import ar.edu.itba.pod.mmxivii.sube.common.CardService;

public class Cache extends ReceiverAdapter implements CardService {

	private JChannel channel;
	private Map<UID, UserData> user_data;
	private Map<UID, Queue<Operation>> pending_operations;

	public Cache(String cluster_name, String service_name) throws Exception {
		this.channel = new JChannel();
		this.channel.setReceiver(this);
		this.channel.connect(cluster_name);
		this.user_data = new HashMap<UID, UserData>();
		this.pending_operations = new HashMap<UID, Queue<Operation>>();
	}

	public static void main(String[] args) throws Exception {
		Cache cl = new Cache("cacota", args[0]);
		System.out.println(cl.channel.getView().getMembers().get(0)
				+ " is the leader.");
		while (true) {
			if (!cl.isLeader()) {
				System.out.println(cl.getCardBalance(new UID((short) 100)));
				break;
			}
		}
	}

	public void receive(Message msg) {
		if (!msg.getSrc().equals(channel.getAddress())) {
			if (msg.getObject() instanceof UID) {
				processInformationRequestAsLeader((UID) msg.getObject());
			} else if (msg.getObject() instanceof UserData) {
				UserData new_data = (UserData) msg.getObject();
				user_data.put(new_data.userId(), new_data);
			}
		}
	}

	@Override
	public double getCardBalance(UID id) throws RemoteException {
		System.out.println(id.toString());
		UserData data = user_data.get(id);
		if (data != null) {
			return data.balance();
		} else {
			if (iAmTheLeader()) {
				processInformationRequestAsLeader(id);
			} else {
				addPendingOperation(id, new Operation(Type.GET));
				askLeaderUserData(id);
				while (user_data.get(id) == null
						|| !user_data.get(id).isUpdated()) {
					System.out.println("waiting for response");
				}
				return user_data.get(id).balance();
			}
		}
		return 0;
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

	// PRIVATE METHODS

	private void askLeaderUserData(UID id) {
		try {
			System.out.println("Asking leader for information...");
			channel.send(new Message(getLeader(), id));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean isLeader() {
		return channel.getView().getMembers().get(0)
				.equals(channel.getAddress());
	}

	private boolean iAmTheLeader() {
		return channel.getAddress().equals(getLeader());
	}

	private Address getLeader() {
		return channel.getView().getMembers().get(0);
	}

	private void addPendingOperation(UID id, Operation operation) {
		if (pending_operations.containsKey(id)) {
			pending_operations.get(id).add(operation);
		} else {
			LinkedList<Operation> queue = new LinkedList<Operation>(); // TODO:
																		// Verificar
																		// que
																		// esto
																		// sea
																		// ordenado.
																		// CLAVE
			queue.push(operation);
			pending_operations.put(id, queue);
		}
	}

	private void processInformationRequestAsLeader(UID id) {
		System.out.print("Processing request...");
		UserData data = user_data.get(id);
		if (data != null) {
			sendDataToEveryone(data);
		} else {
			UserData new_data = new UserData(id, TestRepo.getInstance()
					.getBalance(id));
			user_data.put(id, new_data);
			sendDataToEveryone(new_data);
		}
		System.out.println("OK");
	}

	private void sendDataToEveryone(Object o) {
		try {
			channel.send(new Message(null, null, o));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

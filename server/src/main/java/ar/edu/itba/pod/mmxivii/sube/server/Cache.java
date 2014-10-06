package ar.edu.itba.pod.mmxivii.sube.server;

import java.rmi.RemoteException;
import java.rmi.server.UID;
import java.util.HashMap;
import java.util.Map;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;

import ar.edu.itba.pod.mmxivii.sube.common.CardService;

public class Cache extends ReceiverAdapter implements CardService {

	private JChannel channel;
	private String service_name;
	private Map<UID, UserData> user_data; 

	public Cache(String cluster_name, String service_name) throws Exception {
		this.channel = new JChannel();
		this.channel.setReceiver(this);
		this.channel.connect(cluster_name);
		this.service_name = service_name;
		this.user_data = new HashMap<UID, UserData>();
	}

	public static void main(String[] args) throws Exception {
		Cache cl = new Cache("cacota", args[0]);
		System.out.println(cl.channel.getView().getMembers().get(0)
				+ " is the leader.");
		while (true) {
			if (cl.isLeader()) {
				System.out.println(cl.service_name + " is the leader.");
			}
		}
	}

	public void receive(Message msg) {
		if (!msg.getSrc().equals(channel.getAddress())) {
			if (msg.getObject() instanceof UID) {
				UID id = (UID) msg.getObject();
				UserData data = user_data.get(id);
				if (data != null)
					sendDataToEveryone(data);
				else {
					// Hablo con el server, y me contesta el balance de un flaco???
					UserData new_data = new UserData(id, 0); // En lugar de 0 poner
					// lo que devuelve
					// el server
					user_data.put(id, new_data);
					sendDataToEveryone(new_data);
				}
			} else if(msg.getObject() instanceof UserData) {
				UserData new_data = (UserData) msg.getObject();
				user_data.put(new_data.getId(), new_data);
			}
		}
	}

	@Override
	public double getCardBalance(UID id) throws RemoteException {
		UserData data = user_data.get(id);
		if (data != null) {
			return data.getBalance();
		} else {
			if (iAmTheLeader()) {
				// Hablo con el server, y me contesta el balance de un flaco???
				UserData new_data = new UserData(id, 0); // En lugar de 0 poner
															// lo que devuelve
															// el server
				user_data.put(id, new_data);
				sendDataToEveryone(new_data);
			} else {
				
				askLeaderUserData(id);
//				while(en operaciones a resolver no tenga la respueta);
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
				return data.getBalance();
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
				return data.getBalance();
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
	
	private void sendDataToEveryone(Object o) {
		try {
			channel.send(new Message(null, null, o));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

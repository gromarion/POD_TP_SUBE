package ar.edu.itba.pod.mmxivii.sube.server;

import java.rmi.RemoteException;
import java.rmi.server.UID;
import java.util.HashMap;
import java.util.Map;

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

	public void sendMessage(Message msg) throws Exception {
		channel.send(msg);
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
		}
	}

	// PRIVATE METHODS

	private boolean isLeader() {
		return channel.getView().getMembers().get(0)
				.equals(channel.getAddress());
	}

	@Override
	public double getCardBalance(UID id) throws RemoteException {
		UserData data = user_data.get(id);
		if (data != null) {
			return data.getBalance();
		} else {
			// Leader must get info and send it back.
		}
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
			// Leader must get info and send it back.
		}
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
	}
}

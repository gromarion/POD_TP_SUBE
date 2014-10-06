package ar.edu.itba.pod.mmxivii.sube.server;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;

public class ChooseLeader extends ReceiverAdapter {

	private JChannel channel;
	private String service_name;
	private Address leader_address;

	public ChooseLeader(String cluster_name, String service_name)
			throws Exception {
		this.channel = new JChannel();
		this.channel.setReceiver(this);
		this.channel.connect(cluster_name);
		this.service_name = service_name;
	}

	public void sendMessage(Message msg) throws Exception {
		channel.send(msg);
	}

	public static void main(String[] args) throws Exception {
		ChooseLeader cl = new ChooseLeader("cacota", args[0]);
		System.out.println(cl.channel.getView().getMembers().get(0)
				+ " is the leader.");
		while (true) {
			if (cl.isTeamLeaderAndNoOneKnows()) {
				cl.notifyEveryOneWhoTheLeaderIs();
			}
		}
	}

	public void receive(Message msg) {
		if (!msg.getSrc().equals(channel.getAddress())) {
			if (msg.getObject() instanceof Integer) {
				processCode(msg);
			}
		}
	}
	
	public boolean isTeamLeaderAndNoOneKnows() {
		return isLeader() && !channel.getAddress().equals(leader_address);
	}

	public void notifyEveryOneWhoTheLeaderIs() {
		sendLeaderCodeToChannel();
		leader_address = channel.getAddress();
		System.out.println(service_name + " is the leader!");
	}

	// PRIVATE METHODS

	private void processCode(Message msg) {
		int code = (Integer) msg.getObject();
		switch (code) {
		case -1234:
			leader_address = msg.getSrc();
			System.out.println("Set new leader: " + leader_address);
			break;
		default:
			break;
		}
	}

	private void sendLeaderCodeToChannel() {
		try {
			channel.send(new Message(null, channel.getAddress(), -1234));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean isLeader() {
		return channel.getView().getMembers().get(0)
				.equals(channel.getAddress());
	}
}

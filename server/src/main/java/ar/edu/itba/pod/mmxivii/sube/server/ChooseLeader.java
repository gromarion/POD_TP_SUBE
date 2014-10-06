package ar.edu.itba.pod.mmxivii.sube.server;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

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
		System.out.println(cl.channel.getView().getMembers().get(0));
		while (true) {
			if (cl.isLeader()) {
				if (!cl.channel.getAddress().equals(cl.leader_address)) {
					cl.sendLeaderCodeToChannel();
					System.out.println(cl.service_name + " is the leader!");
				}
			}
		}
	}

	public void receive(Message msg) {
		if (msg.getObject() instanceof Integer) {
			processCode(msg);
		}
	}

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

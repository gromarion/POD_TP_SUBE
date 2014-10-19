package ar.edu.itba.pod.mmxivii.jgroups;

import java.util.List;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;

/**
 * <pre>
 * Para detectar cuando algun channel se conecta / desconecta
 * simplemente se "overridea" los metodos onConnected y onDisconnected.
 * 
 * Para enviar un mensaje a otro ClusterNode, simplmente usar el metodo send(ClusterNode node, Message message)
 * </pre>
 */
public class ClusterNode {

	private final JChannel _channel;

	public ClusterNode() {
		_channel = JChannels.newChannel();
	}

	public final ClusterNode setReceiver(Receiver receiver) {
		channel().setReceiver(receiver);
		return this;
	}

	public final ClusterNode setName(String name) {
		channel().setName(name);
		return this;
	}

	public final String name() {
		return channel().getName();
	}

	public final ClusterNode connectTo(String clusterName) {
		JChannels.connect(_channel, clusterName);
		onConnected();
		return this;
	}

	public final JChannel channel() {
		return _channel;
	}

	public final Address address() {
		return channel().getAddress();
	}

	public final ClusterNode disconnect() {
		channel().close();
		onDisconected();
		return this;
	}

	public void onConnected() {
	}

	public void onDisconected() {
	}

	public final ClusterNode sendObject(ClusterNode node, Object object) {
		return sendObject(node.channel().getAddress(), object);
	}

	public final ClusterNode sendObject(Address adress, Object object) {
		return send(new Message(address()).setObject(object));
	}

	public final ClusterNode sendObject(Object object) {
		return send(new Message().setObject(object));
	}

	public final ClusterNode send(Message message) {
		JChannels.send(channel(), message);
		return this;
	}
	
	public List<Address> members() {
		return channel().getView().getMembers();
	}
}

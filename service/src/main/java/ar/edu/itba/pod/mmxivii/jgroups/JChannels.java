package ar.edu.itba.pod.mmxivii.jgroups;

import org.jgroups.JChannel;
import org.jgroups.Message;

public final class JChannels {

	public static JChannel newChannel() {
		try {
			return new JChannel();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static JChannel send(JChannel channel, Message msg) {
		try {
			channel.send(msg);
			return channel;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static JChannel connect(JChannel channel, String name) {
		try {
			channel.connect(name);
			return channel;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private JChannels() {
	}
}

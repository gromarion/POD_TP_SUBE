package ar.edu.itba.pod.mmxivii.sube;

import static ar.edu.itba.pod.mmxivii.sube.common.Utils.CARD_REGISTRY_BIND;
import static ar.edu.itba.pod.mmxivii.sube.common.Utils.CARD_SERVICE_REGISTRY_BIND;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import ar.edu.itba.pod.mmxivii.jgroups.ClusterNode;
import ar.edu.itba.pod.mmxivii.sube.common.BaseMain;
import ar.edu.itba.pod.mmxivii.sube.common.CardRegistry;
import ar.edu.itba.pod.mmxivii.sube.common.CardServiceRegistry;
import ar.edu.itba.pod.mmxivii.sube.common.Utils;
import ar.edu.itba.pod.mmxivii.sube.receiver.CacheNodeReceiver;
import ar.edu.itba.pod.mmxivii.sube.receiver.Synchronizer;
import ar.edu.itba.pod.mmxivii.util.Threads;

public class MainCache extends BaseMain {

	private MainCache(@Nonnull String[] args) throws RemoteException,
			NotBoundException {
		super(args, DEFAULT_CLIENT_OPTIONS);
		getRegistry();
		int nodesCount = 3;
		for (int n = 0; n < nodesCount; n++) {
			final CardRegistry server = Utils.lookupObject(CARD_REGISTRY_BIND);
			final CardServiceRegistry cardServiceRegistry = Utils
					.lookupObject(CARD_SERVICE_REGISTRY_BIND);
			ClusterNode cache_node = new ClusterNode().setName("node_" + n);
			CacheNodeReceiver nodeReceiver = new CacheNodeReceiver(cache_node,
					server, cardServiceRegistry);
			cache_node.setReceiver(nodeReceiver).connectTo("cluster");
			Threads.sleep(5, TimeUnit.SECONDS);
		}
		for (int n = 0; n < nodesCount; n++) {
			final CardRegistry server = Utils.lookupObject(CARD_REGISTRY_BIND);
			ClusterNode sync_node = new ClusterNode().setName("node_" + n);
			Synchronizer s = new Synchronizer(sync_node, server);
			sync_node.setReceiver(s).connectTo("cluster");
			s.vote(true);
			Threads.sleep(5, TimeUnit.SECONDS);
		}
	}

	public static void main(@Nonnull String[] args) throws Exception {
		final MainCache main = new MainCache(args);
		main.run();
	}

	private void run() throws RemoteException {
		System.out.println("Starting Service!");
		final Scanner scan = new Scanner(System.in);
		String line;
		do {
			line = scan.next();
			System.out.println("Service running");
		} while (!"x".equals(line));
		scan.close();
		System.out.println("Service exit.");
		System.exit(0);
	}
}

package ar.edu.itba.pod.mmxivii.sube;

import static ar.edu.itba.pod.mmxivii.sube.common.Utils.CARD_SERVICE_REGISTRY_BIND;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import ar.edu.itba.pod.mmxivii.jgroups.ClusterNode;
import ar.edu.itba.pod.mmxivii.sube.common.BaseMain;
import ar.edu.itba.pod.mmxivii.sube.common.CardServiceRegistry;
import ar.edu.itba.pod.mmxivii.sube.common.Utils;
import ar.edu.itba.pod.mmxivii.sube.receiver.CacheNodeReceiver;
import ar.edu.itba.pod.mmxivii.sube.receiver.SynchronizerReceiver;
import ar.edu.itba.pod.mmxivii.util.Threads;

public class MainCache extends BaseMain {

	public static void main(@Nonnull String[] args) throws Exception {
		final MainCache main = new MainCache(args);
		main.run();
	}

	private MainCache(@Nonnull String[] args) throws RemoteException, NotBoundException {
		super(args, DEFAULT_CLIENT_OPTIONS);
		getRegistry();
		int nodesCount = 3;
		for (int n = 0; n < nodesCount; n++) {
			final CardServiceRegistry cardServiceRegistry = Utils.lookupObject(CARD_SERVICE_REGISTRY_BIND);
			ClusterNode cacheNode = new ClusterNode().setName("cache_" + n);
			CacheNodeReceiver nodeReceiver = new CacheNodeReceiver(cacheNode, cardServiceRegistry);
			cacheNode.setReceiver(nodeReceiver).connectTo("cluster");
			Threads.sleep(3, TimeUnit.SECONDS);
		}
		for (int n = 0; n < nodesCount; n++) {
			ClusterNode syncNode = new ClusterNode().setName("sync_" + n);
			SynchronizerReceiver s = new SynchronizerReceiver(syncNode);
			syncNode.setReceiver(s).connectTo("cluster");
			new Thread(s).start();
			Threads.sleep(3, TimeUnit.SECONDS);
		}
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

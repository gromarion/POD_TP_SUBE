package ar.edu.itba.pod.mmxivii.sube.synchronizer;

import static ar.edu.itba.pod.mmxivii.sube.common.Utils.CARD_REGISTRY_BIND;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import ar.edu.itba.pod.mmxivii.jgroups.ClusterNode;
import ar.edu.itba.pod.mmxivii.sube.common.BaseMain;
import ar.edu.itba.pod.mmxivii.sube.common.CardRegistry;
import ar.edu.itba.pod.mmxivii.sube.common.Utils;
import ar.edu.itba.pod.mmxivii.util.Threads;

public class SyncMain extends BaseMain {

	protected SyncMain(String[] args) throws NotBoundException {
		super(args, DEFAULT_CLIENT_OPTIONS);
		int nodesCount = 3;
		for (int n = 0; n < nodesCount; n++) {
			final CardRegistry server = Utils.lookupObject(CARD_REGISTRY_BIND);
			ClusterNode node = new ClusterNode().setName("node_" + n);
			new Synchronizer(node, server);
			Threads.sleep(5, TimeUnit.SECONDS);
		}
	}

	public static void main(String[] args) throws NotBoundException,
			RemoteException {
		final SyncMain main = new SyncMain(args);
		main.run();
	}

	private void run() throws RemoteException {
		System.out.println("Starting Synchronizers!");
		final Scanner scan = new Scanner(System.in);
		String line;
		do {
			line = scan.next();
			System.out.println("Synchronizers running");
		} while (!"x".equals(line));
		scan.close();
		System.out.println("Synchronizers exit.");
		System.exit(0);
	}
}

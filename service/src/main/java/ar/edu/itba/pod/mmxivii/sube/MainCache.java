package ar.edu.itba.pod.mmxivii.sube;

import ar.edu.itba.pod.mmxivii.jgroups.ClusterNode;
import ar.edu.itba.pod.mmxivii.sube.common.BaseMain;
import ar.edu.itba.pod.mmxivii.sube.common.CardRegistry;
import ar.edu.itba.pod.mmxivii.sube.common.CardServiceRegistry;
import ar.edu.itba.pod.mmxivii.sube.common.Utils;
import ar.edu.itba.pod.mmxivii.sube.receiver.CacheNodeReceiver;
import ar.edu.itba.pod.mmxivii.sube.service.CardServiceImpl;

public class MainCache extends BaseMain {

	private MainCache(@Nonnull String[] args) throws RemoteException, NotBoundException {
		super(args, DEFAULT_CLIENT_OPTIONS);
		getRegistry();
		// Setup one node for now....
		final CardRegistry server = Utils.lookupObject(CARD_REGISTRY_BIND);
		ClusterNode node = new ClusterNode().setName("node_1");
		CacheNodeReceiver nodeReceiver = new CacheNodeReceiver(node, server); 
		node.setReceiver(nodeReceiver).connectTo("cluster");
		CardServiceImpl cardService = new CardServiceImpl(server, nodeReceiver);
		// ???
		CardServiceRegistry cardServiceRegistry = Utils.lookupObject(CARD_SERVICE_REGISTRY_BIND);
		cardServiceRegistry.registerService(cardService);
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
		System.out.println("Service exit.");
		System.exit(0);

	}
}

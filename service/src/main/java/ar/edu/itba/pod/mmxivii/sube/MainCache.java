package ar.edu.itba.pod.mmxivii.sube;

import static ar.edu.itba.pod.mmxivii.sube.common.Utils.CARD_REGISTRY_BIND;
import static ar.edu.itba.pod.mmxivii.sube.common.Utils.CARD_SERVICE_REGISTRY_BIND;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

import javax.annotation.Nonnull;

import ar.edu.itba.pod.mmxivii.sube.common.BaseMain;
import ar.edu.itba.pod.mmxivii.sube.common.CardRegistry;
import ar.edu.itba.pod.mmxivii.sube.common.CardServiceRegistry;
import ar.edu.itba.pod.mmxivii.sube.common.Utils;
import ar.edu.itba.pod.mmxivii.sube.service.CardServiceImpl;

public class MainCache extends BaseMain {
	private final CardServiceRegistry cardServiceRegistry;
	private final CardServiceImpl cardService;

	private MainCache(@Nonnull String[] args) throws RemoteException, NotBoundException {
		super(args, DEFAULT_CLIENT_OPTIONS);
		getRegistry();
		final CardRegistry cardRegistry = Utils.lookupObject(CARD_REGISTRY_BIND);
		cardServiceRegistry = Utils.lookupObject(CARD_SERVICE_REGISTRY_BIND);
		cardService = new CardServiceImpl(cardRegistry);
	}

	public static void main(@Nonnull String[] args) throws Exception {
		final MainCache main = new MainCache(args);
		main.run();
	}

	private void run() throws RemoteException {
		cardServiceRegistry.registerService(cardService);
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

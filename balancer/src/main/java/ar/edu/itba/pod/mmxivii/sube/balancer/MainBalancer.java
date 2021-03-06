package ar.edu.itba.pod.mmxivii.sube.balancer;

import static ar.edu.itba.pod.mmxivii.sube.common.Utils.CARD_CLIENT_BIND;
import static ar.edu.itba.pod.mmxivii.sube.common.Utils.CARD_REGISTRY_BIND;
import static ar.edu.itba.pod.mmxivii.sube.common.Utils.CARD_SERVICE_REGISTRY_BIND;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

import javax.annotation.Nonnull;

import ar.edu.itba.pod.mmxivii.sube.common.BaseMain;
import ar.edu.itba.pod.mmxivii.sube.common.CardRegistry;
import ar.edu.itba.pod.mmxivii.sube.common.Utils;

public class MainBalancer extends BaseMain
{
	private static MainBalancer main = null;

	private MainBalancer(@Nonnull String[] args) throws RemoteException, NotBoundException
	{
		super(args, DEFAULT_CLIENT_OPTIONS);
		getRegistry();
		setDelay();
		final CardRegistry cardRegistry = Utils.lookupObject(CARD_REGISTRY_BIND);
		final CardServiceRegistryImpl cardServiceRegistry = new CardServiceRegistryImpl();
		bindObject(CARD_SERVICE_REGISTRY_BIND, cardServiceRegistry);

		final CardClientImpl cardClient = new CardClientImpl(cardRegistry, cardServiceRegistry);
		bindObject(CARD_CLIENT_BIND, cardClient);
	}

	public static void main(@Nonnull String[] args) throws Exception
	{
		main = new MainBalancer(args);
		main.run();
	}

	private void run()
	{
		System.out.println("Starting Balancer!");
		final Scanner scan = new Scanner(System.in);
		String line;
		do {
			line = scan.next();
			System.out.println("Balancer running");
		} while(!"x".equals(line));
		shutdown();
		scan.close();
		System.out.println("Balancer exit.");
		System.exit(0);

	}

	public static void shutdown()
	{
		main.unbindObject(CARD_SERVICE_REGISTRY_BIND);
		main.unbindObject(CARD_CLIENT_BIND);
	}
}


package ar.edu.itba.pod.mmxivii.sube.client;

import static ar.edu.itba.pod.mmxivii.sube.common.Utils.CARD_CLIENT_BIND;
import static com.google.common.base.Preconditions.checkArgument;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import ar.edu.itba.pod.mmxivii.sube.common.BaseMain;
import ar.edu.itba.pod.mmxivii.sube.common.Card;
import ar.edu.itba.pod.mmxivii.sube.common.CardClient;
import ar.edu.itba.pod.mmxivii.sube.common.Utils;

public class MainClient extends BaseMain {

	public static void main(@Nonnull String[] args) throws Exception {
//		args = new String[] { "-host", "192.168.1.116" };
		final MainClient main = new MainClient(args);
		main.run();
	}

	private CardClient cardClient = null;

	private MainClient(@Nonnull String[] args) throws NotBoundException, RemoteException {
		super(args, DEFAULT_CLIENT_OPTIONS);
		getRegistry();
		cardClient = Utils.lookupObject(CARD_CLIENT_BIND);
		boolean auto = false;
		for (String s : args) {
			if (s.startsWith("_auto")) {
				auto = true;
				break;
			}
		}
		if (auto) {
			heavyLoadtest();
		} else {
			MainManualClient.main(args);
		}
	}

	private void run() throws RemoteException {
		System.out.println("Main.run");
//		rechargeTravelAndRechargeAgainTest();
//		insuficcientCreditTravel();
		heavyLoadtest();
	}

	private void rechargeTravelAndRechargeAgainTest() throws RemoteException {
		String cardName = randomCardId();
		Card card = cardClient.newCard(cardName, "");
		UID cardId = card.getId();
		float actualBalance = 100;
		final float travelCost = 10;
		double rechargeStatus = cardClient.recharge(cardId, "recarga", actualBalance);
		checkArgument(rechargeStatus > 0);
		for (int i = 0; i < 10; i++) {
			double reportedBalance = cardClient.getCardBalance(cardId);
			checkArgument((int) reportedBalance == (int) actualBalance);
			cardClient.travel(cardId, "viaje" + i, travelCost);
			actualBalance -= travelCost;
			Threads.sleep(2, TimeUnit.SECONDS);
		}
		cardClient.recharge(cardId, "recarga", travelCost);
		cardClient.travel(cardId, "travelExtraa", travelCost);
		System.out.println("Test 1 seems OK");
	}

	private void insuficcientCreditTravel() throws RemoteException {
		String cardName = randomCardId();
		Card card = cardClient.newCard(cardName, "");
		UID cardId = card.getId();
		cardClient.recharge(cardId, "recarga", 50);
		Threads.sleep(1, TimeUnit.SECONDS);
		double status = cardClient.travel(cardId, "travel", 51);
		checkArgument(status < 0);
		System.out.println("Test 2 seems OK");
	}

	private void heavyLoadtest() throws RemoteException {
		new Thread(new ManyTravelingMFS(10)).start();
		new Thread(new ManyTravelingMFS(10)).start();
		new Thread(new ManyTravelingMFS(10)).start();
	}

	private String randomCardId() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	private final class ManyTravelingMFS implements Runnable {

		private final int _usersCount;

		public ManyTravelingMFS(int usersCount) {
			_usersCount = usersCount;
		}

		@Override
		public void run() {
			try {
				run_();
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Se pudrio todo!");
			}
		}

		private void run_() throws Exception {
			List<UID> cardIds = new ArrayList<UID>(_usersCount);
			System.out.println("Registering users....");
			for (int i = 0; i < _usersCount; i++) {
				String cardName = randomCardId();
				Card card = cardClient.newCard(cardName, "");
				cardIds.add(card.getId());
				if (i % 20 == 0) {
					System.out.println(Thread.currentThread().getId() + ": " + i / (float) _usersCount);
				}
			}
			System.out.println("Registered " + _usersCount + " new users...");
			System.out.println("Let the fun begin....");
			int updateIndex = 0;
			int operationCount = 0;
			while (operationCount < 5) {
				int operationsCount = 1;
				for (int i = 0; i < operationsCount; i++) {
					UID cardId = cardIds.get(randomInt(0, cardIds.size()));
					String desc = "operation" + updateIndex + "x" + i;
					float amount = 10;
					double status = cardClient.travel(cardId, "Tx" + desc, amount);
					if (status < 0) {
						cardClient.recharge(cardId, "Rx" + desc, amount * randomInt(5, 10));
					}
					Threads.sleep(1, TimeUnit.SECONDS);
				}
				Thread.sleep(500);
			}
		}

		private int randomInt(int min, int max) {
			return (int) (Math.random() * (max - min) + min);
		}
	}
}

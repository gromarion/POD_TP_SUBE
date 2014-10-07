package ar.edu.itba.pod.mmxivii.sube.server;

import java.rmi.server.UID;
import java.util.HashMap;
import java.util.Map;

public class TestRepo {

	private static TestRepo instance = null;
	private Map<UID, UserData> user_data;

	private TestRepo() {
		user_data = new HashMap<UID, UserData>();
	}

	public static TestRepo getInstance() {
		if (instance == null)
			instance = new TestRepo();
		return instance;
	}

	public double getBalance(UID id) {
		delay();
		if (!user_data.containsKey(id))
			user_data.put(id, new UserData(id, 10));
		return user_data.get(id).balance();
	}

	public double travel(UID id, double amount) {
		delay();
		UserData data = user_data.get(id);
		data.substractAmount(amount);
		return data.balance();
	}

	public double recharge(UID id, double amount) {
		delay();
		UserData data = user_data.get(id);
		data.addAmount(amount);
		return data.balance();
	}

	private void delay() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

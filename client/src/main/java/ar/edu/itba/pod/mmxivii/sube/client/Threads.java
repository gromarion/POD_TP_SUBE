package ar.edu.itba.pod.mmxivii.sube.client;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class Threads {

	public static long currentsId() {
		return Thread.currentThread().getId();
	}

	public static Thread start(Runnable runnable) {
		return start(new Thread(runnable));
	}

	public static Thread start(Thread thread) {
		thread.start();
		return thread;
	}

	public static void joinAll(Collection<Thread> threads) {
		for (Thread thread : threads) {
			join(thread);
		}
	}

	public static Thread join(Thread thread) {
		try {
			thread.join();
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
		return thread;
	}

	public static void sleep(long millis, TimeUnit unit) {
		sleep(unit.toMillis(millis));
	}

	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}
}

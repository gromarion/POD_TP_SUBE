package ar.edu.itba.pod.mmxivii.sube;

import ar.edu.itba.pod.mmxivii.sube.server.MainServer;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class MainTest
{

	public static final String[] ARGS = {};

	public MainTest() {}

	@Test
	public void testMain() throws Exception
	{
		new Thread() {
			@Override
			public void run()
			{
				try {
					MainServer.main(ARGS);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}.start();

		Thread.sleep(1000);
		MainServer.shutdown();
	}
}

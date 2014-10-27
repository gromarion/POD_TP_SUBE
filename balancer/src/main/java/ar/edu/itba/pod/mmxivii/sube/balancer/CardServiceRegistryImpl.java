package ar.edu.itba.pod.mmxivii.sube.balancer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

import ar.edu.itba.pod.mmxivii.sube.common.CardService;
import ar.edu.itba.pod.mmxivii.sube.common.CardServiceRegistry;

public class CardServiceRegistryImpl extends UnicastRemoteObject implements CardServiceRegistry {
	
	private static final long serialVersionUID = 2473638728674152366L;
	private final List<CardService> serviceList = Collections.synchronizedList(new ArrayList<CardService>());

	protected CardServiceRegistryImpl() throws RemoteException {
		super();
	}

	@Override
	public void registerService(@Nonnull CardService service) throws RemoteException {
		synchronized (serviceList) {
			serviceList.add(service);
		}
	}

	@Override
	public void unRegisterService(@Nonnull CardService service) throws RemoteException {
		synchronized (serviceList) {
			serviceList.remove(service);
		}
	}

	@Override
	public Collection<CardService> getServices() throws RemoteException {
		return serviceList;
	}

	private AtomicInteger index = new AtomicInteger();

	CardService getCardService() {
		synchronized (serviceList) {
			boolean pingOK;
			CardService service;
			do {
				service = serviceList.get(index.get());
				try {
					pingOK = service.ping();
				} catch (Exception e) {
					pingOK = false;
					serviceList.remove(index);
				}
				index.incrementAndGet();
				index.set(index.get() % serviceList.size());
			} while (!pingOK);
			return service;
		}
	}
}

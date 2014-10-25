package ar.edu.itba.pod.mmxivii.sube.balancer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

import ar.edu.itba.pod.mmxivii.sube.common.CardService;
import ar.edu.itba.pod.mmxivii.sube.common.CardServiceRegistry;

import com.google.common.collect.Iterables;

public class CardServiceRegistryImpl extends UnicastRemoteObject implements CardServiceRegistry {
	private static final long serialVersionUID = 2473638728674152366L;
	private final List<CardService> serviceList = Collections.synchronizedList(new ArrayList<CardService>());
	private final Iterator<CardService> _servicesCycle;

	protected CardServiceRegistryImpl() throws RemoteException {
		_servicesCycle = Iterables.cycle(serviceList).iterator();
	}

	@Override
	public void registerService(@Nonnull CardService service) throws RemoteException {
		serviceList.add(service);
	}

	@Override
	public void unRegisterService(@Nonnull CardService service) throws RemoteException {
		serviceList.remove(service);
	}

	@Override
	public Collection<CardService> getServices() throws RemoteException {
		return serviceList;
	}

	CardService getCardService() {
		return _servicesCycle.next();
	}
}

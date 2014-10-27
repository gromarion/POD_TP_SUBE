package ar.edu.itba.pod.mmxivii.sube.service;

import java.rmi.RemoteException;
import java.rmi.server.UID;
import java.rmi.server.UnicastRemoteObject;

import javax.annotation.Nonnull;

import ar.edu.itba.pod.mmxivii.sube.common.CardService;

public class CardServiceImpl extends UnicastRemoteObject implements CardService {

	private static final long serialVersionUID = 2919260533266908792L;

	private CardService _delegate;

	public CardServiceImpl(CardService delegate) throws RemoteException {
		super(0);
		_delegate = delegate;
	}

	@Override
	public boolean ping() throws RemoteException {
		return _delegate.ping();
	}

	@Override
	public double getCardBalance(@Nonnull UID id) throws RemoteException {
		return _delegate.getCardBalance(id);
	}

	@Override
	public double travel(@Nonnull UID id, @Nonnull String description, double amount) throws RemoteException {
		return _delegate.travel(id, description, amount);
	}

	@Override
	public double recharge(@Nonnull UID id, @Nonnull String description, double amount) throws RemoteException {
		return _delegate.recharge(id, description, amount);
	}
}

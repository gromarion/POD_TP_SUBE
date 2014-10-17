package ar.edu.itba.pod.mmxivii.sube.service;

import java.rmi.RemoteException;
import java.rmi.server.UID;
import java.rmi.server.UnicastRemoteObject;

import javax.annotation.Nonnull;

import ar.edu.itba.pod.mmxivii.sube.common.CardRegistry;
import ar.edu.itba.pod.mmxivii.sube.common.CardService;

public class CardServiceImpl extends UnicastRemoteObject implements CardService {

	private static final long serialVersionUID = 2919260533266908792L;

	@Nonnull
	private final CardRegistry cardRegistry;

	private CardService _delegate;

	public CardServiceImpl(@Nonnull CardRegistry cardRegistry, CardService delegate) throws RemoteException {
		super(0);
		this.cardRegistry = cardRegistry;
		_delegate = delegate;
	}

	@Override
	public double getCardBalance(@Nonnull UID id) throws RemoteException {
//		return cardRegistry.getCardBalance(id);
		return _delegate.getCardBalance(id);
	}

	@Override
	public double travel(@Nonnull UID id, @Nonnull String description, double amount) throws RemoteException {
//		return cardRegistry.addCardOperation(id, description, amount * -1);
		return _delegate.travel(id, description, amount);
	}

	@Override
	public double recharge(@Nonnull UID id, @Nonnull String description, double amount) throws RemoteException {
//		return cardRegistry.addCardOperation(id, description, amount);
		return _delegate.recharge(id, description, amount);
	}
}

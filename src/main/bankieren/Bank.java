package main.bankieren;

import main.centrale.IBankTbvCentrale;
import main.centrale.ICentrale;
import main.util.*;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Bank extends UnicastRemoteObject implements IBankTbvCentrale {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8728841131739353765L;
	private Map<Integer,IRekeningTbvBank> accounts;
	private Collection<IKlant> clients;
	private int nieuwReknr;
	private String name;
        private ICentrale centrale;

	public Bank(String name, ICentrale centrale) throws RemoteException {
		accounts = new HashMap<Integer,IRekeningTbvBank>();
		clients = new ArrayList<IKlant>();
		nieuwReknr = 100000000;	
                this.centrale = centrale;
		this.name = name;
                
                centrale.addBank(this);
	}

        @Override
	public synchronized int openRekening(String name, String city) {
		if (name.equals("") || city.equals(""))
			return -1;

		IKlant klant = getKlant(name, city);
		IRekeningTbvBank account = new Rekening(nieuwReknr, klant, Geld.EURO);
		accounts.put(nieuwReknr,account);
		nieuwReknr++;
		return nieuwReknr-1;
	}

	private IKlant getKlant(String name, String city) {
		for (IKlant k : clients) {
			if (k.getNaam().equals(name) && k.getPlaats().equals(city))
				return k;
		}
		IKlant klant = new Klant(name, city);
		clients.add(klant);
		return klant;
	}

        @Override
	public IRekening getRekening(int nr) {
		return this.accounts.get(nr);
	}

        @Override
	public synchronized boolean maakOver(String name, int source, int destination, Geld money)
			throws NumberDoesntExistException, RemoteException {
                System.out.println("[outgoing] Transferring " + money.getValue() + " from " + String.valueOf(source) + " to " + String.valueOf(destination) + " at " + name);
                
                System.out.println("Known accounts: ");
                for(Integer key : accounts.keySet()) {
                    System.out.println(String.valueOf(key));
                }
		if (source == destination)
			throw new RuntimeException(
					"cannot transfer money to your own account");
		if (!money.isPositive())
			throw new RuntimeException("money must be positive");

		IRekeningTbvBank source_account = (IRekeningTbvBank) getRekening(source);
		if (source_account == null)
			throw new NumberDoesntExistException("Source account " + source
					+ " unknown at " + name);

		Geld negative = Geld.difference(new Geld(0, money.getCurrency()),
				money);
		boolean success = source_account.muteer(negative);
		if (!success)
			return false;
                
                success = centrale.transferToBank(name, destination, money);
                
		if (!success) // rollback
			source_account.muteer(money);
		return success;
	}

	@Override
	public String getName() {
		return name;
	}

    @Override
    public boolean transferToRekening(int destination, Geld money) throws RemoteException, NumberDoesntExistException {
        System.out.println("[incoming] Transferring " + money.getValue() + " to " + String.valueOf(destination) + " at " + name);
                
        System.out.println("Known accounts: ");
        for(Integer key : accounts.keySet()) {
            System.out.println(String.valueOf(key));
        }
        if (!money.isPositive())
                throw new RuntimeException("money must be positive");

        IRekeningTbvBank dest_account = (IRekeningTbvBank) getRekening(destination);
        if (dest_account == null) 
                throw new NumberDoesntExistException("Destination account " + destination
                                + " unknown at " + name);
        return dest_account.muteer(money);
    }

}

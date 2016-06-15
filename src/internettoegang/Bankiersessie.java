package internettoegang;

import bankieren.IBank;
import bankieren.IRekening;
import bankieren.Geld;
import observer.IRemotePropertyListener;
import util.InvalidSessionException;
import util.NumberDoesntExistException;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Bankiersessie extends UnicastRemoteObject implements
		IBankiersessie {

    private static final long serialVersionUID = 1L;
    private long laatsteAanroep;
    private int reknr;
    private IBank bank;
    private IRemotePropertyListener listener;

    public Bankiersessie(int reknr, IBank bank) throws RemoteException {
            laatsteAanroep = System.currentTimeMillis();
            this.reknr = reknr;
            this.bank = bank;

    }

    public boolean isGeldig() {
        // Check whether the inactivity timer has expired and if the listener
        // is not null.
        if (System.currentTimeMillis() - laatsteAanroep > GELDIGHEIDSDUUR
                && listener != null) {
            try {
                this.removeListener(listener);
            } catch (RemoteException ex) {
            }
        }
        return System.currentTimeMillis() - laatsteAanroep < GELDIGHEIDSDUUR;
    }

    @Override
    public boolean maakOver(String banknaam, int bestemming, Geld bedrag)
                    throws NumberDoesntExistException, InvalidSessionException,
                    RemoteException {

            updateLaatsteAanroep();

            if (reknr == bestemming)
                    throw new RuntimeException(
                                    "source and destination must be different - Trying to transfer from " + String.valueOf(reknr) + " to " + String.valueOf(bestemming));
            if (!bedrag.isPositive())
                    throw new RuntimeException("amount must be positive");

            return bank.maakOver(banknaam, reknr, bestemming, bedrag);
    }

    private void updateLaatsteAanroep() throws InvalidSessionException {
            if (!isGeldig()) {
                    throw new InvalidSessionException("session has been expired");
            }

            laatsteAanroep = System.currentTimeMillis();
    }

    @Override
    public IRekening getRekening() throws InvalidSessionException,
                    RemoteException {

            updateLaatsteAanroep();

            return bank.getRekening(reknr);
    }

    @Override
    public void logUit() throws RemoteException {
        if (listener != null) {
            this.removeListener(listener);
        }
        UnicastRemoteObject.unexportObject(this, true);
    }

    @Override
    public void addListener(IRemotePropertyListener listener) throws RemoteException {
        this.listener = listener;
        IRekening rekening = bank.getRekening(reknr);// Get the bankaccount belonging to the session user
        rekening.addListener(listener, "saldo");
    }

    @Override
    public void removeListener(IRemotePropertyListener listener) throws RemoteException {
        IRekening rekening = bank.getRekening(reknr); // Get the bankaccount belonging to the session user
        rekening.removeListener(listener, "saldo");
    }

}

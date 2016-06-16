package internettoegang;

import bankieren.IRekening;
import bankieren.Geld;
import observer.IRemotePropertyListener;
import util.InvalidSessionException;
import util.NumberDoesntExistException;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IBankiersessie extends Remote {

	// Set this variable to 1000 when testing
	// back to 600000 when done
	long GELDIGHEIDSDUUR = 1000;
	/**
	 * @returns true als de laatste aanroep van getRekening of maakOver voor deze
	 *          sessie minder dan GELDIGHEIDSDUUR geleden is
	 *          en er geen communicatiestoornis in de tussentijd is opgetreden, 
	 *          anders false
	 */
	boolean isGeldig() throws RemoteException; 

	/**
	 * er wordt bedrag overgemaakt van de bankrekening met het nummer bron naar
	 * de bankrekening met nummer bestemming
	 * 
	 * @param banknaam
         *          van de bestemming bank
	 * @param bestemming
	 *            is ongelijk aan rekeningnummer van deze bankiersessie
	 * @param bedrag
	 *            is groter dan 0
	 * @return <b>true</b> als de overmaking is gelukt, anders <b>false</b>
	 * @throws NumberDoesntExistException
	 *             als bestemming onbekend is
	 * @throws InvalidSessionException
	 *             als sessie niet meer geldig is 
         * 
	 * @throws RemoteException
	 */
	boolean maakOver(String banknaam, int bestemming, Geld bedrag)
			throws NumberDoesntExistException, InvalidSessionException,
			RemoteException;

	/**
	 * sessie wordt beeindigd
	 */
	void logUit() throws RemoteException;

	/**
	 * @return de rekeninggegevens die horen bij deze sessie
	 * @throws InvalidSessionException
	 *             als de sessieId niet geldig of verlopen is
	 * @throws RemoteException
	 */
	IRekening getRekening() throws InvalidSessionException, RemoteException;
        
        /**
        * Method that adds a given RemotePropertyListener to the listener and adds
        * it to the bank account that belongs to the session user.
        *
        * @param listener RemotePropertyListener belonging to the session user.
        * @throws RemoteException
        */
       void addListener(IRemotePropertyListener listener)
               throws RemoteException;

       /**
        * Method that removes a given RemotePropertyListener from the a bank
        * account that belongs to the session user.
        *
        * @param listener RemotePropertyListener belonging to the session user.
        * @throws RemoteException
        */
       void removeListener(IRemotePropertyListener listener)
               throws RemoteException;
}

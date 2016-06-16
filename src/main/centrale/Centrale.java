/*
 */
package centrale;

import bankieren.Geld;
import util.NumberDoesntExistException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author Edwin
 *         Created on 6/16/2016
 */
public class Centrale implements ICentrale {

    private ArrayList<IBankTbvCentrale> banks;
    private Registry registry;



    public Centrale() throws IOException {
        try (FileOutputStream out = new FileOutputStream(Constants.PROPERTIES_FILENAME)) {
            Properties props = new Properties();
            props.setProperty(Constants.KEY_RMI_BINDNAME, Constants.RMI_BINDNAME);
            props.setProperty(Constants.KEY_IP, Constants.IP);
            props.setProperty(Constants.KEY_PORT, String.valueOf(Constants.PORT));
            props.store(out, null);


            registry = LocateRegistry.createRegistry(Constants.PORT);
            UnicastRemoteObject.exportObject(this, Constants.PORT);
            registry.rebind(Constants.KEY_RMI_BINDNAME, this);
        } catch (RemoteException ex) {
            Logger.getLogger(Centrale.class.getName()).log(Level.SEVERE, null, ex);
        }
        banks = new ArrayList();
    }

    @Override
    public boolean addBank(IBankTbvCentrale bank) throws RemoteException {
        for (IBankTbvCentrale tempBank : banks) {
            if (tempBank.equals(bank)) return false;
        }

        banks.add(bank);
        return true;
    }

    @Override
    public boolean removeBank(String naam) throws RemoteException {
        IBankTbvCentrale toRemove = null;
        for (IBankTbvCentrale tempBank : banks) {
            if (tempBank
                    .getName()
                    .toUpperCase()
                    .trim()
                    .equals(
                            naam
                                    .toUpperCase()
                                    .trim()
                    ))
                toRemove = tempBank;
        }

        if (toRemove != null) {
            banks.remove(toRemove);
            return true;
        }
        return false;
    }

    @Override
    public boolean transferToBank(String bank, int ontvanger, Geld amount) throws RemoteException, NumberDoesntExistException {
        System.out.println("[Centrale] Transferring " + amount.getValue() + " to " + String.valueOf(ontvanger) + " at " + bank);
        IBankTbvCentrale foundBank = null;
        for (IBankTbvCentrale tempBank : banks) {
            if (tempBank
                    .getName()
                    .toUpperCase()
                    .trim()
                    .equals(
                            bank
                                    .toUpperCase()
                                    .trim()
                    )
                    ) foundBank = tempBank;
        }

        if (foundBank == null) {
            throw new RuntimeException("Bank " + bank + " does not exist");
        }

        return foundBank.transferToRekening(ontvanger, amount);
    }

}

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


/**
 * @author Edwin
 *         Created on 6/16/2016
 */
public class Centrale implements ICentrale {

    private ArrayList<IBankInCentrale> banks;

    public Centrale() throws IOException {
        savePropertiesFile();
        bindRegistry();

        banks = new ArrayList<>();
    }

    private void bindRegistry() throws RemoteException {
        Registry registry = LocateRegistry.createRegistry(Constants.PORT);
        UnicastRemoteObject.exportObject(this, Constants.PORT);
        registry.rebind(Constants.KEY_RMI_BINDNAME, this);
    }

    private void savePropertiesFile() throws IOException {
        try (FileOutputStream out = new FileOutputStream(Constants.PROPERTIES_FILENAME)) {
            Properties props = new Properties();
            props.setProperty(Constants.KEY_RMI_BINDNAME, Constants.RMI_BINDNAME);
            props.setProperty(Constants.KEY_IP, Constants.IP);
            props.setProperty(Constants.KEY_PORT, String.valueOf(Constants.PORT));
            props.store(out, null);
        }
    }

    @Override
    public boolean addBank(IBankInCentrale bank) throws RemoteException {
        for (IBankInCentrale tempBank : banks) {
            if (tempBank.equals(bank)) {
                return false;
            }
        }
        banks.add(bank);
        return true;
    }

    @Override
    public boolean removeBank(String bankNaam) throws RemoteException {

        for (IBankInCentrale tempBank : banks) {
            if (tempBank.getName().toUpperCase().equals(bankNaam.toUpperCase())){
                banks.remove(tempBank);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean transferToBank(String bank, int ontvanger, Geld bedrag) throws RemoteException, NumberDoesntExistException {
        System.out.println("[Centrale] Transferring " + bedrag.getValue() + " to " + String.valueOf(ontvanger) + " at " + bank);
        IBankInCentrale foundBank = null;
        for (IBankInCentrale tempBank : banks) {
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

        return foundBank.transferToRekening(ontvanger, bedrag);
    }

}

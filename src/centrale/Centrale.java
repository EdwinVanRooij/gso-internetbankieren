/*
 */
package centrale;

import bankieren.Geld;
import util.NumberDoesntExistException;

import java.io.FileNotFoundException;
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
 *
 * @author cas
 */
public class Centrale implements ICentrale {

    private ArrayList<IBankTbvCentrale> banks;
    private Registry registry;
    private int port = 1234;
    
    
    public Centrale() throws FileNotFoundException, IOException {
        FileOutputStream out = null;
        try {
                String address = java.net.InetAddress.getLocalHost().getHostAddress();
                Properties props = new Properties();
                String rmiCentrale = address + ":" + port + "/centrale";
                props.setProperty("centrale", rmiCentrale);
                props.setProperty("ip", address);
                props.setProperty("port", String.valueOf(port));
                out = new FileOutputStream("centrale.props");
                props.store(out, null);
                out.close();
            
            
            registry = LocateRegistry.createRegistry(port);
            UnicastRemoteObject.exportObject(this, port);
            registry.rebind("centrale", this);
        } catch (RemoteException ex) {
            Logger.getLogger(Centrale.class.getName()).log(Level.SEVERE, null, ex);
        }
        banks = new ArrayList();
    }
    
    @Override
    public boolean addBank(IBankTbvCentrale bank) throws RemoteException {
        for(IBankTbvCentrale tempBank : banks) {
            if(tempBank.equals(bank)) return false;
        }
        
        banks.add(bank);
        return true;
    }

    @Override
    public boolean removeBank(String naam) throws RemoteException {
        IBankTbvCentrale toRemove = null;
        for(IBankTbvCentrale tempBank : banks) {
            if(tempBank
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
        
        if(toRemove != null) {
            banks.remove(toRemove);
            return true;
        }
        return false;
    }

    @Override
    public boolean transferToBank(String bank, int ontvanger, Geld amount) throws RemoteException, NumberDoesntExistException {
        System.out.println("[Centrale] Transferring " + amount.getValue() + " to " + String.valueOf(ontvanger) + " at " + bank);
        IBankTbvCentrale foundBank = null;
        for(IBankTbvCentrale tempBank : banks) {
            if(tempBank
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
        
        if(foundBank == null) {
            throw new RuntimeException("Bank " + bank + " does not exist");
        }
        
        return foundBank.transferToRekening(ontvanger, amount);
    }
    
}

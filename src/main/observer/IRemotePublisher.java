package observer;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Edwin
 *         Created on 6/16/2016
 */
public interface IRemotePublisher extends Remote {
    
    /**
     * Subscribe a listener to a property
     * @param listener Object implementing IRemotePropertyListener that wants to subscribe to a property
     * @param property Property that listener wants to subscribe to
     * @throws RemoteException 
     */
    void addListener(IRemotePropertyListener listener, String property) throws RemoteException;
    
    /**
     * Unsubscribe a listener from a property
     * @param listener Object implementing IRemotePropertyListener that wants to unsubscribe from a property
     * @param property Property that the listener wants to unsubscribe from
     * @throws RemoteException 
     */
    void removeListener(IRemotePropertyListener listener, String property) throws RemoteException;
}

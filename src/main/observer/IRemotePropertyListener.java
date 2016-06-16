package main.observer;

import java.beans.PropertyChangeEvent;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.EventListener;

/**
 * @author Edwin
 *         Created on 6/16/2016
 */
public interface IRemotePropertyListener extends EventListener, Remote {
    
    /**
     * Notify all listeners subscribed to a property about a change
     * @param evt property change details
     * @throws RemoteException 
     */
    void propertyChange(PropertyChangeEvent evt) throws RemoteException;
}

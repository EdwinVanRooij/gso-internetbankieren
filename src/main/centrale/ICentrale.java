/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package centrale;

import bankieren.Geld;
import util.NumberDoesntExistException;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Edwin
 *         Created on 6/16/2016
 */
public interface ICentrale extends Remote {
    
    /**
     * registratie van een nieuwe bank
     * 
     * @param bank 
     *            de nieuwe bank
     * @return true wanneer het gelukt is en false wanneer er een fout is opgetreden
     * @throws RemoteException
     *             als er iets mis is met de verbinding, of er niet wordt gereageerd op de methodeaanroep
     */
    boolean addBank(IBankInCentrale bank) throws RemoteException;
    
    /**
     * verwijderen van een bank
     * 
     * @param bankNaam de bank die je wilt verwijderen
     *
     * @return true wanneer het gelukt is en false wanneer er een fout is opgetreden
     * @throws RemoteException bij een communicatiefout
     */
    boolean removeBank(String bankNaam) throws RemoteException;
    
    /**
     * overboeking van geld naar een rekening van een andere bank via de centrale
     * 
     * @param bank de bank waar het geld naartoe gaat
     * @param ontvanger Rekeningnummer van de ontvanger
     * @param bedrag hoeveelheid geld die wordt overgemaakt
     * @return true wanneer het gelukt is en false wanneer er een fout is opgetreden
     * @throws RemoteException bij een communicatiefout
     * @throws NumberDoesntExistException incorrect bedrag
     */
    boolean transferToBank(String bank, int ontvanger, Geld bedrag) throws RemoteException, NumberDoesntExistException;
}

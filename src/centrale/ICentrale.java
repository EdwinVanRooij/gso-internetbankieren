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
public interface ICentrale extends Remote{
    
    /**
     * registratie van een nieuwe bank
     * 
     * @param bank 
     *            de nieuwe bank
     * @return true wanneer het gelukt is en false wanneer er een fout is opgetreden
     * @throws RemoteException
     *             als er iets mis is met de verbinding, of er niet wordt gereageerd op de methodeaanroep
     */
    boolean addBank(IBankTbvCentrale bank) throws RemoteException;
    
    /**
     * verwijderen van een bank
     * 
     * @param naam 
     *            van de bank
     * @return true wanneer het gelukt is en false wanneer er een fout is opgetreden
     * @throws RemoteException
     *             als er iets mis is met de verbinding, of er niet wordt gereageerd op de methodeaanroep
     */
    boolean removeBank(String naam) throws RemoteException;
    
    /**
     * overboeking van geld naar een rekening van een andere bank via de centrale
     * 
     * @param bank 
     *            waarbij de rekening bekend is
     * @param ontvanger
     *            Rekeningnummer van de ontvangende partij
     * @param amount 
     *            hoeveelheid geld die wordt overgemaakt
     * @return true wanneer het gelukt is en false wanneer er een fout is opgetreden
     * @throws RemoteException
     *             als er iets mis is met de verbinding, of er niet wordt gereageerd op de methodeaanroep
     * @throws NumberDoesntExistException
     *             wanneer het meegegeven bedrag negatief is
     */
    boolean transferToBank(String bank, int ontvanger, Geld amount) throws RemoteException, NumberDoesntExistException;
}

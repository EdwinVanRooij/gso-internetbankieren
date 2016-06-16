/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.centrale;

import main.bankieren.IBank;
import main.bankieren.Geld;
import main.util.NumberDoesntExistException;

import java.rmi.RemoteException;

/**
 * @author Edwin
 *         Created on 6/16/2016
 */
public interface IBankTbvCentrale extends IBank {
    
    /**
     * overboeking van geld naar een rekening van een andere bank via de centrale
     * 
     * @param verzender 
     *            van de transactie
     * @param ontvanger 
     *            waarnaar het geld wordt verstuurd
     * @param amount 
     *            hoeveelheid geld die wordt overgemaakt
     * @return true wanneer het gelukt is en false wanneer er een fout is opgetreden
     * @throws RemoteException
     *             als er iets mis is met de verbinding, of er niet wordt gereageerd op de methodeaanroep
     */
    boolean transferToRekening(int ontvanger, Geld amount) throws RemoteException, NumberDoesntExistException;
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.gui;

import main.bankieren.IRekening;
import main.bankieren.Geld;
import main.internettoegang.IBalie;
import main.internettoegang.IBankiersessie;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import main.observer.IRemotePropertyListener;
import main.util.InvalidSessionException;
import main.util.NumberDoesntExistException;

import java.beans.PropertyChangeEvent;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FXML Controller class
 *
 * @author frankcoenen
 */
public class BankierSessieController implements Initializable, IRemotePropertyListener {

    @FXML
    private Hyperlink hlLogout;

    @FXML
    private TextField tfNameCity;
    @FXML
    private TextField tfAccountNr;
    @FXML
    private TextField tfBalance;
    @FXML
    private TextField tfAmount;
    @FXML
    private TextField tfToAccountNr;
    @FXML
    private Button btTransfer;
    @FXML
    private TextArea taMessage;
    @FXML
    private TextField tfBank;

    private BankierClient application;
    private IBalie balie;
    private IBankiersessie sessie;

    public void setApp(BankierClient application, IBalie balie, IBankiersessie sessie) {
        this.balie = balie;
        this.sessie = sessie;
        this.application = application;
        IRekening rekening = null;
        try {
            rekening = sessie.getRekening();
            tfAccountNr.setText(rekening.getNr() + "");
            tfBalance.setText(rekening.getSaldo() + "");
            String eigenaar = rekening.getEigenaar().getNaam() + " te "
                    + rekening.getEigenaar().getPlaats();
            tfNameCity.setText(eigenaar);
            UnicastRemoteObject.exportObject(this, 0);
            sessie.addListener(this);
        } catch (InvalidSessionException ex) {
            taMessage.setText("bankiersessie is verlopen");
            Logger.getLogger(BankierSessieController.class.getName()).log(Level.SEVERE, null, ex);

        } catch (RemoteException ex) {
            taMessage.setText("verbinding verbroken");
            Logger.getLogger(BankierSessieController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    private void logout(ActionEvent event) {
        try {
            sessie.logUit();
            UnicastRemoteObject.unexportObject(this, true);
            application.gotoLogin(balie, "");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void transfer(ActionEvent event) {
        try {
            int from = Integer.parseInt(tfAccountNr.getText());
            int to = Integer.parseInt(tfToAccountNr.getText());
            String bank = tfBank.getText();
            if (from == to) {
                taMessage.setText("can't transfer money to your own account");
            }
            long centen = (long) (Double.parseDouble(tfAmount.getText()) * 100);
            sessie.maakOver(bank, to, new Geld(centen, Geld.EURO));
        } catch (RemoteException e1) {
            e1.printStackTrace();
            taMessage.setText("verbinding verbroken");
        } catch (NumberDoesntExistException | InvalidSessionException e1) {
            e1.printStackTrace();
            taMessage.setText(e1.getMessage());
        }
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) throws RemoteException {
        Task task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> {
                    // Update the current balance with the new value
                    Geld updatedBalance = (Geld) evt.getNewValue();
                    System.out.println("New balance: " + updatedBalance.getValue());
                    tfBalance.setText(updatedBalance.getValue() + "");
                });
                return null;
            }
        };
        (new Thread(task)).start();
    }
}

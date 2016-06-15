/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import bankieren.Bank;
import centrale.ICentrale;
import gui.BankierClient;
import internettoegang.Balie;
import internettoegang.IBalie;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author frankcoenen
 */
public class BalieServer extends Application {

    private Stage stage;
    private final double MINIMUM_WINDOW_WIDTH = 600.0;
    private final double MINIMUM_WINDOW_HEIGHT = 200.0;
    private String nameBank;

    @Override
    public void start(Stage primaryStage) throws IOException {

        try {
            stage = primaryStage;
            stage.setTitle("Bankieren");
            stage.setMinWidth(MINIMUM_WINDOW_WIDTH);
            stage.setMinHeight(MINIMUM_WINDOW_HEIGHT);
            gotoBankSelect();

            primaryStage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    protected String connectToCentraleString() {
        try {
            FileInputStream in = new FileInputStream("centrale.props");
            Properties props = new Properties();
            props.load(in);
            String rmiCentrale = props.getProperty("centrale");
            in.close();

            return "rmi://" + rmiCentrale;

            } catch (Exception exc) {
                exc.printStackTrace();
                return null;
            }
    }

    public boolean startBalie(String nameBank) throws NotBoundException {
            FileOutputStream out = null;
            try {
                this.nameBank = nameBank;
                String address = java.net.InetAddress.getLocalHost().getHostAddress();
                int port = 1233;
                Properties props = new Properties();
                String rmiBalie = address + ":" + port + "/" + nameBank;
                props.setProperty("balie", rmiBalie);
                out = new FileOutputStream(nameBank + ".props");
                props.store(out, null);
                out.close();
                
                FileInputStream in = new FileInputStream("centrale.props");
                props = new Properties();
                props.load(in);
                String addressCentrale = props.getProperty("ip");
                String portCentrale = props.getProperty("port");
                in.close();
                
                Registry registry = LocateRegistry.getRegistry(addressCentrale, Integer.parseInt(portCentrale));
                ICentrale centrale = (ICentrale) registry.lookup("centrale");
                registry = LocateRegistry.createRegistry(port);
                
                IBalie balie = new Balie(new Bank(nameBank, centrale));
                registry.rebind(nameBank, balie);
                //Naming.rebind(nameBank, balie);
               
                return true;

            } catch (IOException ex) {
                Logger.getLogger(BalieServer.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    out.close();
                } catch (IOException ex) {
                    Logger.getLogger(BalieServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return false;
    }

    public void gotoBankSelect() {
        try {
            server.BalieController bankSelect = (server.BalieController) replaceSceneContent("Balie.fxml");
            bankSelect.setApp(this);
        } catch (Exception ex) {
            Logger.getLogger(BankierClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Initializable replaceSceneContent(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        InputStream in = BalieServer.class.getResourceAsStream(fxml);
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(BalieServer.class.getResource(fxml));
        AnchorPane page;
        try {
            page = (AnchorPane) loader.load(in);
        } finally {
            in.close();
        }
        Scene scene = new Scene(page, 800, 600);
        stage.setScene(scene);
        stage.sizeToScene();
        return (Initializable) loader.getController();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}

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
            String rmiCentrale = props.getProperty("main/centrale");
            in.close();

            return "rmi://" + rmiCentrale;

        } catch (Exception exc) {
            exc.printStackTrace();
            return null;
        }
    }


    public boolean startBalie(String nameBank) throws NotBoundException {
        /**
         * Initialize variables
         */
        Properties props = new Properties();
        this.nameBank = nameBank;

        /**
         * Save properties file
         */
        try (FileOutputStream out = new FileOutputStream(this.nameBank + ".props")) {

            String propertiesFileName = String.format("%s:%s/%s", String.valueOf(Constants.IP), String.valueOf(Constants.PORT), this.nameBank);
            props.setProperty(Constants.KEY_BALIE, propertiesFileName);
            props.store(out, null);

            try (FileInputStream in = new FileInputStream(Constants.PROPERTIES_FILENAME_CENTRALE)) {
                props = new Properties();
                props.load(in);
                String addressCentrale = props.getProperty(Constants.KEY_IP);
                String portCentrale = props.getProperty(Constants.KEY_PORT);


                Registry registry = LocateRegistry.getRegistry(addressCentrale, Integer.parseInt(portCentrale));
                ICentrale centrale = (ICentrale) registry.lookup(Constants.RMI_CENTRALE_BINDNAME);
                registry = LocateRegistry.createRegistry(Constants.PORT);

                IBalie balie = new Balie(new Bank(nameBank, centrale));
                registry.rebind(nameBank, balie);
            }

            return true;

        } catch (IOException ex) {
            ex.printStackTrace();
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

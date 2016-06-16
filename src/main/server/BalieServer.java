/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import bankieren.Bank;
import bankieren.IBank;
import centrale.ICentrale;
import gui.BankierClient;
import internettoegang.Balie;
import internettoegang.IBalie;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
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

    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            stage = primaryStage;
            stage.setTitle(Constants.WINDOW_TITLE);
            stage.setMinWidth(Constants.MINIMUM_WINDOW_WIDTH);
            stage.setMinHeight(Constants.MINIMUM_WINDOW_HEIGHT);
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


    boolean startBalie(String nameBank) throws NotBoundException {
        /**
         * Save properties file
         */
        try (FileOutputStream out = new FileOutputStream(nameBank + ".props")) {
            Properties props = new Properties();
            String propertiesFileName = String.format("%s:%s/%s", String.valueOf(Constants.IP), String.valueOf(Constants.PORT), nameBank);
            props.setProperty(Constants.KEY_BALIE, propertiesFileName);
            props.store(out, null);

            /**
             * Get the centrale from registry
             */
            ICentrale centrale = getCentrale();

            /**
             * Bind balie to a registry, balie needs a centrale
             */
            Registry balieRegistry = LocateRegistry.createRegistry(Constants.PORT);
            IBank rabobank = new Bank(nameBank, centrale);
            IBalie balie = new Balie(rabobank);
            balieRegistry.rebind(nameBank, balie);

            return true;

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private ICentrale getCentrale() throws IOException, NotBoundException {
        try (FileInputStream in = new FileInputStream(Constants.PROPERTIES_FILENAME_CENTRALE)) {
            Properties props = new Properties();
            props.load(in);
            String addressCentrale = props.getProperty(Constants.KEY_IP);
            String portCentrale = props.getProperty(Constants.KEY_PORT);

            Registry centraleRegistry = LocateRegistry.getRegistry(addressCentrale, Integer.parseInt(portCentrale));
            return (ICentrale) centraleRegistry.lookup(Constants.RMI_CENTRALE_BINDNAME);
        }
    }


    private void gotoBankSelect() {
        try {
            server.BalieController bankSelect = (server.BalieController) replaceSceneContent(Constants.KEY_BALIE_FXML);
            bankSelect.setApp(this);
        } catch (Exception ex) {
            Logger.getLogger(BankierClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Initializable replaceSceneContent(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        try (InputStream in = BalieServer.class.getResourceAsStream(fxml)) {
            loader.setBuilderFactory(new JavaFXBuilderFactory());
            loader.setLocation(BalieServer.class.getResource(fxml));

            Scene scene = new Scene(loader.load(in), 800, 600);
            stage.setScene(scene);
            stage.sizeToScene();
            return (Initializable) loader.getController();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}

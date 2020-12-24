package com.victorouy.application;

import com.victorouy.business.MailSendingReceiving;
import com.victorouy.controller.ConfigPropertiesFXMLController;
import com.victorouy.controller.RootEmailFXMLController;
import com.victorouy.exceptions.AbsentEmailAddressException;
import com.victorouy.exceptions.InvalidEmailAddressException;
import com.victorouy.exceptions.SessionFailureException;
import com.victorouy.manager.PropertiesManager;
import com.victorouy.properties.MailConfigBean;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Locale;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application to set the GUI of the application
 *
 * @author Victor Ouy
 */
public class MainApp extends Application {
    private final static Logger LOG = LoggerFactory.getLogger(MailSendingReceiving.class);
    
    private Stage primaryStage;
    private Parent rootPane;
    private MailConfigBean mailConfigBean;
    private PropertiesManager propManager;

    private Locale currentLocale;

    /**
     * All JavaFX programs must override start and receive the Stage object from
     * the framework. This creates the scene a shows the stage
     *
     * @param primaryStage
     */
    @Override
    public void start(Stage primaryStage) throws IOException, SQLException {
        currentLocale = Locale.getDefault();
        
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle(ResourceBundle.getBundle("MessagesBundle", currentLocale).getString("title"));
        this.primaryStage.getIcons().add(
                new Image(MainApp.class.getResourceAsStream("/images/mail.png")));
        
        initRootLayout();
        Scene scene = new Scene(rootPane);
        primaryStage.setScene(scene);
        primaryStage.show();
        LOG.info("Set scene of primary stage");
    }
    
    /**
     * Load the root layout and controller for an FXML application. 
     * 
     * @throws SQLException
     * @throws IOException 
     */
    public void initRootLayout() throws SQLException, IOException {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(ResourceBundle.getBundle("MessagesBundle", currentLocale));
            
            if (retrieveMailConfig()) {
                if (checkEmailAddress()) {
                    if (checkValidEmailFields()) {
                        try {
                            loader.setLocation(MainApp.class.getResource("/fxml/RootEmailFXML.fxml"));
                            rootPane = (BorderPane) loader.load();

                            RootEmailFXMLController rootController = loader.getController();
                            rootController.initializeChildrenControllers(mailConfigBean, propManager, primaryStage);
                        }
                        catch (UnknownHostException e) {
                            LOG.error("MySQL URL error/invalid input");
                            errorAlert("errorConfigUnk");
                            initConfigPropertyLayout();
                        }
                        catch (ConnectException e) {
                            LOG.error("MySQL Port error/invalid input");
                            errorAlert("errorConfigConn");
                            initConfigPropertyLayout();
                        }
                        catch (SQLException e) {
                            LOG.error("MySQL database, password, or username error/invalid input");
                            errorAlert("errorConfigSQL");
                            initConfigPropertyLayout();
                        }
                    }
                    else {
                        LOG.error("Invalid config fields associating with email");
                        errorAlert("errorConfigEmailFields");
                        initConfigPropertyLayout();
                    }
                }
                else {
                    LOG.error("Invalid email address");
                    errorAlert("errorConfigEmail");
                    initConfigPropertyLayout();
                    return;
                }
            }
            else {
                LOG.error("Invalid configuration property file");
                errorAlert("errorConfigFile");
                initConfigPropertyLayout();
            }
        } 
        catch (IOException ex) {
            LOG.error("Error displaying form", ex);
        }
    }
    
    /**
     * Initializes the ConfigProperties layout and controller
     * 
     * @throws IOException 
     */
    public void initConfigPropertyLayout() throws IOException {
        FXMLLoader newLoader = new FXMLLoader();
        newLoader.setResources(ResourceBundle.getBundle("MessagesBundle", currentLocale));

        newLoader.setLocation(MainApp.class.getResource("/fxml/ConfigPropertiesFXML.fxml"));
        rootPane = (GridPane) newLoader.load();

        ConfigPropertiesFXMLController configController = newLoader.getController();
        configController.setupProperties(propManager, mailConfigBean, primaryStage);
    }
    
    /**
     * The stop method is called before the stage is closed. You can use this
     * method to perform any actions that must be carried out before the program
     * ends. The JavaFX GUI is still running. The only action you cannot perform
     * is to cancel the Platform.exit() that led to this method.
     */
    @Override
    public void stop() {
        LOG.info("Stage is closing");
    }

    /**
     * Loads the properties for MailConfigBean instance variable using the PropertiesManager method
     * 
     * @return
     * @throws IOException 
     */
    private boolean retrieveMailConfig() throws IOException {
        propManager = new PropertiesManager();
        mailConfigBean = new MailConfigBean();
        return propManager.loadTextProperties(mailConfigBean, "", "MailConfig");
    }
    
    /**
     * Checks if an email address is valid
     * 
     * @return true if valid, false otherwise
     */
    private boolean checkEmailAddress() {
        MailSendingReceiving sendReceive = new MailSendingReceiving();
        return sendReceive.checkMailConfigBean(mailConfigBean);
    }
    
    /**
     * Checks if configuration property fields are valid
     * 
     * @return true if valid, false otherwise
     */
    private boolean checkValidEmailFields() {
        MailSendingReceiving sendReceive = new MailSendingReceiving();
        return sendReceive.checkValidConfigBeanFields(mailConfigBean);
    }

    /**
     * Error message popup dialog
     *
     * @param msg
     */
    private void errorAlert(String msg) {
        Alert dialog = new Alert(Alert.AlertType.ERROR);
        dialog.setTitle(ResourceBundle.getBundle("MessagesBundle", currentLocale).getString("errorTitle"));
        dialog.setHeaderText(ResourceBundle.getBundle("MessagesBundle", currentLocale).getString("errorTitleConfig"));
        dialog.setContentText(ResourceBundle.getBundle("MessagesBundle", currentLocale).getString(msg));
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.showAndWait();
    }

    /**
     * Main Method that starts program
     *
     * @param args
     */
    public static void main(String[] args) throws InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException, FileNotFoundException, SQLException, IOException {
        launch(args);
        System.exit(0);
    }
}

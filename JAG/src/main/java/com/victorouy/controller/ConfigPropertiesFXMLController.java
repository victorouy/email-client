package com.victorouy.controller;

import com.victorouy.application.MainApp;
import com.victorouy.business.MailSendingReceiving;
import com.victorouy.manager.PropertiesManager;
import com.victorouy.properties.MailConfigBean;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration properties controller used to allow users to enter fields to fill a properties file
 * that will be used to set up the email client
 * 
 * @author Victor Ouy   1739282
 */
public class ConfigPropertiesFXMLController {
    
    private final static Logger LOG = LoggerFactory.getLogger(MailSendingReceiving.class);
    
    private MailConfigBean mailConfigBean;
    private PropertiesManager propManager;
    private Stage primaryStage;

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // fx:id="usernameFieldId"
    private TextField usernameFieldId; // Value injected by FXMLLoader

    @FXML // fx:id="emailAddressFieldId"
    private TextField emailAddressFieldId; // Value injected by FXMLLoader

    @FXML // fx:id="emailPasswordFieldId"
    private TextField emailPasswordFieldId; // Value injected by FXMLLoader

    @FXML // fx:id="urlIMAPFieldId"
    private TextField urlIMAPFieldId; // Value injected by FXMLLoader

    @FXML // fx:id="urlSMTPFieldId"
    private TextField urlSMTPFieldId; // Value injected by FXMLLoader

    @FXML // fx:id="portIMAPFieldId"
    private TextField portIMAPFieldId; // Value injected by FXMLLoader

    @FXML // fx:id="portSMTPFieldId"
    private TextField portSMTPFieldId; // Value injected by FXMLLoader

    @FXML // fx:id="urlMySQLFieldId"
    private TextField urlMySQLFieldId; // Value injected by FXMLLoader
    
    @FXML // fx:id="databaseMySQLFieldId"
    private TextField databaseMySQLFieldId; // Value injected by FXMLLoader

    @FXML // fx:id="portMySQLFieldId"
    private TextField portMySQLFieldId; // Value injected by FXMLLoader

    @FXML // fx:id="userMySQLFieldId"
    private TextField userMySQLFieldId; // Value injected by FXMLLoader

    @FXML // fx:id="passwordMySQLFieldId"
    private TextField passwordMySQLFieldId; // Value injected by FXMLLoader
    
    @FXML // fx:id="hboxConfig"
    private HBox hboxConfig; // Value injected by FXMLLoader
    
    /**
     * Exits application since user did not save mail configurations to build email application
     * 
     * @param event 
     */
    @FXML
    void cancelAction(ActionEvent event) {
        LOG.info("Cancel application");
        Platform.exit();
        System.exit(0);
    }

    /**
     * Saves the mail configuration fields to a MailConfigBean object which will be used
     * to connect to the database. If valid, it will show the root controller FXML layout
     * 
     * @param event
     * @throws IOException
     * @throws SQLException 
     */
    @FXML
    void saveAction(ActionEvent event) throws IOException, SQLException {
        PropertiesManager propManager = new PropertiesManager();
        propManager.writeTextProperties(mailConfigBean, "", "MailConfig");
        
        if (checkContainsNoEmptyValues()) {
            if (checkEmailAddress()) {
                if (checkValidEmailFields()) {
                    try {
                        Stage stage = (Stage) hboxConfig.getScene().getWindow();
                        stage.close();

                        // After save mail configurations, open/show rootController
                        FXMLLoader loader = new FXMLLoader();
                        loader.setResources(ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()));
                        loader.setLocation(MainApp.class.getResource("/fxml/RootEmailFXML.fxml"));
                        
                        Parent rootPane = (BorderPane) loader.load();

                        RootEmailFXMLController rootController = loader.getController();
                        rootController.initializeChildrenControllers(mailConfigBean, propManager, primaryStage);
                        LOG.info("Success: all Configurations form input fields are valid");

                        Scene scene = new Scene(rootPane);
                        primaryStage.setScene(scene);
                        primaryStage.show();
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
            }
        }
        else {
            LOG.warn("Empty values fields in mail config form");
            errorAlert("errorValuesEmpty");
            initConfigPropertyLayout();
        }
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
     * Initializes the config property controller layout and sets it to stage
     * 
     * @throws IOException 
     */
    public void initConfigPropertyLayout() throws IOException {
        FXMLLoader newLoader = new FXMLLoader();
        newLoader.setResources(ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()));

        newLoader.setLocation(MainApp.class.getResource("/fxml/ConfigPropertiesFXML.fxml"));
        Parent rootPane = (GridPane) newLoader.load();

        ConfigPropertiesFXMLController configController = newLoader.getController();
        configController.setupProperties(propManager, mailConfigBean, primaryStage);
        
        Scene scene = new Scene(rootPane);
        primaryStage.setScene(scene);
        primaryStage.show();
        LOG.info("Initiazes and displays mail config fxml layout");
    }
    
    /**
     * Checks if MailConfig properties file contains all necessary values to fill MailConfigBean
     * 
     * @param prop
     * @return true if properties file contains all full values, false otherwise
     */
    private boolean checkContainsNoEmptyValues() {
        if (usernameFieldId.getText().isEmpty() || emailAddressFieldId.getText().isEmpty() || emailPasswordFieldId.getText().isEmpty() || urlIMAPFieldId.getText().isEmpty() 
                || urlSMTPFieldId.getText().isEmpty() || portIMAPFieldId.getText().isEmpty() || portSMTPFieldId.getText().isEmpty() || urlMySQLFieldId.getText().isEmpty() 
                || databaseMySQLFieldId.getText().isEmpty() || portMySQLFieldId.getText().isEmpty() || userMySQLFieldId.getText().isEmpty() || passwordMySQLFieldId.getText().isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * This method is called by the FXMLLoader when initialization is complete
     */
    @FXML 
    void initialize() {
        assert usernameFieldId != null : "fx:id=\"usernameFieldId\" was not injected: check your FXML file 'ConfigPropertiesFXML.fxml'.";
        assert emailAddressFieldId != null : "fx:id=\"emailAddressFieldId\" was not injected: check your FXML file 'ConfigPropertiesFXML.fxml'.";
        assert emailPasswordFieldId != null : "fx:id=\"emailPasswordFieldId\" was not injected: check your FXML file 'ConfigPropertiesFXML.fxml'.";
        assert urlIMAPFieldId != null : "fx:id=\"urlIMAPFieldId\" was not injected: check your FXML file 'ConfigPropertiesFXML.fxml'.";
        assert urlSMTPFieldId != null : "fx:id=\"urlSMTPFieldId\" was not injected: check your FXML file 'ConfigPropertiesFXML.fxml'.";
        assert portIMAPFieldId != null : "fx:id=\"portIMAPFieldId\" was not injected: check your FXML file 'ConfigPropertiesFXML.fxml'.";
        assert portSMTPFieldId != null : "fx:id=\"portSMTPFieldId\" was not injected: check your FXML file 'ConfigPropertiesFXML.fxml'.";
        assert urlMySQLFieldId != null : "fx:id=\"urlMySQLFieldId\" was not injected: check your FXML file 'ConfigPropertiesFXML.fxml'.";
        assert databaseMySQLFieldId != null : "fx:id=\"databaseMySQLFieldId\" was not injected: check your FXML file 'ConfigPropertiesFXML.fxml'.";
        assert portMySQLFieldId != null : "fx:id=\"portMySQLFieldId\" was not injected: check your FXML file 'ConfigPropertiesFXML.fxml'.";
        assert userMySQLFieldId != null : "fx:id=\"userMySQLFieldId\" was not injected: check your FXML file 'ConfigPropertiesFXML.fxml'.";
        assert passwordMySQLFieldId != null : "fx:id=\"passwordMySQLFieldId\" was not injected: check your FXML file 'ConfigPropertiesFXML.fxml'.";
    }
    
    /**
     * Sets up properties of this controller
     * 
     * @param propertiesManger
     * @param mailConfigBean
     * @param primaryStage 
     */
    public void setupProperties(PropertiesManager propertiesManger, MailConfigBean mailConfigBean, Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.propManager = propertiesManger;
        this.mailConfigBean = mailConfigBean;
        doBindings();
        LOG.info("ConfigPropertiesFXMLController properites set");
    }
    
    /**
     * Binds the mail config fxbean with the javafx text properties
     */
    private void doBindings() {
        Bindings.bindBidirectional(usernameFieldId.textProperty(), mailConfigBean.usernameProperty());
        Bindings.bindBidirectional(emailAddressFieldId.textProperty(), mailConfigBean.userEmailAddressProperty());
        Bindings.bindBidirectional(emailPasswordFieldId.textProperty(), mailConfigBean.pwdEmailAddressProperty());
        Bindings.bindBidirectional(urlIMAPFieldId.textProperty(), mailConfigBean.urlIMAPProperty());
        Bindings.bindBidirectional(urlSMTPFieldId.textProperty(), mailConfigBean.urlSMTPProperty());
        Bindings.bindBidirectional(portIMAPFieldId.textProperty(), mailConfigBean.portIMAPProperty());
        Bindings.bindBidirectional(portSMTPFieldId.textProperty(), mailConfigBean.portSMTPProperty());
        Bindings.bindBidirectional(urlMySQLFieldId.textProperty(), mailConfigBean.urlMySQLProperty());
        Bindings.bindBidirectional(databaseMySQLFieldId.textProperty(), mailConfigBean.databaseProperty());
        Bindings.bindBidirectional(portMySQLFieldId.textProperty(), mailConfigBean.portMySQLProperty());
        Bindings.bindBidirectional(userMySQLFieldId.textProperty(), mailConfigBean.userMySQLProperty());
        Bindings.bindBidirectional(passwordMySQLFieldId.textProperty(), mailConfigBean.pwdMySQLProperty());
        
        LOG.info("Mail config fxbean with the javafx text properties binded");
    }
    
    /**
     * Error message popup dialog
     *
     * @param msg
     */
    private void errorAlert(String msg) {
        Alert dialog = new Alert(Alert.AlertType.ERROR);
        dialog.setTitle(ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()).getString("errorTitle"));
        dialog.setHeaderText(ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()).getString("errorTitle"));
        dialog.setContentText(ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()).getString(msg));
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.showAndWait();
    }
}

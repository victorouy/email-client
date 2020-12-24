package com.victorouy.controller;

import com.victorouy.application.MainApp;
import com.victorouy.business.MailSendingReceiving;
import com.victorouy.manager.PropertiesManager;
import com.victorouy.persistence.EmailDAO;
import com.victorouy.persistence.EmailDAOImpl;
import com.victorouy.properties.MailConfigBean;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Root Email Controller that is the parent of its children controllers that builds it all together 
 * and offers a menu bar for users to perform multiple actions such as going back to the config controller
 * 
 * @author Victor Ouy   1739282
 */
public class RootEmailFXMLController {
    
    private final static Logger LOG = LoggerFactory.getLogger(MailSendingReceiving.class);
    
    private PropertiesManager propManager;
    private MailConfigBean mailConfig;
    private EmailDAO emailDAO;
    private FolderTreeFXMLController folderTreeFXMLController;
    private EmailTableFXMLController emailTableFXMLController;
    private EditorFXMLController editorFXMLController;
    private Stage primaryStage;
    private static final int BLOBSIZELIMIT = 65535;
    
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // fx:id="leftSplit"
    private BorderPane leftSplit; // Value injected by FXMLLoader

    @FXML // fx:id="rightUpperSplit"
    private BorderPane rightUpperSplit; // Value injected by FXMLLoader

    @FXML // fx:id="rightLowerSplit"
    private BorderPane rightLowerSplit; // Value injected by FXMLLoader

    
    
    /**
     * Event handler on click of menu item change configurations that will change the primary stage to the mail config controller
     * layout to allow users to update their configurations 
     * 
     * @param event
     * @throws IOException 
     */
    @FXML
    void doChangeConfig(ActionEvent event) throws IOException {
        String alertMsg = ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()).getString("alertMsgMailConfig");
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, alertMsg, ButtonType.YES, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()));

            loader.setLocation(MainApp.class.getResource("/fxml/ConfigPropertiesFXML.fxml"));
            Parent rootPane = (GridPane) loader.load();

            ConfigPropertiesFXMLController configController = loader.getController();
            configController.setupProperties(propManager, mailConfig, primaryStage);

            Scene scene = new Scene(rootPane);
            primaryStage.setScene(scene);
            primaryStage.show();
            LOG.info("Mail config properties form displayed");
        }
    }
    
    /**
     * Event handler on click of about menu item that will set a stage and scene of the
     * Web View controller and layout FXML that will display an HTML page about buttons of 
     * the application
     * 
     * @param event 
     */
    @FXML
    void doHelp(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setResources(ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()));

        Stage stage = new Stage();
        stage.setTitle(ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()).getString("help"));
        loader.setLocation(MainApp.class.getResource("/fxml/HelpWebViewFXML.fxml"));
        AnchorPane anchorPane = (AnchorPane) loader.load();
        
        stage.setScene(new Scene(anchorPane));
        stage.show();
        LOG.info("Help html page displayed");
    }
    
    /**
     * Event handler that opens the WebView of about
     * 
     * @param event
     * @throws IOException 
     */
    @FXML
    void doAbout(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setResources(ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()));

        Stage stage = new Stage();
        stage.setTitle(ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()).getString("about"));
        loader.setLocation(MainApp.class.getResource("/fxml/AboutWebViewFXML.fxml"));
        AnchorPane anchorPane = (AnchorPane) loader.load();
        
        stage.setScene(new Scene(anchorPane));
        stage.show();
        LOG.info("About html page displayed");
    }

    /**
     * Event handler on click of close menu item that will ask user confirmation
     * if they would like to close the program
     * 
     * @param event 
     */
    @FXML
    void doClose(ActionEvent event) {
        String alertMsg = ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()).getString("alertMsgClose");
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, alertMsg, ButtonType.YES, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            LOG.info("Exit application");
            Platform.exit();
            System.exit(0);
        }
    }
    
    /**
     * Event handler on click of menu item to add attachments
     * 
     * @param event 
     */
    @FXML
    void doAddAttachments(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()).getString("chooseAttachment"));
        File fileAdd = fileChooser.showOpenDialog(this.primaryStage);
        
        if (fileAdd.length() > BLOBSIZELIMIT) {
            errorAlert("errorAttachLimit");
            return;
        }
        if (fileAdd != null) {
            this.editorFXMLController.addAttachment(fileAdd);
            LOG.info("Added attachment");
        }
    }

    /**
     * Event handler on click of menu item to save attachments
     * 
     * @param event 
     */
    @FXML
    void doSaveAttachments(ActionEvent event) {
        if (this.emailTableFXMLController.getEmailDataTable().getSelectionModel().getSelectedItem() != null) {
            if (this.editorFXMLController.getAttachments().size() == 0) {
                LOG.error("Cannot save attachment if there are no attachments on the email selected");
                errorAlert("errorAttachEmpty");
                return;
            }
            
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle(ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()).getString("saveAttachment"));
            File directorySave = directoryChooser.showDialog(primaryStage);

            if (directorySave != null && directorySave.isDirectory()) {
                for (File fileSave : this.editorFXMLController.getAttachments()) {
                    String path = directorySave.getAbsolutePath() + "/" + fileSave.getName();
                    File filePath = new File(path);
                    try {
                        OutputStream outputStream = new FileOutputStream(filePath);
                        outputStream.write(Files.readAllBytes(fileSave.toPath()));
                    } 
                    catch (IOException e) {
                        LOG.error("IOException when attempting to save attachment to file");
                        errorAlert("errorAttachIO");
                    }
                    LOG.info(fileSave.getName() + " saved to directory");
                }
            }
        }
        else {
            errorAlert("errorSaveAttach");
        }
    }
    
    /**
     * Initializes the properties and calls the other initializes the other children layouts
     * and displays the tree of folders
     * 
     * @throws SQLException
     */
    public void initializeChildrenControllers(MailConfigBean mailConfig, PropertiesManager propManager, Stage primaryStage) throws SQLException {
        this.primaryStage = primaryStage;
        this.propManager = propManager;
        this.mailConfig = mailConfig;
        this.emailDAO = new EmailDAOImpl(mailConfig);
        
        initLeftLayout();
        initRightUpperLayout();
        initRightLowerLayout();
        
        this.folderTreeFXMLController.setTableController(emailTableFXMLController);
        this.folderTreeFXMLController.displayTree();
        this.emailTableFXMLController.setEditorController(editorFXMLController);
        LOG.info("Folder tree displayed");
    }
    
    /**
     * Initializes the folder tree to the root layout
     */
    private void initLeftLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(resources);

            loader.setLocation(RootEmailFXMLController.class
                    .getResource("/fxml/FolderTreeFXML.fxml"));
            BorderPane treeView = (BorderPane) loader.load();

            // Give the controller the emailDAO object.
            folderTreeFXMLController = loader.getController();
            folderTreeFXMLController.setEmailDAO(emailDAO);

            leftSplit.getChildren().add(treeView);
            
            treeView.prefHeightProperty().bind(leftSplit.heightProperty());
            treeView.prefWidthProperty().bind(leftSplit.widthProperty());
            LOG.info("Folder tree binded");
        } 
        catch (IOException ex) {
            LOG.error("initLeftLayout error", ex);
            Platform.exit();
            System.exit(0);
        }
    }
    
    /**
     * Initializes the email table to the root layout
     */
    private void initRightUpperLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(resources);

            loader.setLocation(RootEmailFXMLController.class
                    .getResource("/fxml/EmailTableFXML.fxml"));
            BorderPane tableView = (BorderPane) loader.load();

            // Give the controller the emailDAO object.
            emailTableFXMLController = loader.getController();
            emailTableFXMLController.setEmailDAO(this.emailDAO);
            emailTableFXMLController.setMailConfig(this.mailConfig);
            emailTableFXMLController.setFolderTreeController(folderTreeFXMLController);

            rightUpperSplit.getChildren().add(tableView);
            
            tableView.prefHeightProperty().bind(rightUpperSplit.heightProperty());
            tableView.prefWidthProperty().bind(rightUpperSplit.widthProperty());
            LOG.info("Email table binded");
        } 
        catch (IOException ex) {
            LOG.error("initUpperRightLayout error", ex);
            Platform.exit();
            System.exit(0);
        }
    }
    
    /**
     * Initializes the HTML editor to the root layout
     */
    private void initRightLowerLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setResources(resources);

            loader.setLocation(RootEmailFXMLController.class
                    .getResource("/fxml/EditorFXML.fxml"));
            BorderPane tableView = (BorderPane) loader.load();

            // Give the controller the emailDAO object.
            editorFXMLController = loader.getController();
            editorFXMLController.setEmailDAO(emailDAO);
            editorFXMLController.setMailConfig(mailConfig);
            editorFXMLController.setFolderTreeController(folderTreeFXMLController);

            rightLowerSplit.getChildren().add(tableView);
            
            tableView.prefHeightProperty().bind(rightLowerSplit.heightProperty());
            tableView.prefWidthProperty().bind(rightLowerSplit.widthProperty());
            LOG.info("HTML editor binded");
        } 
        catch (IOException ex) {
            LOG.error("initRightLowerLayout error", ex);
            Platform.exit();
            System.exit(0);
        }
    }
    
    /**
     * Error message popup dialog
     *
     * @param msg
     */
    private void errorAlert(String msg) {
        Alert dialog = new Alert(Alert.AlertType.ERROR);
        dialog.setTitle(ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()).getString("errorTitle"));
        dialog.setHeaderText(ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()).getString("errorText"));
        dialog.setContentText(ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()).getString(msg));
        dialog.show();
    }
}
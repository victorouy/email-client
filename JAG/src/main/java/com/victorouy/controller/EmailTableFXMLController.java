package com.victorouy.controller;

import com.victorouy.business.MailSendingReceiving;
import com.victorouy.exceptions.InvalidEmailAddressException;
import com.victorouy.persistence.EmailDAO;
import com.victorouy.properties.EmailDataBean;
import com.victorouy.properties.EmailTableFXBean;
import com.victorouy.properties.MailConfigBean;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import jodd.mail.ReceivedEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Email table controller used to display the emails and allow certain actions such as deleting, replying, forwarding
 * 
 * @author Victor Ouy   1739282
 */
public class EmailTableFXMLController {

    private final static Logger LOG = LoggerFactory.getLogger(MailSendingReceiving.class);

    private EmailDAO emailDAO;
    private MailConfigBean mailConfig;
    private EditorFXMLController editorFXMLController;
    private FolderTreeFXMLController folderTreeFXMLController;
    private final static int DRAFTKEY = 3;
    private final static int INBOXKEY = 1;

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // fx:id="emailFXTable"
    private BorderPane emailFXTable; // Value injected by FXMLLoader

    @FXML // fx:id="emailDataTable"
    private TableView<EmailTableFXBean> emailDataTable; // Value injected by FXMLLoader

    @FXML // fx:id="idColumn"
    private TableColumn<EmailTableFXBean, Number> idColumn; // Value injected by FXMLLoader

    @FXML // fx:id="fromColumn"
    private TableColumn<EmailTableFXBean, String> fromColumn; // Value injected by FXMLLoader

    @FXML // fx:id="subjectColumn"
    private TableColumn<EmailTableFXBean, String> subjectColumn; // Value injected by FXMLLoader

    @FXML // fx:id="dateColumn"
    private TableColumn<EmailTableFXBean, LocalDateTime> dateColumn; // Value injected by FXMLLoader
    
    @FXML // fx:id="replyButton"
    private Button replyButton; // Value injected by FXMLLoader

    @FXML // fx:id="replyAllButton"
    private Button replyAllButton; // Value injected by FXMLLoader

    @FXML // fx:id="forwwarButton"
    private Button forwardButton; // Value injected by FXMLLoader

    @FXML // fx:id="delButton"
    private Button delButton; // Value injected by FXMLLoader

    /**
     * Initializes the EmailTableFXMLController
     */
    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        idColumn.setCellValueFactory(cellData -> cellData.getValue().emailIdProperty());
        fromColumn.setCellValueFactory(cellData -> cellData.getValue().fromProperty());
        subjectColumn.setCellValueFactory(cellData -> cellData.getValue().subjectProperty());
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        LOG.info("Email table FXBean binded");

        bindButtons();
        adjustColumnWidths();
    }

    /**
     * The table displays the email data Sourced from database
     *
     * @throws SQLException
     * @throws IOException
     */
    public void displayTable() throws SQLException, IOException {
        ObservableList<EmailTableFXBean> findAll = this.emailDAO.findAll();
        Iterator<EmailTableFXBean> iterate = findAll.iterator();
        
        while (iterate.hasNext()) {
            EmailTableFXBean emailTableBean = iterate.next();
            boolean sameEmail = emailTableBean.getFrom().equals(this.mailConfig.getUserEmailAddress());
            if (emailTableBean.getFolderKey() == this.DRAFTKEY && !sameEmail) {
                iterate.remove();
            }
        }
        emailDataTable.setItems(findAll);
        LOG.info("Email table displayed");
    }
    
    /**
     * Binds the disable property button with the table view so that it only enables the button when an email 
     * has been selected
     */
    private void bindButtons() {
        // Binding the buttons to the selected row to disable them if no row is selected
        replyButton.disableProperty().bind(Bindings.isEmpty(emailDataTable.getSelectionModel().getSelectedItems()));
        replyAllButton.disableProperty().bind(Bindings.isEmpty(emailDataTable.getSelectionModel().getSelectedItems()));
        forwardButton.disableProperty().bind(Bindings.isEmpty(emailDataTable.getSelectionModel().getSelectedItems()));
        delButton.disableProperty().bind(Bindings.isEmpty(emailDataTable.getSelectionModel().getSelectedItems()));
    }

    /**
     * Event listener of the reply button that calls the editor controller method replyFill given the selected email
     * 
     * @param event
     * @throws SQLException
     * @throws IOException 
     */
    @FXML
    void replyAction(ActionEvent event) throws SQLException, IOException {
        EmailTableFXBean selectedEmail = emailDataTable.getSelectionModel().getSelectedItem();
        editorFXMLController.replyFill(emailDAO.findByID(selectedEmail.getEmailId()));
    }

    /**
     * Event listener of the reply all button that calls the editor controller method replyAllFill given the selected email
     * 
     * @param event
     * @throws SQLException
     * @throws IOException 
     */
    @FXML
    void replyAllAction(ActionEvent event) throws SQLException, IOException {
        EmailTableFXBean selectedEmail = emailDataTable.getSelectionModel().getSelectedItem();
        editorFXMLController.replyAllFill(emailDAO.findByID(selectedEmail.getEmailId()));
    }
    
    /**
     * Event listener of the forward button that calls the editor controller method forwardFill given the selected email
     * 
     * @param event
     * @throws SQLException
     * @throws IOException 
     */
    @FXML
    void forwardAction(ActionEvent event) throws SQLException, IOException {
        EmailTableFXBean selectedEmail = emailDataTable.getSelectionModel().getSelectedItem();
        editorFXMLController.forwardFill(emailDAO.findByID(selectedEmail.getEmailId()));
    }
    
    /**
     * Event listener of the delete button that deletes the email of the selected row from the database
     * 
     * @param event
     * @throws SQLException
     * @throws IOException 
     */
    @FXML
    void deleteAction(ActionEvent event) throws SQLException, IOException {
        String alertMsg = ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()).getString("delEmail");
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, alertMsg, ButtonType.YES, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            EmailTableFXBean selectedEmail = emailDataTable.getSelectionModel().getSelectedItem();
            emailDAO.deleteEmail(selectedEmail.getEmailId());
            folderTreeFXMLController.showTreeDetails(selectedEmail.getFolderKey());
        }
    }
    
    /**
     * This refreshes a table of the current folder. This is mainly used for the INBOX folder since it 
     * interacts with the internet to receive incoming emails from the user's email
     * 
     * @param event
     * @throws InvalidEmailAddressException
     * @throws SQLException
     * @throws IOException 
     */
    @FXML
    void refreshAction(ActionEvent event) throws InvalidEmailAddressException, SQLException, IOException {
        if (this.folderTreeFXMLController.getCurrentFolderKey() == INBOXKEY) {
            MailSendingReceiving sendReceive = new MailSendingReceiving();
            ReceivedEmail[] receivedEmails = sendReceive.receiveEmail(mailConfig);
            
            if (receivedEmails != null) {
                LOG.info("Refresh button clicked");
                emailDAO.createReceivedEmail(receivedEmails);
            }
        }
        folderTreeFXMLController.showTreeDetails(this.folderTreeFXMLController.getCurrentFolderKey());
    }
    
    /**
     * Event listener when an email table row is selected
     * 
     * @param event
     * @throws SQLException
     * @throws IOException 
     */
    @FXML
    void clickedEmailRow(MouseEvent event) throws SQLException, IOException {
        LOG.info("Email row clicked");
        EmailTableFXBean selectedEmail = emailDataTable.getSelectionModel().getSelectedItem();
        
        if (selectedEmail != null) {
            // Disables the reply, replyall, forward button if select an email row inside draft folder
            if (selectedEmail.getFolderKey() == DRAFTKEY) {
                replyButton.disableProperty().unbind();
                replyAllButton.disableProperty().unbind();
                forwardButton.disableProperty().unbind();
                
                replyButton.setDisable(true);
                replyAllButton.setDisable(true);
                forwardButton.setDisable(true);
            }
            else {
                bindButtons();
            }
            this.editorFXMLController.displayEmail(selectedEmail);
        }
    }

    /**
     * This allows an email row to be dragged
     *
     * @param event
     */
    @FXML
    void dragDetected(MouseEvent event) {
        LOG.info("Drag Detected");
        EmailTableFXBean selectedEmail = emailDataTable.getSelectionModel().getSelectedItem();
        if (selectedEmail != null) {
            Dragboard db = emailDataTable.startDragAndDrop(TransferMode.ANY);
            
            ClipboardContent content = new ClipboardContent();
            content.putString(Integer.toString(selectedEmail.getEmailId()));
            
            db.setContent(content);
            event.consume();
        }
    }

    /**
     * Sets the width of the columns based on a percentage of the overall width
     */
    private void adjustColumnWidths() {
        double width = emailFXTable.getPrefWidth();
        idColumn.setPrefWidth(width * 0.04);
        fromColumn.setPrefWidth(width * 0.30);
        subjectColumn.setPrefWidth(width * 0.51);
        dateColumn.setPrefWidth(width * 0.20);
    }
    
    public TableView<EmailTableFXBean> getEmailDataTable() {
        return this.emailDataTable;
    }
    
    public void setEmailDAO(EmailDAO emailDAO) {
        this.emailDAO = emailDAO;
    }
    
    public void setEditorController(EditorFXMLController editorFXMLController) {
        this.editorFXMLController = editorFXMLController;
    }
    
    public void setFolderTreeController(FolderTreeFXMLController folderTreeFXMLController) {
        this.folderTreeFXMLController = folderTreeFXMLController;
    }
    
    public void setMailConfig(MailConfigBean mailConfig) {
        this.mailConfig = mailConfig;
    }
}

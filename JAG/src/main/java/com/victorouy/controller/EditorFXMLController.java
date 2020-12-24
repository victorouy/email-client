package com.victorouy.controller;

import com.victorouy.business.MailSendingReceiving;
import com.victorouy.exceptions.AbsentEmailAddressException;
import com.victorouy.exceptions.ForbiddenEmailEditAttempException;
import com.victorouy.exceptions.InvalidEmailAddressException;
import com.victorouy.exceptions.SessionFailureException;
import com.victorouy.persistence.EmailDAO;
import com.victorouy.properties.HTMLEditorFXBean;
import com.victorouy.properties.EmailDataBean;
import com.victorouy.properties.EmailTableFXBean;
import com.victorouy.properties.FormFXBean;
import com.victorouy.properties.MailConfigBean;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Modality;
import jodd.mail.Email;
import jodd.mail.EmailAddress;
import jodd.mail.EmailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTML editor controller used to fill in recipient fields, subject, and HTML message to send, save,
 * or compose an email
 * 
 * @author Victor Ouy   1739282
 */
public class EditorFXMLController {
    
    private final static Logger LOG = LoggerFactory.getLogger(MailSendingReceiving.class);
    
    private FormFXBean formFXBean;
    private HTMLEditorFXBean HTMLEditorBean;
    private MailConfigBean mailConfig;
    
    private EmailDAO emailDAO;
    private MailSendingReceiving sendReceive;
    private FolderTreeFXMLController folderTreeFXMLController;
    
    private final static int DRAFTKEY = 3;
    private int draftEmailId = -1;
    private ArrayList<File> attachments;

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // fx:id="toField"
    private TextField toField; // Value injected by FXMLLoader

    @FXML // fx:id="bccField"
    private TextField bccField; // Value injected by FXMLLoader

    @FXML // fx:id="ccField"
    private TextField ccField; // Value injected by FXMLLoader

    @FXML // fx:id="subjectField"
    private TextField subjectField; // Value injected by FXMLLoader

    @FXML // fx:id="emailHTMLEditor"
    private HTMLEditor emailHTMLEditor; // Value injected by FXMLLoader
    
    @FXML // fx:id="sendButton"
    private Button sendButton; // Value injected by FXMLLoader

    @FXML // fx:id="saveButton"
    private Button saveButton; // Value injected by FXMLLoader
    
    /**
     * Sends an email and stores it in the database
     * 
     * @param event
     * @throws SessionFailureException
     * @throws FileNotFoundException
     * @throws SQLException
     * @throws IOException
     * @throws ForbiddenEmailEditAttempException 
     */
    @FXML
    void sendAction(ActionEvent event) throws SessionFailureException, FileNotFoundException, SQLException, IOException, ForbiddenEmailEditAttempException {
        try {
            ArrayList<String> toFields = getRecipients(this.formFXBean.getTo());
            ArrayList<String> ccFields = getRecipients(this.formFXBean.getCc());
            ArrayList<String> bccFields = getRecipients(this.formFXBean.getBcc());
            
            // Stores text messages and embedded attachments as null since we use its alternatives
            Email emailSent = sendReceive.sendEmail(mailConfig, toFields, ccFields, bccFields, this.formFXBean.getSubject(), 
                    null, emailHTMLEditor.getHtmlText(), null, attachments);
            
            if (draftEmailId != -1) {
                // Sends a email in draft
                EmailDataBean emailBean = emailDAO.findByID(draftEmailId);
                int folderKey = emailBean.getFolderKey();
                emailBean.email = emailSent;
                emailDAO.updateSendDraftEmail(emailBean);
                folderTreeFXMLController.showTreeDetails(folderKey);
            }
            else {
                // Sends a composed email
                EmailDataBean emailBean = new EmailDataBean(-1, -1, null, emailSent);
                emailDAO.createSendEmail(emailBean);
                folderTreeFXMLController.showTreeDetails(emailBean.getFolderKey());
            }
            infoAlert("confirm", "sentEmail");
            this.attachments = new ArrayList<File>();
        }
        catch (InvalidEmailAddressException e) {
            LOG.error("InvalidEmailAddressException");
            errorAlert("errorTitle", "errorTitle", "errorConfigEmail");
        }
        catch (SessionFailureException e) {
            LOG.error("SessionFailureException");
            errorAlert("errorTitle", "errorTitle", "errorTitle");
        }
        catch (ForbiddenEmailEditAttempException e) {
            LOG.error("ForbiddenEmailEditAttempException");
            errorAlert("errorTitle", "errorTitle", "errorTitle");
        }
        catch (AbsentEmailAddressException e) {
            LOG.error("AbsentEmailAddressException");
            errorAlert("errorTitle", "errorTitle", "errorRecipients");
        }
    }
    
    /**
     * The listener for the save as draft button that takes the current fields and
     * creates a saved email that stores it into the database
     * 
     * @param event 
     */
    @FXML
    void saveAction(ActionEvent event) throws SQLException, IOException {
        try {
            // If draftEmailId is not -1, then it is not a composed email but a draft email
            // that will be saved once again. The distinction is required since when saving 
            // an email that is already in draft, it should replace its existing one instead
            // of creating a new one by finding its emailId.
            if (draftEmailId != -1) {
                EmailDataBean emailBean = emailDAO.findByID(draftEmailId);
                Email emailSaved = getSavedEmail();
                emailBean.email = emailSaved;
                emailDAO.updateEditDraftEmail(emailBean);
                
                folderTreeFXMLController.showTreeDetails(emailBean.getFolderKey());
                infoAlert("confirm", "editDraft");
            }
            else {
                // When saving a composed email
                Email emailSaved = getSavedEmail();
                EmailDataBean emailBean = new EmailDataBean(-1, -1, null, emailSaved);
                emailDAO.createSaveEmail(emailBean);
                
                folderTreeFXMLController.showTreeDetails(emailBean.getFolderKey());
                infoAlert("confirm", "saveEmail");
            }
            LOG.info("Saved email");
        }
        catch (ForbiddenEmailEditAttempException e) {
            LOG.error("ForbiddenEmailEditAttempException");
            errorAlert("errorTitle", "errorTitle", "errorTitle");
        }
    }
    
    /**
     * Event listener when clicking the compose button that resets all the fields
     * to create a new email
     * 
     * @param event 
     */
    @FXML
    void composeAction(ActionEvent event) {
        enableButtons();
        draftEmailId = -1;
        formFXBean.setTo("");
        formFXBean.setCc("");
        formFXBean.setBcc("");
        formFXBean.setSubject("");
        emailHTMLEditor.setHtmlText("");
        this.attachments = new ArrayList<File>();
    }
    
    /**
     * Event listener when reply to a selected email
     * 
     * @param emailDataBean
     * @throws SQLException 
     */
    public void replyFill(EmailDataBean emailDataBean) throws SQLException {
        enableButtons();
        draftEmailId = -1;
        formFXBean.setTo(emailDataBean.email.from().getEmail());
        formFXBean.setCc("");
        formFXBean.setBcc("");
        formFXBean.setSubject(emailDataBean.email.subject());
        setReplied(emailDataBean);
        setContent(emailDataBean, getReplied(emailDataBean));
        LOG.info("Reply filled");
    }
    
    /**
     * Event listener when reply all to a selected email
     * 
     * @param emailDataBean
     * @throws SQLException 
     */
    public void replyAllFill(EmailDataBean emailDataBean) throws SQLException {
        enableButtons();
        draftEmailId = -1;
        
        // This checks if the "from" email address is already in the email recipients to avoid
        // duplicating its email address in the fields
        if (checkFromInsideFields(emailDataBean)) {
            formFXBean.setTo(recipientString(emailDataBean.email.to()));
        }
        else {
            formFXBean.setTo(emailDataBean.email.from().getEmail() + " " + recipientString(emailDataBean.email.to()));
        }
        formFXBean.setCc(recipientString(emailDataBean.email.cc()));
        
        // This checks if the user's email address is whom sent the email
        if (checkShowAllBcc(emailDataBean)) {
            formFXBean.setBcc(recipientString(emailDataBean.email.bcc()));
        }
        else if (checkShowBcc(emailDataBean)) {
            formFXBean.setBcc(this.mailConfig.getUserEmailAddress().toString());
        }
        else {
            formFXBean.setBcc("");
        }
        formFXBean.setSubject(emailDataBean.email.subject());
        setReplied(emailDataBean);
        setContent(emailDataBean, getReplied(emailDataBean));
        LOG.info("Reply to all filled");
    }
    
    /**
     * Event listener when forward button is clicked of a selected email 
     * 
     * @param emailDataBean
     * @throws SQLException 
     */
    public void forwardFill(EmailDataBean emailDataBean) throws SQLException {
        enableButtons();
        draftEmailId = -1;
        formFXBean.setTo("");
        formFXBean.setCc("");
        formFXBean.setBcc("");
        setContent(emailDataBean, getForward(emailDataBean));
    }
    
    /**
     * Display all the contents and fields in the email editor of the email selected by user
     * 
     * @param selectedEmail
     * @throws SQLException
     * @throws IOException 
     */
    public void displayEmail(EmailTableFXBean selectedEmail) throws SQLException, IOException {
        EmailDataBean emailDataBean = this.emailDAO.findByID(selectedEmail.getEmailId());
        if (emailDataBean.getFolderKey() == DRAFTKEY) {
            this.draftEmailId = emailDataBean.getEmailID();
            enableButtons();
        }
        else {
            this.draftEmailId = -1;
            disableButtons();
        }
        formFXBean.setTo(recipientString(emailDataBean.email.to()));
        formFXBean.setCc(recipientString(emailDataBean.email.cc()));

        if (checkShowAllBcc(emailDataBean)) {
            formFXBean.setBcc(recipientString(emailDataBean.email.bcc()));
        }
        else if (checkShowBcc(emailDataBean)) {
            formFXBean.setBcc(this.mailConfig.getUserEmailAddress().toString());
        }
        else {
            formFXBean.setBcc("");
        }
        formFXBean.setSubject(emailDataBean.email.subject());
        setContent(emailDataBean, "");
    }
    
    /**
     * This method adds an File to the list of attachments and displays the attachment in the message
     * 
     * @param fileAttachment 
     */
    public void addAttachment(File fileAttachment) {
        this.attachments.add(fileAttachment);
        String htmlImage = "<img src='" + fileAttachment.toURI() + "'/> ";
        this.emailHTMLEditor.setHtmlText(this.emailHTMLEditor.getHtmlText() + htmlImage);
    }
    
    /**
     * Checks if the "from" email address is inside an recipient fields
     * 
     * @param emailDataBean
     * @return true if inside any recipient fields, false otherwise
     */
    private boolean checkFromInsideFields(EmailDataBean emailDataBean) {
        String fromField = emailDataBean.email.from().toString().toLowerCase();
        for (EmailAddress emailAddress : emailDataBean.email.to()) {
            if (emailAddress.toString().toLowerCase().equals(fromField)) {
                return true;
            }
        }
        for (EmailAddress emailAddress : emailDataBean.email.cc()) {
            if (emailAddress.toString().toLowerCase().equals(fromField)) {
                return true;
            }
        }
        if (checkShowAllBcc(emailDataBean)) {
            for (EmailAddress emailAddress : emailDataBean.email.bcc()) {
                if (emailAddress.toString().toLowerCase().equals(fromField)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Checks if if user's email address is whom sent the email or the email is in draft
     * 
     * @param emailDataBean
     * @return true if user's email address is whom sent the email or the email is in draft, false otherwise
     */
    private boolean checkShowAllBcc(EmailDataBean emailDataBean) {
        String userEmailAddress = this.mailConfig.getUserEmailAddress().toString().toLowerCase();
        boolean sentEmail = emailDataBean.email.from().toString().toLowerCase().equals(userEmailAddress);
        boolean inDraft = emailDataBean.getFolderKey() == this.DRAFTKEY;
        return sentEmail || inDraft;
    }
    
    /**
     * This checks to display the bcc if the user's email address is in the bcc field
     * 
     * @param emailDataBean
     * @return true if user's email address in the bcc field, false otherwise
     */
    private boolean checkShowBcc(EmailDataBean emailDataBean) {
        // Displays the BCC if the email is from the user or the user is in BCC or if draft email
        String userEmailAddress = this.mailConfig.getUserEmailAddress().toString().toLowerCase();
        for (EmailAddress emailAddress : emailDataBean.email.bcc()) {
            if (emailAddress.toString().toLowerCase().equals(userEmailAddress)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Sets the content/message
     * 
     * @param emailDataBean
     * @param replied
     * @throws SQLException 
     */
    private void setContent(EmailDataBean emailDataBean, String replied) throws SQLException {
        StringBuilder attachmentString = new StringBuilder();
        this.attachments = new ArrayList<File>();
        for (String fileNameEmb: emailDAO.findAttachmentNames(emailDataBean.getEmailID())) {
            // Since in EmailDAOImpl, it already saves the attachment file to disk
            File file = new File(fileNameEmb);
            attachments.add(file);
            attachmentString.append("<img src='" + file.toURI() + "'/> ");
        }
        for (EmailMessage emailMsg : emailDataBean.email.messages()) {
            String content = emailMsg.getContent().replaceAll("\\<.*?\\>","");
            this.emailHTMLEditor.setHtmlText(replied + content + attachmentString.toString());
        }
    }
    
    /**
     * Gets the replied content of a message when replying to an email
     * 
     * @param emailDataBean
     * @return 
     */
    private String getReplied(EmailDataBean emailDataBean) {
        return "<br><br><hr><p>On " + emailDataBean.email.sentDate() + " " + emailDataBean.email.from().getEmail() + " wrote:</p><br>";
    }
    
    /**
     * Sets the subject when replying to an email
     * 
     * @param emailDataBean
     * @return 
     */
    private void setReplied(EmailDataBean emailDataBean) {
        if (formFXBean.getSubject() != null) {
            this.formFXBean.setSubject("RE: " + formFXBean.getSubject());
        }
    }
    
    /**
     * Gets a Jodd email that has been saved
     * 
     * @return a Jodd email that has been saved
     * @throws FileNotFoundException 
     */
    private Email getSavedEmail() throws FileNotFoundException {
        ArrayList<String> toFields = getRecipients(this.formFXBean.getTo());
        ArrayList<String> ccFields = getRecipients(this.formFXBean.getCc());
        ArrayList<String> bccFields = getRecipients(this.formFXBean.getBcc());

        Email emailSaved = sendReceive.saveEmail(mailConfig, toFields, ccFields, bccFields, this.formFXBean.getSubject(), 
                null, emailHTMLEditor.getHtmlText(), null, attachments);
        return emailSaved;
    }
    
    /**
     * Gets the forward message of a forwarded email
     * 
     * @param emailDataBean
     * @return the forward message of a forwarded email
     */
    private String getForward(EmailDataBean emailDataBean) {
        if (formFXBean.getSubject() != null) {
            this.formFXBean.setSubject("FWD: " + formFXBean.getSubject());
        }
        StringBuilder replied = new StringBuilder();
        replied.append("<p>------------- Forwarded message ---------</p>");
        replied.append("<p>From: " + emailDataBean.email.from().getEmail() + "</p>");
        replied.append("<p>Date: " + emailDataBean.email.sentDate() + "</p>");
        if (emailDataBean.email.subject() != null) {
            replied.append("<p>Subject: " + emailDataBean.email.subject() + "</p><br>");
        }
        return replied.toString();
    }
    
    /**
     * Gets the recipient string that is separated by a blank space
     * 
     * @param recipientEmails
     * @return the recipient string
     */
    private String recipientString(EmailAddress[] recipientEmails) {
        String recipients = "";
        for (EmailAddress recipientEmail : recipientEmails) {
            recipients = recipients + recipientEmail + " ";
        }
        return recipients;
    }
    
    /**
     * Gets the recipients ArrayList in String
     * 
     * @param recipients
     * @return ArrayList<String> of recipients
     */
    private ArrayList<String> getRecipients(String recipients) {
        ArrayList<String> recipientFields = new ArrayList<String>();
        for (String emailAddress : recipients.split(" ")) {
            recipientFields.add(emailAddress);
        }
        return recipientFields;
    }
    
    /**
     * This method enables the send and save button
     */
    private void enableButtons() {
        this.sendButton.setDisable(false);
        this.saveButton.setDisable(false);
    }
    
    /**
     * This method disables the send and save button
     */
    private void disableButtons() {
        this.sendButton.setDisable(true);
        this.saveButton.setDisable(true);
    }
    
    public ArrayList<File> getAttachments() {
        return this.attachments;
    }
    
    public void setEmailDAO(EmailDAO emailDAO) {
        this.emailDAO = emailDAO;
    }
    
    public void setMailConfig(MailConfigBean mailConfig) {
        this.mailConfig = mailConfig;
    }
    
    public void setFolderTreeController(FolderTreeFXMLController folderTreeFXMLController) {
        this.folderTreeFXMLController = folderTreeFXMLController;
    }
    
    /**
     * Error message popup dialog
     *
     * @param msg
     */
    private void errorAlert(String title, String header, String text) {
        Alert dialog = new Alert(Alert.AlertType.ERROR);
        dialog.setTitle(ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()).getString(title));
        dialog.setHeaderText(ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()).getString(header));
        dialog.setContentText(ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()).getString(text));
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.showAndWait();
    }
    
    /**
     * Error message popup dialog
     *
     * @param msg
     */
    private void infoAlert(String title, String header) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle(ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()).getString(title));
        dialog.setHeaderText(ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()).getString(header));
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.showAndWait();
    }
    
    /**
     * This method is called by the FXMLLoader when initialization is complete
     */
    @FXML 
    void initialize() {
        this.attachments = new ArrayList<File>();
        this.formFXBean = new FormFXBean();
        this.HTMLEditorBean = new HTMLEditorFXBean();
        this.sendReceive = new MailSendingReceiving();
        
        Bindings.bindBidirectional(this.toField.textProperty(), formFXBean.toProperty());
        Bindings.bindBidirectional(this.ccField.textProperty(), formFXBean.ccProperty());
        Bindings.bindBidirectional(this.bccField.textProperty(), formFXBean.bccProperty());
        Bindings.bindBidirectional(this.subjectField.textProperty(), formFXBean.subjectProperty());
    }
}

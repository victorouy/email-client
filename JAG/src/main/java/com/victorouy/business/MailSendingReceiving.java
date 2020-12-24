/**
 * Here is your documentation on Jodd
 * https://jodd.org/email/index.html
 */
package com.victorouy.business;

import com.victorouy.exceptions.AbsentEmailAddressException;
import com.victorouy.exceptions.InvalidEmailAddressException;
import com.victorouy.exceptions.SessionFailureException;
import com.victorouy.properties.MailConfigBean;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Flags;

import jodd.mail.EmailFilter;
import jodd.mail.Email;
import jodd.mail.EmailAttachment;
import jodd.mail.ImapServer;
import jodd.mail.MailServer;
import jodd.mail.RFC2822AddressParser;
import jodd.mail.ReceiveMailSession;
import jodd.mail.ReceivedEmail;
import jodd.mail.SendMailSession;
import jodd.mail.SmtpServer;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Java class using Jodd to send and receive Email
 *
 * @author Victor Ouy   1739282
 *
 */
public class MailSendingReceiving {
    
    private final static Logger LOG = LoggerFactory.getLogger(MailSendingReceiving.class);
    
    /**
     * Standard send routine for Jodd using SmtpServer
     * 
     * @throws InvalidEmailAddressException from calling handleRecepients method
     * @throws AbsentEmailAddressException from calling handleRecepients method
     * @throws SessionFailureException if exception at SendMailSession
     * @throws FileNotFoundException when attachment to email is not found
     * @param sendConfig
     * @param toAddresses
     * @param ccAddresses
     * @param bccAddresses
     * @param subject
     * @param textMsg
     * @param htmlMsg
     * @param embAttachments
     * @param regAttachments
     * @return the Email object if all checks and validations are correct, 
     *         null if SendMailSessionsession session error
     */
    public Email sendEmail(MailConfigBean sendConfig, ArrayList<String> toAddresses, ArrayList<String> ccAddresses, ArrayList<String> bccAddresses, String subject, String textMsg, String htmlMsg, 
            ArrayList<File> embAttachments, ArrayList<File> regAttachments) throws InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException, FileNotFoundException {
        if (checkMailConfigBean(sendConfig)) {
            SmtpServer smtpServer = createSmtpServer(sendConfig);

            Email email = Email.create().from(sendConfig.getUserEmailAddress());
            if (handleRecipients(toAddresses, ccAddresses, bccAddresses, email)) {
                createEmailSubMsg(subject, textMsg, htmlMsg, email);
                handleEmbAttachment(embAttachments, email);
                handleRegAttachment(regAttachments, email);
                LOG.info("Jodd Email created");
            }
            else {
                // Will throw exception if there are no valid recipients to send to
                throw new AbsentEmailAddressException("No recipient to send email to");
            }

            try (SendMailSession session = smtpServer.createSession()) {
                session.open();
                // Set date/time right before sending to get most accurate time
                email.currentSentDate();
                session.sendMail(email);
                LOG.info("Email sent");
                return email;
            }
            catch (Exception e) {
                throw new SessionFailureException("Failure in sending email");
            }
        }
        else {
            throw new InvalidEmailAddressException("The sendConfig email address is invalid or null");
        }
    }
    
    /**
     * Standard receive routine for Jodd using an ImapServer
     * 
     * @param receiveConfig
     * @return an array of ReceivedEmail
     */
    public ReceivedEmail[] receiveEmail(MailConfigBean receiveConfig) throws InvalidEmailAddressException {
        if (checkMailConfigBean(receiveConfig)) {
            ImapServer imapServer = createImapServer(receiveConfig);
            try ( ReceiveMailSession session = imapServer.createSession()) {
                session.open();
                ReceivedEmail[] receivedEmails =  session.receiveEmailAndMarkSeen(EmailFilter.filter().flag(Flags.Flag.SEEN, false));
                if (emptyReceivedEmail(receivedEmails)) {
                    return null;
                }
                LOG.info("ReceivedEmail is valid");
                return receivedEmails;
            }
        }
        else {
            throw new InvalidEmailAddressException("The receiveConfig email address is invalid or null");
        }
    }
    
    /**
     * This uses multiple methods from sending email(very similar), however it simply returns the created saved email 
     * instead of creating one and sending one. It also avoid multiple exceptions since it is simply used as a draft email
     * 
     * @param saveConfig
     * @param toAddresses
     * @param ccAddresses
     * @param bccAddresses
     * @param subject
     * @param textMsg
     * @param htmlMsg
     * @param embAttachments
     * @param regAttachments
     * @return
     * @throws FileNotFoundException 
     */
    public Email saveEmail(MailConfigBean saveConfig, ArrayList<String> toAddresses, ArrayList<String> ccAddresses, ArrayList<String> bccAddresses, String subject, String textMsg, String htmlMsg, 
            ArrayList<File> embAttachments, ArrayList<File> regAttachments) throws FileNotFoundException {
        Email email;
        email = Email.create().from(saveConfig.getUserEmailAddress());
        try {
            handleRecipients(toAddresses, ccAddresses, bccAddresses, email);
        } catch (InvalidEmailAddressException ex) {
            // Will not need to catch this when saving, will do when sending
        } catch (AbsentEmailAddressException ex) {
            // Will not need to catch this when saving, will do when sending
        }
        createEmailSubMsg(subject, textMsg, htmlMsg, email);
        handleEmbAttachment(embAttachments, email);
        handleRegAttachment(regAttachments, email);
        LOG.info("Jodd Email saved");
        return email;
    }
    
    /**
     * Checks if MailConfigBean object is valid
     * 
     * @param emailInfo
     * @return true if emailInfo is not null and valid, false otherwise
     */
    public boolean checkMailConfigBean(MailConfigBean emailInfo) {
        if (emailInfo == null || !checkEmailAddress(emailInfo.getUserEmailAddress())) {
            return false;
        }
        return true;
    }
    
    public boolean checkValidConfigBeanFields(MailConfigBean emailInfo) {
        SmtpServer smtpServer = createSmtpServer(emailInfo);
        try (SendMailSession session = smtpServer.createSession()) {
            session.open();
        }
        catch (Exception e) {
            return false;
        }
        
        ImapServer imapServer = createImapServer(emailInfo);
        try ( ReceiveMailSession session = imapServer.createSession()) {
            session.open();
        }
        catch (Exception e) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Creates and returns StmpServer
     * 
     * @return the SmtpServer
     */
    private SmtpServer createSmtpServer(MailConfigBean emailInfo) {
        return MailServer.create()
                .ssl(true)
                .host(emailInfo.getUrlSMTP())
                .auth(emailInfo.getUserEmailAddress(), emailInfo.getPwdEmailAddress())
                .buildSmtpMailServer();
    }
    
    /**
     * Creates and returns ImapServer
     * 
     * @return the ImapServer
     */
    private ImapServer createImapServer(MailConfigBean emailInfo) {
        return MailServer.create()
                .host(emailInfo.getUrlIMAP())
                .ssl(true)
                .auth(emailInfo.getUserEmailAddress(), emailInfo.getPwdEmailAddress())
                //.debugMode(true)
                .buildImapMailServer();
    }
    
    /**
     * Checks To/CC/BCC forms of sending addresses using helper methods. If all 3 handling
     * methods return false, this method will throw AbsentEmailAddressException
     *
     * @throws InvalidEmailAddressException from calling helper/handle method
     * @throws AbsentEmailAddressException if all To/CC/BCC recipient forms returns false
     * @param toAddresses
     * @param ccAddresses
     * @param bccAddresses
     * @param email
     */
    private boolean handleRecipients(ArrayList<String> toAddresses, ArrayList<String> ccAddresses, ArrayList<String> bccAddresses, Email email) throws InvalidEmailAddressException, AbsentEmailAddressException {
        boolean handledTo = handleTo(toAddresses, email);
        boolean handledCC = handleCC(ccAddresses, email);
        boolean handledBCC = handleBCC(bccAddresses, email);
        return handledTo || handledCC || handledBCC;
    }
    
    /**
     * Assigns the to addresses to the email object using helper methods to 
     * check if valid
     *
     * @throws InvalidEmailAddressException if an address is invalid
     * @param toAddresses
     * @param email
     * @return true if all toAddress String are valid addresses, false otherwise
     */
    private boolean handleTo(ArrayList<String> toAddresses, Email email) throws InvalidEmailAddressException {
        boolean validSendRecipient = false;
        // Returns false if ArrayList object is invalid
        if (isArrayListNull(toAddresses) || toAddresses.isEmpty()) {
            return validSendRecipient;
        }
        for (String toAddress : toAddresses) {
            if (checkEmailAddress(toAddress)) {
                validSendRecipient = true;
                email.to(toAddress);
            }
        }
        return validSendRecipient;
    }
    
    /**
     * Assigns the cc addresses to the email object using helper methods to 
     * check if valid
     *
     * @throws InvalidEmailAddressException if an address is invalid
     * @param ccAddresses
     * @param email
     * @return true if all ccAddress String are valid addresses, false otherwise
     */
    private boolean handleCC(ArrayList<String> ccAddresses, Email email) throws InvalidEmailAddressException {
        boolean validSendRecipient = false;
        // Returns false if ArrayList object is invalid
        if (isArrayListNull(ccAddresses) || ccAddresses.isEmpty()) {
            return validSendRecipient;
        }
        for (String ccAddress : ccAddresses) {
            if (checkEmailAddress(ccAddress)) {
                validSendRecipient = true;
                email.cc(ccAddress);
            }
        }
        return validSendRecipient;
    }
    
    /**
     * Assigns the bcc addresses to the email object using helper methods to 
     * check if valid
     *
     * @throws InvalidEmailAddressException if an address is invalid
     * @param bccAddresses
     * @param email
     * @return true if all toAddress String are valid addresses, false otherwise
     */
    private boolean handleBCC(ArrayList<String> bccAddresses, Email email) throws InvalidEmailAddressException {
        boolean validSendRecipient = false;
        // Returns false if ArrayList object is invalid
        if (isArrayListNull(bccAddresses) || bccAddresses.isEmpty()) {
            return validSendRecipient;
        }
        for (String bccAddress : bccAddresses) {
            if (checkEmailAddress(bccAddress)) {
                validSendRecipient = true;
                email.bcc(bccAddress);
            }
        }
        return validSendRecipient;
    }
    
    /**
     * Assign subject and calls method to assign messages to null
     *
     * @param email
     * @param subject
     * @param textMsg
     * @param htmlMsg
     */
    private void createEmailSubMsg(String subject, String textMsg, String htmlMsg, Email email) {
        if (!isStringNull(subject)) {
            email.subject(subject);
        }
        // If handleMsgs returns false, assigns empty string to email message to avoid error
        if (!handleMsgs(email, textMsg, htmlMsg)) {
            email.textMessage("");
        }
    }
    
    /**
     * Assigns text and html messages to email. Checks if there is any message 
     * to attach to email
     *
     * @param email
     * @param textMsg
     * @param htmlMsg
     * @return true if any valid message has been assigned to email, false otherwise
     */
    private boolean handleMsgs(Email email, String textMsg, String htmlMsg) {
        // handledMsg false if no messages are to be sent
        boolean handledMsg = false;
        if (!isStringNull(textMsg)) {
            email.textMessage(textMsg);
            handledMsg = true;
        }
        if (!isStringNull(htmlMsg)) {
            email.htmlMessage(htmlMsg);
            handledMsg = true;
        }
        return handledMsg;
    }
    
    /**
     * Assigns the embedded attachments to the email object using helper methods
     * to check if valid
     *
     * @throws FileNotFoundException when attachment to email is invalid
     * @param embAttachments
     * @param email
     */
    private void handleEmbAttachment(ArrayList<File> embAttachments, Email email) throws FileNotFoundException {
        if (checkAttachments(embAttachments)) {
            for (File attachment : embAttachments) {
                if (!checkFile(attachment)) {
                    throw new FileNotFoundException("Attachment is not valid");
                }
                email.embeddedAttachment(EmailAttachment.with().content(attachment));
            }
        }
    }
    
    /**
     * Assigns the regular attachments(not embedded) to the email object using 
     * helper methods to check if valid
     *
     * @throws FileNotFoundException when attachment to email is invalid
     * @param regAttachments
     * @param email
     */
    private void handleRegAttachment(ArrayList<File> regAttachments, Email email) throws FileNotFoundException {
        if (checkAttachments(regAttachments)) {
            for (File attachment : regAttachments) {
                if (!checkFile(attachment)) {
                    throw new FileNotFoundException("Attachment is not valid");
                }
                email.attachment(EmailAttachment.with().content(attachment));
            }
        }
    }
    
    /**
     * Checks if ArrayList of any type is null
     * 
     * @param arrayList of any type
     * @return true if ArrayList is null, false if not
     */
    private boolean isArrayListNull(ArrayList<?> arrayList) {
        return arrayList == null;
    }
    
    /**
     * CHecks if String is null
     * 
     * @param string
     * @return true if String is null, false if not
     */
    private boolean isStringNull(String string) {
        return string == null;
    }
    
    /**
     * Checks if emailAddress is not null and valid
     * 
     * @param emailAddress
     * @return true if isStringNull(emailAddress) is false and 
     *         RFC2822AddressParser is not null, false if not
     */
    private boolean checkEmailAddress(String emailAddress) {
        return !isStringNull(emailAddress) && RFC2822AddressParser.STRICT.parseToEmailAddress(emailAddress) != null;
    }
    
    /**
     * Checks if File ArrayList is not null and not empty
     * 
     * @param attachments
     * @return true if isArrayListNull(attachments) and attachments.isEmpty() is 
     *         false, false if not
     */
    private boolean checkAttachments(ArrayList<File> attachments) {
        return !isArrayListNull(attachments) && !attachments.isEmpty();
    }
    
    /**
     * Checks if file is valid
     * 
     * @param file
     * @return false if file does not exists or is a directory, true otherwise
     */
    private boolean checkFile(File file) {
        if (!file.exists() || file.isDirectory()) {
            return false;
        }
        return true;
    }
    
    /**
     * Check if ReceivedEmail array is empty
     * 
     * @param receivedEmails
     * @return true if empty, false otherwise
     */
    private boolean emptyReceivedEmail(ReceivedEmail[] receivedEmails) {
        if (receivedEmails.length == 0) {
            return true;
        }
        return false;
    }
}
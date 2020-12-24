/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.victorouy.test;

import com.victorouy.business.MailSendingReceiving;
import com.victorouy.exceptions.AbsentEmailAddressException;
import com.victorouy.exceptions.DeleteMandatoryFolderException;
import com.victorouy.exceptions.FolderNameAlreadyExistsException;
import com.victorouy.exceptions.ForbiddenEmailEditAttempException;
import com.victorouy.exceptions.ForbiddenFolderMoveException;
import com.victorouy.exceptions.InvalidEmailAddressException;
import com.victorouy.exceptions.InvalidFolderNameException;
import com.victorouy.exceptions.SessionFailureException;
import com.victorouy.persistence.EmailDAO;
import com.victorouy.persistence.EmailDAOImpl;
import com.victorouy.properties.EmailDataBean;
import com.victorouy.properties.MailConfigBean;
import com.victorouy.testlogger.TestMethodLogger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import jodd.mail.Email;
import jodd.mail.ReceivedEmail;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for EmailDAOImpl CRUD methods
 * 
 * NOTE: Test has been altered and ignored due to confidentiality purposes on a public github
 *
 * @author Victor Ouy    1739282
 */
@Ignore
public class TestEmailDAOImpl {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    // Rule implemented to log information of every test method
    @Rule
    public TestMethodLogger testMethodLogger = new TestMethodLogger();
    
    private final static Logger LOG = LoggerFactory.getLogger(MailSendingReceiving.class);
    
    private MailSendingReceiving mailSendReceive;
    private MailConfigBean mailBeanConfig;
    private ArrayList<String> toAddresses;
    private ArrayList<String> ccAddresses;
    private ArrayList<String> bccAddresses;
    private ArrayList<File> embAttachments;
    private ArrayList<File> regAttachments;
    
    /*
     * Helper method used to sleep program for 3 seconds
    */
    private void sleepThreeSeconds() {
        try {
            Thread.sleep(3000);
        } 
        catch (InterruptedException e) {
            LOG.error("Threaded sleep failed", e);
        }
    }
    
    /**
     * Instantiates instance variables before every test method
     * 
     * @throws InvalidEmailAddressException
     * @throws AbsentEmailAddressException
     * @throws SessionFailureException
     * @throws FileNotFoundException 
     */
    @Before
    public void assigningTestInstanceVars() throws InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException, FileNotFoundException {
        seedDatabase();
        
        this.toAddresses = new ArrayList<String>();
        this.ccAddresses = new ArrayList<String>();
        this.bccAddresses = new ArrayList<String>();
        this.embAttachments = new ArrayList<File>();
        this.regAttachments = new ArrayList<File>();
        this.mailSendReceive = new MailSendingReceiving();
        
        this.regAttachments.add(new File("attachment1.jpg"));
        this.embAttachments.add(new File("attachment2.jpg"));
    }
    
    /**
     * Test if email sent is stored into database using createSendEmail method
     * 
     * @throws SQLException
     * @throws IOException
     * @throws InvalidEmailAddressException
     * @throws AbsentEmailAddressException
     * @throws SessionFailureException 
     */
//    @Ignore
    @Test (timeout = 30000)
    public void testCreateSendEmail() throws SQLException, IOException, InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException {
        Email sentEmail = mailSendReceive.sendEmail(mailBeanConfig, toAddresses, ccAddresses, bccAddresses, "Test subject", "text message", "html msg", regAttachments, null);
        EmailDataBean emailDataBean = new EmailDataBean(-1, -1, null, sentEmail);
        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
        emailDAO.createSendEmail(emailDataBean);
        EmailDataBean emailDataBeanTemp = emailDAO.findByID(emailDataBean.getEmailID());
        
        assertEquals("A sent email record was not created", emailDataBean, emailDataBeanTemp);
    }
    
    /**
     * Test if sent email is in SENT folder
     * 
     * @throws SQLException
     * @throws IOException
     * @throws InvalidEmailAddressException
     * @throws AbsentEmailAddressException
     * @throws SessionFailureException 
     */
//    @Ignore
    @Test (timeout = 30000)
    public void testCreateSendEmailInSent() throws SQLException, IOException, InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException {
        Email sentEmail = mailSendReceive.sendEmail(mailBeanConfig, toAddresses, ccAddresses, bccAddresses, "Test subject", "text message", "html msg", null, null);
        EmailDataBean emailDataBean = new EmailDataBean(-1, -1, null, sentEmail);
        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
        emailDAO.createSendEmail(emailDataBean);
        
        // 2 represents SENT folderkey
        assertEquals("A sent email record was not created in 'SENT'", 2, emailDataBean.getFolderKey());
    }
    
    /**
     * Test that received emails is stored into database with createReceivedEmail method
     * 
     * @throws SQLException
     * @throws IOException
     * @throws InvalidEmailAddressException
     * @throws AbsentEmailAddressException
     * @throws SessionFailureException 
     */
//    @Ignore
    @Test (timeout = 30000)
    public void testCreateReceivedEmail() throws SQLException, IOException, InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException {
        this.toAddresses.add("project.username01@gmail.com");
        mailSendReceive.sendEmail(mailBeanConfig, toAddresses, null, null, "Test subject receive 1", null, "html msg", null, null);
        mailSendReceive.sendEmail(mailBeanConfig, toAddresses, null, null, "Test subject receive 2", null, "html msg", null, null);
        sleepThreeSeconds();
        ReceivedEmail[] receivedEmails = mailSendReceive.receiveEmail(mailBeanConfig);
        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
        List<EmailDataBean> receiveEmailDataBeans = emailDAO.createReceivedEmail(receivedEmails);
        
        boolean receivedEmailsCreated = true;
        for (EmailDataBean emailData : receiveEmailDataBeans) {
            EmailDataBean emailDataBeanTemp = emailDAO.findByID(emailData.getEmailID());
            // 1 represents INBOX folderkey
            if (!emailDataBeanTemp.equals(emailData) && emailData.getFolderKey() == 1) {
                receivedEmailsCreated = false;
            }
        }
        assertTrue("A received email record was not created", receivedEmailsCreated);
    }
    
    /**
     * Test if email is "saved" to databaseCreateSaveEmail method
     * 
     * @throws SQLException
     * @throws IOException
     * @throws InvalidEmailAddressException
     * @throws AbsentEmailAddressException
     * @throws SessionFailureException 
     */
//    @Ignore
    @Test (timeout = 30000)
    public void testCreateSaveEmail() throws SQLException, IOException, InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException {
        Email saveEmail = Email.create().from("project.username01@gmail.com")
                .to("project.username03@gmail.com")
                .subject("DRAFT email")
                .textMessage("saved me");
        EmailDataBean emailDataBean = new EmailDataBean(-1, -1, null, saveEmail);
        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
        emailDAO.createSaveEmail(emailDataBean);
        EmailDataBean emailDataBeanTemp = emailDAO.findByID(emailDataBean.getEmailID());
        
        assertEquals("A sent email record was not created", emailDataBean, emailDataBeanTemp);
    }
    
    /**
     * Test to see if saved email is in "DRAFT" folder
     * 
     * @throws SQLException
     * @throws IOException
     * @throws InvalidEmailAddressException
     * @throws AbsentEmailAddressException
     * @throws SessionFailureException 
     */
//    @Ignore
    @Test (timeout = 30000)
    public void testCreateSaveEmailInDraft() throws SQLException, IOException, InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException {
        Email saveEmail = mailSendReceive.sendEmail(mailBeanConfig, toAddresses, ccAddresses, bccAddresses, "Test subject", "text message", "html msg", null, null);
        EmailDataBean emailDataBean = new EmailDataBean(-1, -1, null, saveEmail);
        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
        emailDAO.createSaveEmail(emailDataBean);
        
        // 3 represents DRAFT folderkey
        assertEquals("A sent email record was not created", 3, emailDataBean.getFolderKey());
    }
    
    /**
     * Test if folder is created into the database using CreateFolder method
     * 
     * @throws SQLException
     * @throws IOException
     * @throws FolderNameAlreadyExistsException
     * @throws InvalidFolderNameException 
     */
//    @Ignore
//    @Test (timeout = 30000)
//    public void testCreateFolder() throws SQLException, IOException, FolderNameAlreadyExistsException, InvalidFolderNameException {
//        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
//        emailDAO.createFolder("IMPORTANT");
//        boolean folderCreated = false;
//        for (String folder : emailDAO.findAllFolderNames()) {
//            // If a folder is IMPORTANT, then createFolder worked
//            if (folder.equals("IMPORTANT")) {
//                folderCreated = true;
//            }
//        }
//        assertTrue("Folder 'IMPORTANT' not created", folderCreated);
//    }
    
    /**
     * Test if FolderNameAlreadyExistsException is thrown when inputting "INBOX" in createFolder method
     * 
     * @throws SQLException
     * @throws IOException
     * @throws FolderNameAlreadyExistsException
     * @throws InvalidFolderNameException 
     */
    @Ignore
    @Test (timeout = 30000)
    public void testInboxFolderNameAlreadyExistsCreateFolder() throws SQLException, IOException, FolderNameAlreadyExistsException, InvalidFolderNameException {
        thrown.expect(FolderNameAlreadyExistsException.class);
        
        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
        emailDAO.createFolder("INBOX");
    }
    
    /**
     * Test if FolderNameAlreadyExistsException is thrown when inputting "SENT" in createFolder method
     * 
     * @throws SQLException
     * @throws IOException
     * @throws FolderNameAlreadyExistsException
     * @throws InvalidFolderNameException 
     */
    @Ignore
    @Test (timeout = 30000)
    public void testSentFolderNameAlreadyExistsCreateFolder() throws SQLException, IOException, FolderNameAlreadyExistsException, InvalidFolderNameException {
        thrown.expect(FolderNameAlreadyExistsException.class);
        
        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
        emailDAO.createFolder("SENT");
    }
    
    /**
     * Test if FolderNameAlreadyExistsException is thrown when inputting "DRAFT" in createFolder method
     * 
     * @throws SQLException
     * @throws IOException
     * @throws FolderNameAlreadyExistsException
     * @throws InvalidFolderNameException 
     */
    @Ignore
    @Test (timeout = 30000)
    public void testDraftFolderNameAlreadyExistsCreateFolder() throws SQLException, IOException, FolderNameAlreadyExistsException, InvalidFolderNameException {
        thrown.expect(FolderNameAlreadyExistsException.class);
        
        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
        emailDAO.createFolder("DRAFT");
    }
    
    /**
     * Test if InvalidFolderNameException is throw when entering null into createFolder method
     * 
     * @throws SQLException
     * @throws IOException
     * @throws FolderNameAlreadyExistsException
     * @throws InvalidFolderNameException 
     */
    @Ignore
    @Test (timeout = 30000)
    public void testNullInvalidFolderNameCreateFolder() throws SQLException, IOException, FolderNameAlreadyExistsException, InvalidFolderNameException {
        thrown.expect(InvalidFolderNameException.class);
        
        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
        emailDAO.createFolder(null);
    }
    
    /**
     * Test if InvalidFolderNameException is throw when entering "" into createFolder method
     * 
     * @throws SQLException
     * @throws IOException
     * @throws FolderNameAlreadyExistsException
     * @throws InvalidFolderNameException 
     */
    @Ignore
    @Test (timeout = 30000)
    public void testEmptyInvalidFolderNameCreateFolder() throws SQLException, IOException, FolderNameAlreadyExistsException, InvalidFolderNameException {
        thrown.expect(InvalidFolderNameException.class);
        
        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
        emailDAO.createFolder("");
    }
    
    /**
     * Test if InvalidFolderNameException is throw when entering over 30 characters into createFolder method
     * 
     * @throws SQLException
     * @throws IOException
     * @throws FolderNameAlreadyExistsException
     * @throws InvalidFolderNameException 
     */
    @Ignore
    @Test (timeout = 30000)
    public void testOverLimitInvalidFolderNameCreateFolder() throws SQLException, IOException, FolderNameAlreadyExistsException, InvalidFolderNameException {
        thrown.expect(InvalidFolderNameException.class);
        
        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
        // Over varchar(30) characters
        emailDAO.createFolder("1234123412341324123412341234123412341234");
    }
    
    /**
     * Test if findAll method returns a list of EmailDataBean of all emails
     * 
     * @throws SQLException
     * @throws IOException 
     */
//    @Ignore
//    @Test (timeout = 30000)
//    public void testFindAll() throws SQLException, IOException {
//        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
//        List<EmailDataBean> emailDataBeans = emailDAO.findAll();
//        
//        assertEquals("FAILED: email record not deleted", 6, emailDataBeans.size());
//    }
    
    /**
     * Test if findById method returns the correct and accurate EmailDataBean
     * 
     * @throws SQLException
     * @throws IOException
     * @throws ParseException 
     */
//    @Ignore
    @Test (timeout = 30000)
    public void testFindById() throws SQLException, IOException, ParseException {
        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
        EmailDataBean emailDataBean = new EmailDataBean();
        emailDataBean.setEmailID(5);
        emailDataBean.setFolderKey(1);
        // Formatting the date according to have the same format
        SimpleDateFormat dateFormating = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date sentDate = dateFormating.parse("2020-10-01 09:34:21");
        emailDataBean.email.from("project.username02@gmail.com")
                .to("project.username01@gmail.com")
                .cc("project.username02@gmail.com")
                .subject("The subject")
                .textMessage("the text message")
                .htmlMessage("the html message")
                .sentDate(sentDate);
        EmailDataBean emailDataExpected = emailDAO.findByID(5);
        
        assertEquals("Did not find email by id", emailDataBean, emailDataExpected);
    }
    
    /**
     * Test if searching for subject returns correct EmailDataBeans
     * 
     * @throws SQLException
     * @throws IOException 
     */
//    @Ignore
    @Test (timeout = 30000)
    public void testSubjectFindSearchedEmail() throws SQLException, IOException {
        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
        List<EmailDataBean> searches = emailDAO.findSearchedEmail("o messag");
        searches.get(0).getEmailID();
        
        assertEquals("Search for email containing subject 'o messag' failed", 3, searches.get(0).getEmailID());
    }
    
    /**
     * Test if searching for from/to/cc/bcc address fields returns correct EmailDataBeans
     * 
     * @throws SQLException
     * @throws IOException 
     */
//    @Ignore
    @Test (timeout = 30000)
    public void testFromAndRecipientFindSearchedEmail() throws SQLException, IOException {
        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
        List<EmailDataBean> searches = emailDAO.findSearchedEmail("rname04@gm");
        
        assertEquals("Search for email containing from/cc/bcc/to 'rname04@gm' failed", 3, searches.size());
    }
    
    /**
     * Test that findByFolder returns correct List of EmailDataBeans 
     * 
     * @throws SQLException
     * @throws IOException 
     */
//    @Ignore
    @Test (timeout = 30000)
    public void testFindByFolder() throws SQLException, IOException {
        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
        List<EmailDataBean> emailsInFolder = emailDAO.findByFolder("SENT");
        
        assertEquals("Did not find emails in folder SENT", 4, emailsInFolder.size());
    }
    
    
    /**
     * Test that findAllFolderNames returns all folder names
     * 
     * @throws SQLException
     * @throws IOException 
     */
//    @Ignore
//    @Test (timeout = 30000)
//    public void testFindAllFolderNames() throws SQLException, IOException {
//        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
//        List<String> folders = emailDAO.findAllFolderNames();
//        boolean haveAllFolders = true;
//        for (String folder : folders) {
//            if (!folder.equals("SENT") && !folder.equals("INBOX") && !folder.equals("DRAFT")) {
//                haveAllFolders = false;
//            }
//        }
//        assertTrue("FindAllFolderNames does not retrieve all folders", haveAllFolders);
//    }
    
    /**
     * Test that draft email has been edited and saved back into database
     * 
     * @throws SQLException
     * @throws IOException
     * @throws InvalidEmailAddressException
     * @throws AbsentEmailAddressException
     * @throws SessionFailureException
     * @throws ForbiddenEmailEditAttempException 
     */
//    @Ignore
    @Test (timeout = 30000)
    public void testUpdateEditDraftEmail() throws SQLException, IOException, InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException, ForbiddenEmailEditAttempException {
        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
        EmailDataBean emailDataBean = emailDAO.findByID(6);
        emailDataBean.email.subject("CHANGED SUBJECT")
                .to("project.username04@gmail.com");
        emailDAO.updateEditDraftEmail(emailDataBean);
        EmailDataBean emailActual = emailDAO.findByID(emailDataBean.getEmailID());
        
        assertEquals("FindAllFolderNames does not retrieve all folders", emailActual, emailDataBean);
    }
    
    /**
     * 
     * 
     * @throws SQLException
     * @throws IOException
     * @throws InvalidEmailAddressException
     * @throws AbsentEmailAddressException
     * @throws SessionFailureException
     * @throws ForbiddenEmailEditAttempException 
     */
//    @Ignore
    @Test (timeout = 30000)
    public void testForbiddenEmailEditAttempUpdateEditDraftEmail() throws SQLException, IOException, InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException, ForbiddenEmailEditAttempException {
        thrown.expect(ForbiddenEmailEditAttempException.class);
        
        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
        // This emailDataBean is not a draft email so you cannot edit it
        EmailDataBean emailDataBean = emailDAO.findByID(5);
        emailDAO.updateEditDraftEmail(emailDataBean);
    }
    
    /**
     * Test that email has been moved to INBOX folder
     * 
     * @throws SQLException
     * @throws IOException
     * @throws ForbiddenFolderMoveException 
     */
//    @Ignore
    @Test (timeout = 30000)
    public void testUpdateMoveEmailFolder() throws SQLException, IOException, ForbiddenFolderMoveException {
        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
        EmailDataBean emailDataBean = emailDAO.findByID(2);
        emailDAO.updateMoveEmailFolder(emailDataBean, "INBOX");
        boolean folderChanged = false;
        if (emailDataBean.getFolderKey() == 1) {
            folderChanged = true;
        }
        
        assertTrue("Did not change email folder", folderChanged);
    }
    
    /**
     * Catch ForbiddenFolderMoveException when trying to move email into DRAFT folder
     * 
     * @throws SQLException
     * @throws IOException
     * @throws ForbiddenFolderMoveException 
     */
//    @Ignore
    @Test (timeout = 30000)
    public void testIntoDraftForbiddenFolderMoveUpdateMoveEmailFolder() throws SQLException, IOException, ForbiddenFolderMoveException {
        thrown.expect(ForbiddenFolderMoveException.class);
        
        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
        EmailDataBean emailDataBean = emailDAO.findByID(2);
        emailDAO.updateMoveEmailFolder(emailDataBean, "DRAFT");
    }
    
    /**
     * Catch ForbiddenFolderMoveException when trying to move email out of DRAFT folder
     * 
     * @throws SQLException
     * @throws IOException
     * @throws ForbiddenFolderMoveException 
     */
//    @Ignore
    @Test (timeout = 30000)
    public void testOutDraftForbiddenFolderMoveUpdateMoveEmailFolder() throws SQLException, IOException, ForbiddenFolderMoveException {
        thrown.expect(ForbiddenFolderMoveException.class);
        
        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
        EmailDataBean emailDataBean = emailDAO.findByID(6);
        emailDAO.updateMoveEmailFolder(emailDataBean, "INBOX");
    }
    
    /**
     * Test that drafted email has been sent in the database
     * 
     * @throws SQLException
     * @throws IOException
     * @throws ForbiddenEmailEditAttempException 
     */
//    @Ignore
    @Test (timeout = 30000)
    public void testUpdateSendDraftEmail() throws SQLException, IOException, ForbiddenEmailEditAttempException {
        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
        EmailDataBean emailDataBean = emailDAO.findByID(6);
        emailDataBean.email.subject("Going to send this now");
        emailDAO.updateSendDraftEmail(emailDataBean);
        EmailDataBean emailDataTemp = emailDAO.findByID(emailDataBean.getEmailID());
        
        assertEquals("Did not send drafted email", emailDataBean, emailDataTemp);
    }
    
    /**
     * Test that ForbiddenEmailEditAttempException is thrown when attempting to edit an email that is not in DRAFT folder
     * 
     * @throws SQLException
     * @throws IOException
     * @throws ForbiddenEmailEditAttempException 
     */
//    @Ignore
    @Test (timeout = 30000)
    public void testForbiddenEmailEditAttempUpdateSendDraftEmail() throws SQLException, IOException, ForbiddenEmailEditAttempException {
        thrown.expect(ForbiddenEmailEditAttempException.class);
        
        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
        EmailDataBean emailDataBean = emailDAO.findByID(3);
        emailDataBean.email.subject("Should fail since it is not an email in draft");
        emailDAO.updateSendDraftEmail(emailDataBean);
    }
    
    /**
     * Test that folderKey has been updated when sending draft email
     * 
     * @throws SQLException
     * @throws IOException
     * @throws ForbiddenEmailEditAttempException 
     */
//    @Ignore
    @Test (timeout = 30000)
    public void testFolderKeyUpdateSendDraftEmail() throws SQLException, IOException, ForbiddenEmailEditAttempException {
        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
        EmailDataBean emailDataBean = emailDAO.findByID(6);
        emailDataBean.email.subject("Going to send this now");
        emailDAO.updateSendDraftEmail(emailDataBean);
        
        assertEquals("Did updated folderkey when send drafted email", emailDataBean.getFolderKey(), 2);
    }
    
    /**
     * Test if email has been deleted in the database
     * 
     * @throws SQLException
     * @throws InvalidFolderNameException
     * @throws DeleteMandatoryFolderException
     * @throws FolderNameAlreadyExistsException
     * @throws InvalidEmailAddressException
     * @throws AbsentEmailAddressException
     * @throws SessionFailureException
     * @throws FileNotFoundException 
     */
//    @Ignore
    @Test (timeout = 30000)
    public void testDeleteEmail() throws SQLException, InvalidFolderNameException, DeleteMandatoryFolderException, FolderNameAlreadyExistsException, InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException, FileNotFoundException {
        Email sentEmail = mailSendReceive.sendEmail(mailBeanConfig, toAddresses, ccAddresses, bccAddresses, "Test subject", "text message", "html msg", null, null);
        EmailDataBean emailDataBean = new EmailDataBean(-1, -1, null, sentEmail);
        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
        emailDAO.createSendEmail(emailDataBean);
        int emailDeleted = emailDAO.deleteEmail(emailDataBean.getEmailID());
        
        assertEquals("Email record not deleted", 1, emailDeleted);
    }
    
    /**
     * Test if folder has been deleted in the database
     * 
     * @throws SQLException
     * @throws InvalidFolderNameException
     * @throws DeleteMandatoryFolderException
     * @throws FolderNameAlreadyExistsException 
     */
    @Ignore
    @Test (timeout = 30000)
    public void testDeleteFolder() throws SQLException, InvalidFolderNameException, DeleteMandatoryFolderException, FolderNameAlreadyExistsException {
        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
        emailDAO.createFolder("TEMP");
        int foldersDeleted = emailDAO.deleteFolder("TEMP");
        
        assertEquals("Folder record 'TEMP' not deleted", 1, foldersDeleted);
    }
    
    /**
     * Test if emails in deleted folder has also been deleted
     * 
     * @throws SQLException
     * @throws DeleteMandatoryFolderException
     * @throws FolderNameAlreadyExistsException
     * @throws InvalidFolderNameException
     * @throws ForbiddenFolderMoveException
     * @throws IOException
     * @throws SessionFailureException
     * @throws AbsentEmailAddressException
     * @throws FileNotFoundException
     * @throws InvalidEmailAddressException 
     */
    @Ignore
    @Test (timeout = 30000)
    public void testDeleteEmailsForDeleteFolder() throws SQLException, DeleteMandatoryFolderException, FolderNameAlreadyExistsException, InvalidFolderNameException, ForbiddenFolderMoveException, IOException, SessionFailureException, AbsentEmailAddressException, FileNotFoundException, InvalidEmailAddressException {
        Email sentEmail = mailSendReceive.sendEmail(mailBeanConfig, toAddresses, null, null, "Test subject", "text message", "html msg", null, null);
        EmailDataBean emailDataBean = new EmailDataBean(-1, -1, null, sentEmail);
        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
        emailDAO.createSendEmail(emailDataBean);
        int emailId = emailDataBean.getEmailID();
        
        emailDAO.createFolder("TEMP");
        emailDAO.updateMoveEmailFolder(emailDataBean, "TEMP");
        emailDAO.deleteFolder("TEMP");
        EmailDataBean emailDataBeanTemp = emailDAO.findByID(emailId);
        
        assertEquals("Email infolder 'TEMP' not deleted", emailDataBeanTemp.getEmailID(), -1);
    }
    
    /**
     * Test that DeleteMandatoryFolderException will be thrown if attempting to delete "INBOX" folder
     * 
     * @throws SQLException
     * @throws DeleteMandatoryFolderException 
     */
//    @Ignore
    @Test (timeout = 30000)
    public void testDeleteInboxFolderExceptionForDeleteFolder() throws SQLException, DeleteMandatoryFolderException {
        thrown.expect(DeleteMandatoryFolderException.class);
        
        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
        emailDAO.deleteFolder("INBOX");
    }
    
    /**
     * Test that DeleteMandatoryFolderException will be thrown if attempting to delete "SENT" folder
     * 
     * @throws SQLException
     * @throws DeleteMandatoryFolderException 
     */
//    @Ignore
    @Test (timeout = 30000)
    public void testDeletetSentFolderExceptionForDeleteFolder() throws SQLException, DeleteMandatoryFolderException {
        thrown.expect(DeleteMandatoryFolderException.class);
        
        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
        emailDAO.deleteFolder("SENT");
    }
    
    /**
     * Test that DeleteMandatoryFolderException will be thrown if attempting to delete "DRAFT" folder
     * 
     * @throws SQLException
     * @throws DeleteMandatoryFolderException 
     */
//    @Ignore
    @Test (timeout = 30000)
    public void testDeletetDraftFolderExceptionForDeleteFolder() throws SQLException, DeleteMandatoryFolderException {
        thrown.expect(DeleteMandatoryFolderException.class);
        
        EmailDAO emailDAO = new EmailDAOImpl(mailBeanConfig);
        emailDAO.deleteFolder("DRAFT");
    }
    
    /**
     * The database is recreated before each test. If the last test is
     * destructive then the database is in an unstable state. @AfterClass is
     * called just once when the test class is finished with by the JUnit
     * framework. It is instantiating the test class anonymously so that it can
     * execute its non-static seedDatabase routine.
     */
    @AfterClass
    public static void seedAfterTestCompleted() {
        LOG.info("@AfterClass seeding");
        new TestEmailDAOImpl().seedDatabase();
    }
    
    /**
     * This routine recreates the database before every test. This makes sure
     * that a destructive test will not interfere with any other test. Does not
     * support stored procedures.
     *
     * This routine is courtesy of Bartosz Majsak, the lead Arquillian developer
     * at JBoss
     */
    private void seedDatabase() {
        LOG.info("Seeding Database");
        final String seedDataScript = loadAsString("createJAGTables.sql");
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/jagdb", "victorouy", "dawson")) {
            for (String statement : splitStatements(new StringReader(
                    seedDataScript), ";")) {
                connection.prepareStatement(statement).execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed seeding database", e);
        }
    }

    /**
     * The following methods support the seedDatabase method
     */
    private String loadAsString(final String path) {
        try (InputStream inputStream = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(path);
                Scanner scanner = new Scanner(inputStream);) {
            return scanner.useDelimiter("\\A").next();
        } catch (IOException e) {
            throw new RuntimeException("Unable to close input stream.", e);
        }
    }

    private List<String> splitStatements(Reader reader,
            String statementDelimiter) {
        final BufferedReader bufferedReader = new BufferedReader(reader);
        final StringBuilder sqlStatement = new StringBuilder();
        final List<String> statements = new LinkedList<>();
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || isComment(line)) {
                    continue;
                }
                sqlStatement.append(line);
                if (line.endsWith(statementDelimiter)) {
                    statements.add(sqlStatement.toString());
                    sqlStatement.setLength(0);
                }
            }
            return statements;
        } catch (IOException e) {
            throw new RuntimeException("Failed parsing sql", e);
        }
    }

    private boolean isComment(final String line) {
        return line.startsWith("--") || line.startsWith("//")
                || line.startsWith("/*");
    }
}

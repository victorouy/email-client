package com.victorouy.test;

import com.victorouy.business.MailSendingReceiving;
import com.victorouy.exceptions.AbsentEmailAddressException;
import com.victorouy.exceptions.InvalidEmailAddressException;
import com.victorouy.exceptions.SessionFailureException;
import com.victorouy.properties.MailConfigBean;
import com.victorouy.testlogger.TestMethodLogger;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import jodd.mail.Email;
import jodd.mail.ReceivedEmail;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit test class for MailSendingReceiving java class
 * 
 * NOTE: Test has been altered and ignored due to confidentiality purposes on a public github
 * 
 * @author Victor Ouy   1739282
 */
@Ignore
public class TestMailSendingReceiving {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    // Rule implemented to log information of every test method
    @Rule
    public TestMethodLogger testMethodLogger = new TestMethodLogger();
    
    private final static Logger LOG = LoggerFactory.getLogger(MailSendingReceiving.class);
    
    private MailConfigBean sendConfig;
    private MailConfigBean receiveConfig;
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
    
    /*
     * Before method:
     * Instantiates instance variables before every test method
    */
    @Before
    public void assigningTestInstanceVars() {
        this.toAddresses = new ArrayList<String>();
        this.ccAddresses = new ArrayList<String>();
        this.bccAddresses = new ArrayList<String>();
        this.embAttachments = new ArrayList<File>();
        this.regAttachments = new ArrayList<File>();
    }
    
    /*
     * Test method:
     * Compares email from recipient to see if the same as received
    */
    @Test (timeout = 30000)
    public void testFromRecipientReceivedEmail() throws InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException, FileNotFoundException {
        String subject = "from recipient";
        
        MailSendingReceiving mailSendReceive = new MailSendingReceiving();
        Email sentEmail = mailSendReceive.sendEmail(this.sendConfig, this.toAddresses, 
                null, null, subject, null, null, null, null);
        sleepThreeSeconds();
        ReceivedEmail[] receivedEmails = mailSendReceive.receiveEmail(this.receiveConfig);
        
        assertEquals("FAILED: not the same recipient sent from", sentEmail.from().toString(), receivedEmails[0].from().toString());
    }
    
    /*
     * Test method:
     * Compares email To recipient to see if the same as received
    */
    @Test (timeout = 30000)
    public void testToRecipientReceivedEmail() throws InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException, FileNotFoundException {
        this.toAddresses.add("project.username02@gmail.com");
        String subject = "to To recipient";
        
        MailSendingReceiving mailSendReceive = new MailSendingReceiving();
        Email sentEmail = mailSendReceive.sendEmail(this.sendConfig, this.toAddresses, 
                null, null, subject, null, null, null, null);
        sleepThreeSeconds();
        ReceivedEmail[] receivedEmails = mailSendReceive.receiveEmail(this.receiveConfig);
        
        assertEquals("FAILED: not the same recipient sent To", sentEmail.to()[0].toString(), receivedEmails[0].to()[0].toString());
    }
    
    /*
     * Test method:
     * Compares email CC recipient to see if the same as received
    */
    @Test (timeout = 30000)
    public void testCCRecipientReceivedEmail() throws InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException, FileNotFoundException {
        this.ccAddresses.add("project.username02@gmail.com");
        String subject = "to CC recipient";
        
        MailSendingReceiving mailSendReceive = new MailSendingReceiving();
        Email sentEmail = mailSendReceive.sendEmail(this.sendConfig, null, 
                this.ccAddresses, null, subject, null, null, null, null);
        sleepThreeSeconds();
        ReceivedEmail[] receivedEmails = mailSendReceive.receiveEmail(this.receiveConfig);
        
        assertEquals("FAILED: not the same recipient sent CC", sentEmail.cc()[0].toString(), receivedEmails[0].cc()[0].toString());
    }
    
    /*
     * Test method:
     * Compares email BCC recipient to see if the same as received
    */
    @Test (timeout = 30000)
    public void testBCCRecipientReceivedEmail() throws InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException, FileNotFoundException {
        this.bccAddresses.add("project.username02@gmail.com");
        String subject = "to BCC recipient";
        
        MailSendingReceiving mailSendReceive = new MailSendingReceiving();
        Email sentEmail = mailSendReceive.sendEmail(this.sendConfig, null, 
                null, this.bccAddresses, subject, null, null, null, null);
        sleepThreeSeconds();
        ReceivedEmail[] receivedEmails = mailSendReceive.receiveEmail(this.receiveConfig);
        
        assertEquals("FAILED: not the same recipient sent BCC", sentEmail.bcc()[0].toString(), this.receiveConfig.getUserEmailAddress());
    }
    
    /*
     * Test method:
     * Tests that email still sends to valid recipient if there are invalid recipients,
     * as long as at least one recipient is valid
    */
    @Test (timeout = 30000)
    public void testSendsIfOneValid() throws InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException, FileNotFoundException {
        this.toAddresses.add("lfjsaduin32n");
        this.ccAddresses.add(null);
        this.ccAddresses.add("project.username02@gmail.com");
        this.bccAddresses.add("sjanfon31ui");
        String subject = "Send if at least one is valid";
        
        MailSendingReceiving mailSendReceive = new MailSendingReceiving();
        Email sentEmail = mailSendReceive.sendEmail(this.sendConfig, this.toAddresses, 
                this.ccAddresses, this.bccAddresses, subject, null, null, null, null);
        sleepThreeSeconds();
        ReceivedEmail[] receivedEmails = mailSendReceive.receiveEmail(this.receiveConfig);
        
        assertEquals("FAILED: email not sent", subject, receivedEmails[0].subject());
    }
    
    /*
     * Test method:
     * Sending an email to multiple email addresses
    */
    @Test (timeout = 30000)
    public void testSendToMultipleAddress() throws InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException, FileNotFoundException {
        this.toAddresses.add("project.username01@gmail.com");
        this.toAddresses.add("project.username02@gmail.com");
        this.ccAddresses.add("project.username03@gmail.com");
        this.bccAddresses.add("project.username04@gmail.com");
        String subject = "Sending to multiple people";
        
        MailSendingReceiving mailSendReceive = new MailSendingReceiving();
        Email sentEmail = mailSendReceive.sendEmail(this.sendConfig, this.toAddresses, 
                this.ccAddresses, this.bccAddresses, subject, null, null, null, null);
        sleepThreeSeconds();
        ReceivedEmail[] receivedEmail = mailSendReceive.receiveEmail(this.receiveConfig);
        
        int numberOfRecipients = sentEmail.to().length + sentEmail.cc().length + sentEmail.bcc().length;
        assertEquals("FAILED: email did not send to 4 recipients", 4, numberOfRecipients);
    }
    
    /*
     * Test method:
     * Compares email subject to see if the same as received
    */
    @Test (timeout = 30000)
    public void testSubjectSentIsReceived() throws InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException, FileNotFoundException {
        this.toAddresses.add("project.username02@gmail.com");
        String subject = "Testing subject sent";
        
        MailSendingReceiving mailSendReceive = new MailSendingReceiving();
        Email sentEmail = mailSendReceive.sendEmail(this.sendConfig, this.toAddresses, 
                null, null, subject, null, null, null, null);
        sleepThreeSeconds();
        ReceivedEmail[] receivedEmails = mailSendReceive.receiveEmail(this.receiveConfig);

        assertEquals("FAILED: subject sent not the same as received", sentEmail.subject(), receivedEmails[0].subject());
    }
    
    /*
     * Test method:
     * Compares email text messages to see if the same as received
    */
    @Test (timeout = 30000)
    public void testMessageSentIsReceived() throws InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException, FileNotFoundException {
        this.toAddresses.add("project.username02@gmail.com");
        String message = "Sending this message";
        
        MailSendingReceiving mailSendReceive = new MailSendingReceiving();
        Email sentEmail = mailSendReceive.sendEmail(this.sendConfig, this.toAddresses, 
                null, null, null, message, null, null, null);
        sleepThreeSeconds();
        ReceivedEmail[] receivedEmails = mailSendReceive.receiveEmail(this.receiveConfig);

        assertEquals("FAILED: text message sent not the same as received", receivedEmails[0].messages().get(0).getContent().trim(), sentEmail.messages().get(0).getContent().trim());
    }
    
    /*
     * Test method:
     * Compares email html messages to see if the same as received
    */
    @Test (timeout = 30000)
    public void testHtmlSentIsReceived() throws InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException, FileNotFoundException {
        this.toAddresses.add("project.username02@gmail.com");
        String htmlMessage = "<p>HTML message in html form</p>";
        
        MailSendingReceiving mailSendReceive = new MailSendingReceiving();
        Email sentEmail = mailSendReceive.sendEmail(this.sendConfig, this.toAddresses, 
                null, null, null, null, htmlMessage, null, null);
        sleepThreeSeconds();
        ReceivedEmail[] receivedEmails = mailSendReceive.receiveEmail(this.receiveConfig);

        assertEquals("FAILED: html message sent not the same as received", sentEmail.messages().get(0).getContent().trim(), receivedEmails[0].messages().get(0).getContent().trim());
    }
    
    /*
     * Test method:
     * Checks if html message mime type received is "text/html"
    */
    @Test (timeout = 30000)
    public void testHtmlMessageIsHTMLString() throws InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException, FileNotFoundException {
        this.toAddresses.add("project.username02@gmail.com");
        String htmlMessage = "<p>HTML message in html form</p>";
        
        MailSendingReceiving mailSendReceive = new MailSendingReceiving();
        Email sentEmail = mailSendReceive.sendEmail(this.sendConfig, this.toAddresses, 
                null, null, null, null, htmlMessage, null, null);
        sleepThreeSeconds();
        ReceivedEmail[] receivedEmails = mailSendReceive.receiveEmail(this.receiveConfig);
        
        assertEquals("FAILED: not an hmtl message", sentEmail.messages().get(0).getMimeType(), "text/html");
    }
    
    /*
     * Test method:
     * Compares email embedded attachments name to see if the same as received
    */
    @Test (timeout = 30000)
    public void testEmbeddedAttachmentSentIsReceived() throws InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException, FileNotFoundException {
        this.toAddresses.add("project.username02@gmail.com");
        String htmlMsg = "<img width=100 height=100 id=\"1\" src=\"cid:attachment1.jpg\" / >";
        this.embAttachments.add(new File("attachment1.jpg"));
        
        MailSendingReceiving mailSendReceive = new MailSendingReceiving();
        Email sentEmail = mailSendReceive.sendEmail(this.sendConfig, this.toAddresses, 
                null, null, null, null, htmlMsg, this.embAttachments, null);
        sleepThreeSeconds();
        ReceivedEmail[] receivedEmails = mailSendReceive.receiveEmail(this.receiveConfig);
        
        assertEquals("FAILED: does not have the same embedded attachment name", sentEmail.attachments().get(0).getName(), receivedEmails[0].attachments().get(0).getName());
    }
    
    /*
     * Test method:
     * Compares email attachments name to see if the same as received
    */
    @Test (timeout = 30000)
    public void testAttachmentSentIsReceived() throws InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException, FileNotFoundException {
        this.toAddresses.add("project.username02@gmail.com");
        this.regAttachments.add(new File("attachment2.jpg"));
        
        MailSendingReceiving mailSendReceive = new MailSendingReceiving();
        Email sentEmail = mailSendReceive.sendEmail(this.sendConfig, this.toAddresses, 
                null, null, null, null, null, null, this.regAttachments);
        sleepThreeSeconds();
        ReceivedEmail[] receivedEmails = mailSendReceive.receiveEmail(this.receiveConfig);
        
        assertEquals("FAILED: does not have the same attachment name", sentEmail.attachments().get(0).getName(), receivedEmails[0].attachments().get(0).getName());
    }
    
    /*
     * Test method:
     * Should throw InvalidEmailAddressException as the MailConfigBean is invalid
    */
    @Test (timeout = 30000)
    public void testInvalidMailConfigForSendingEmail() throws InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException, FileNotFoundException {
        thrown.expect(InvalidEmailAddressException.class);
        
        this.toAddresses.add("project.username02@gmail.com");
        MailConfigBean invalidSendConfig = new MailConfigBean("smtp.gmail.com", "imap.gmail.com", "bob", "asdfjkl;");
        
        MailSendingReceiving mailSendReceive = new MailSendingReceiving();
        Email sentEmail = mailSendReceive.sendEmail(invalidSendConfig, this.toAddresses, 
                null, null, null, null, null, null, null);
    }
    
    /*
     * Test method:
     * Should throw AbsentEmailAddressException when entering all null recipients to send email to
    */
    @Test (timeout = 30000)
    public void testAbsentEmailAddressForNullSendingEmail() throws InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException, FileNotFoundException {
        thrown.expect(AbsentEmailAddressException.class);
        
        MailSendingReceiving mailSendReceive = new MailSendingReceiving();
        Email sentEmail = mailSendReceive.sendEmail(this.sendConfig, null, 
                null, null, null, null, null, null, null);
    }
    
    /*
     * Test method:
     * Should throw AbsentEmailAddressException when entering all empty recipients to send email to
    */
    @Test (timeout = 30000)
    public void testAbsentEmailAddressForEmptySendingEmail() throws InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException, FileNotFoundException {
        thrown.expect(AbsentEmailAddressException.class);
        
        MailSendingReceiving mailSendReceive = new MailSendingReceiving();
        // Did not assign anything to to/cc/bcc addresses, hence they are empty
        Email sentEmail = mailSendReceive.sendEmail(this.sendConfig, this.toAddresses, 
                this.ccAddresses, this.bccAddresses, null, null, null, null, null);
    }
    
    /*
     * Test method:
     * Should throw FileNotFoundException when entering invalid file/path to embedded attachment
    */
    @Test (timeout = 30000)
    public void testFileNotFoundForEmbAttachmentSendingEmail() throws InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException, FileNotFoundException {
        thrown.expect(FileNotFoundException.class);
        
        this.toAddresses.add("project.username02@gmail.com");
        String htmlMsg = "<img width=100 height=100 id=\"1\" src=\"cid:1ui0fu9w.jpg\" / >";
        this.embAttachments.add(new File("1ui0fu9w.jpg"));
        
        MailSendingReceiving mailSendReceive = new MailSendingReceiving();
        Email sentEmail = mailSendReceive.sendEmail(this.sendConfig, this.toAddresses, 
                null, null, null, null, htmlMsg, this.embAttachments, null);
    }
    
    /*
     * Test method:
     * Should throw FileNotFoundException when entering invalid file/path to attachment
    */
    @Test (timeout = 30000)
    public void testFileNotFoundForAttachmentSendingEmail() throws InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException, FileNotFoundException {
        thrown.expect(FileNotFoundException.class);
        
        this.toAddresses.add("project.username02@gmail.com");
        this.regAttachments.add(new File("afwq1"));
        
        MailSendingReceiving mailSendReceive = new MailSendingReceiving();
        Email sentEmail = mailSendReceive.sendEmail(this.sendConfig, this.toAddresses, 
                null, null, null, null, null, null, this.regAttachments);
    }
    
    /*
     * Test method:
     * Should throw InvalidEmailAddressException the MailConfigBean for receiving is invalid
    */
    @Test (timeout = 30000)
    public void testInvalidMailConfigForReceivingEmail() throws InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException, FileNotFoundException {
        thrown.expect(InvalidEmailAddressException.class);

        // Entered bob as email address
        MailConfigBean invalidSendConfig = new MailConfigBean("smtp.gmail.com", "imap.gmail.com", "bob", "asdfjkl;");
        
        MailSendingReceiving mailSendReceive = new MailSendingReceiving();
        ReceivedEmail[] receivedEmails = mailSendReceive.receiveEmail(invalidSendConfig);
    }
    
    /*
     * Test method:
     * receiveEmail() should return null when the ReceivedEmail[] is empty
    */
    @Test (timeout = 30000)
    public void testReturnedNullWhenReceiveEmailArrayEmpty() throws InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException, FileNotFoundException {
        MailSendingReceiving mailSendReceive = new MailSendingReceiving();
        
        // tempReceivedEmails to make sure all unseen email is seen
        ReceivedEmail[] tempReceivedEmails = mailSendReceive.receiveEmail(this.receiveConfig);
        ReceivedEmail[] receivedEmails = mailSendReceive.receiveEmail(this.receiveConfig);
        
        Assert.assertArrayEquals("FAILED: receiveEmail() did not return null when ReceivedEmail[] was empty", null, receivedEmails);
    }
    
    /*
     * Test method:
     * Compares if email account will receive 3 emails sent
    */
    @Test (timeout = 30000)
    public void testNumberOfEmailsSent() throws InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException, FileNotFoundException {
        this.toAddresses.add("project.username02@gmail.com");
        String subject = "Sending 3 identical emails";
        
        MailSendingReceiving mailSendReceive = new MailSendingReceiving();
        for (int i = 0; i < 3; i++) {
            Email sentEmail = mailSendReceive.sendEmail(this.sendConfig, this.toAddresses, 
                    null, null, subject, null, null, null, null);
        }
        sleepThreeSeconds();
        ReceivedEmail[] receivedEmails = mailSendReceive.receiveEmail(this.receiveConfig);
        
        assertEquals("FAILED: did not receive 3 emails sent", 3, receivedEmails.length);
    }
    
    /*
     * Test method:
     * Tests that email still sends empty string message if entered message values are null
    */
    @Test (timeout = 30000)
    public void testMessageEmptyIfNull() throws InvalidEmailAddressException, AbsentEmailAddressException, SessionFailureException, FileNotFoundException {
        this.toAddresses.add("project.username02@gmail.com");
        
        MailSendingReceiving mailSendReceive = new MailSendingReceiving();
        Email sendEmail = mailSendReceive.sendEmail(this.sendConfig, this.toAddresses, 
                null, null, null, null, null, null, null);
        sleepThreeSeconds();
        ReceivedEmail[] receivedEmails = mailSendReceive.receiveEmail(this.receiveConfig);
        
        assertEquals("FAILED: message is not an empty string", "", sendEmail.messages().get(0).getContent());
    }
}
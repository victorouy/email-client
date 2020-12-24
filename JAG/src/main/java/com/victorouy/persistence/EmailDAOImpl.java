package com.victorouy.persistence;

import com.victorouy.properties.EmailDataBean;
import com.victorouy.business.MailSendingReceiving;
import com.victorouy.exceptions.DeleteMandatoryFolderException;
import com.victorouy.exceptions.FolderNameAlreadyExistsException;
import com.victorouy.exceptions.ForbiddenEmailEditAttempException;
import com.victorouy.exceptions.ForbiddenFolderMoveException;
import com.victorouy.exceptions.InvalidFolderNameException;
import com.victorouy.properties.EmailTableFXBean;
import com.victorouy.properties.FolderTreeFXBean;
import com.victorouy.properties.MailConfigBean;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.activation.DataSource;
import javax.sql.rowset.serial.SerialBlob;
import jodd.mail.Email;
import jodd.mail.EmailAddress;
import jodd.mail.EmailAttachment;
import jodd.mail.EmailMessage;
import jodd.mail.ReceivedEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Java class using JDBC creating a functioning CRUD for an 
 * email based database implementing EmailDAO interface
 * 
 * @author Victor Ouy    1739282
 */
public class EmailDAOImpl implements EmailDAO {
    
    private final static Logger LOG = LoggerFactory.getLogger(MailSendingReceiving.class);
    
    private MailConfigBean mailConfigBean;
    private String dbConnection;
    
    public EmailDAOImpl(MailConfigBean mailConfigBean) {
        this.mailConfigBean = mailConfigBean;
        
        this.dbConnection = "jdbc:mysql://"
                + mailConfigBean.getUrlMySQL()
                + ":"
                + mailConfigBean.getPortMySQL()
                + "/"
                + mailConfigBean.getDatabase();
    }
    
    /**
     * Takes in an EmailDataBean object that has an email that was sent in which it uses its values to enter to the database
     * 
     * @param emailDataBean
     * @return int   number of folder record inserted
     * @throws SQLException
     */
    @Override
    public int createSendEmail(EmailDataBean emailDataBean) throws SQLException {
        int numResultReturned = 0;
        int emailId = -1;
        String insertEmailQuery = "INSERT INTO Email (FromEmailAddress, Subject, TextMessage, HtmlMessage, SentDate, FolderKey) VALUES (?,?,?,?,?,?)";

        try (Connection connection = DriverManager.getConnection(this.dbConnection, mailConfigBean.getUserMySQL(), mailConfigBean.getPwdMySQL());
                PreparedStatement prepStatement = connection.prepareStatement(insertEmailQuery, Statement.RETURN_GENERATED_KEYS);) {

            fillPrepStatementSentEmail(prepStatement, emailDataBean);
            numResultReturned = prepStatement.executeUpdate();

            try (ResultSet resultSet = prepStatement.getGeneratedKeys();) {
                if (resultSet.next()) {
                    emailId = resultSet.getInt(1);
                }
            }
            LOG.info("New generated emailId: " + emailId);
        }
        LOG.info("# of email records created : " + numResultReturned);
        
        emailDataBean.setEmailID(emailId);
        emailDataBean.setFolderKey(findFolderKey("SENT"));
        createEmailAddresses(emailDataBean.getEmailID(), emailDataBean.email);
        insertAttachments(emailDataBean.email.attachments(), emailDataBean.getEmailID());
        return numResultReturned;
    }
    
    /**
     * Inserts ReceivedEmail[] values from unread received emails into the database and converts the array
     * into an ArrayList<EmailDataBean> in which it returns
     * 
     * @param receivedEmails
     * @return ArrayList<EmailDataBean>
     * @throws SQLException
     * @throws IOException
     */
    @Override
    public ArrayList<EmailDataBean> createReceivedEmail(ReceivedEmail[] receivedEmails) throws SQLException, IOException {
        ArrayList<EmailDataBean> emailDataBeans = new ArrayList<EmailDataBean>();
        String insertEmailQuery = "INSERT INTO Email (FromEmailAddress, Subject, TextMessage, HtmlMessage, SentDate, ReceivedDate, FolderKey) VALUES (?,?,?,?,?,?,?)";
        
        for (ReceivedEmail receivedEmail : receivedEmails) {
            try (Connection connection = DriverManager.getConnection(this.dbConnection, mailConfigBean.getUserMySQL(), mailConfigBean.getPwdMySQL());
                    PreparedStatement prepStatement = connection.prepareStatement(insertEmailQuery, Statement.RETURN_GENERATED_KEYS);) {

                fillPrepStatementReceivedEmail(prepStatement, receivedEmail);
                prepStatement.executeUpdate();
                try (ResultSet resultSet = prepStatement.getGeneratedKeys();) {
                    if (resultSet.next()) {
                        int emailId = resultSet.getInt(1);
                        insertEmailAddresses(receivedEmail.to(), "TO", emailId);
                        insertEmailAddresses(receivedEmail.cc(), "CC", emailId);
                        insertAttachments(receivedEmail.attachments(), emailId);
                        emailDataBeans.add(convertReceivedToEmailDataBean(emailId));
                    }
                }
            }
        }
        LOG.info("# of created received EmailDataBeans: " + emailDataBeans.size());
        return emailDataBeans;
    }
    
    /**
     * Saves EmailDataBean into database as a "DRAFT" to be sent or edited later
     * 
     * @param emailDataBean
     * @return int  number of email records inserted
     * @throws SQLException
     */
    @Override
    public int createSaveEmail(EmailDataBean emailDataBean) throws SQLException {
        int numResultReturned = 0;
        int emailId = -1;
        String saveEmailQuery = "INSERT INTO Email (FromEmailAddress, Subject, TextMessage, HtmlMessage, FolderKey) VALUES (?,?,?,?,?)";
        
        try (Connection connection = DriverManager.getConnection(this.dbConnection, mailConfigBean.getUserMySQL(), mailConfigBean.getPwdMySQL());
                PreparedStatement prepStatement = connection.prepareStatement(saveEmailQuery, Statement.RETURN_GENERATED_KEYS);) {
            
            fillPrepStatementSaveEmail(prepStatement, emailDataBean);
            numResultReturned = prepStatement.executeUpdate();
            try (ResultSet resultSet = prepStatement.getGeneratedKeys();) {
                if (resultSet.next()) {
                    emailId = resultSet.getInt(1);
                }
            }
            LOG.info("New generated emailId: " + emailId);
        }
        LOG.info("# of email records created : " + numResultReturned);
        
        emailDataBean.setEmailID(emailId);
        emailDataBean.setFolderKey(findFolderKey("DRAFT"));
        createEmailAddresses(emailDataBean.getEmailID(), emailDataBean.email);
        insertAttachments(emailDataBean.email.attachments(), emailDataBean.getEmailID());
        return numResultReturned;
    }
    
//    /**
//     * Creates a new folder by inserting a Folder record into the database
//     * 
//     * @param folderName
//     * @return int   number of folder records inserted
//     * @throws SQLException
//     * @throws FolderNameAlreadyExistsException
//     * @throws InvalidFolderNameException 
//     */
//    @Override
//    public int createFolder(String folderName) throws SQLException, FolderNameAlreadyExistsException, InvalidFolderNameException {
//        validateFolderName(folderName);
//        int numResReturned = 0;
//        String createQuery = "INSERT INTO Folders (FolderName) VALUES (?)";
//        
//        try (Connection connection = DriverManager.getConnection(this.dbConnection, mailConfigBean.getUserMySQL(), mailConfigBean.getPwdMySQL());
//                PreparedStatement prepStatement = connection.prepareStatement(createQuery);) {
//            
//            prepStatement.setString(1, folderName);
//            numResReturned = prepStatement.executeUpdate();
//        }
//        
//        LOG.info("Created folder: " + folderName);
//        return numResReturned;
//    }
    /**
     * Creates a new folder by inserting a Folder record into the database
     * 
     * @param folderName
     * @return FolderTreeFXBean  of folder that was created
     * @throws SQLException
     * @throws FolderNameAlreadyExistsException
     * @throws InvalidFolderNameException 
     */
    @Override
    public FolderTreeFXBean createFolder(String folderName) throws SQLException, FolderNameAlreadyExistsException, InvalidFolderNameException {
        validateFolderName(folderName);
        String createQuery = "INSERT INTO Folders (FolderName) VALUES (?)";
        
        try (Connection connection = DriverManager.getConnection(this.dbConnection, mailConfigBean.getUserMySQL(), mailConfigBean.getPwdMySQL());
                PreparedStatement prepStatement = connection.prepareStatement(createQuery);) {
            
            prepStatement.setString(1, folderName);
            prepStatement.executeUpdate();
        }
        LOG.info("Created folder: " + folderName);
        
        int folderKey = -1;
        String selectQuery = "SELECT FolderKey FROM Folders WHERE FolderName = ?";
        
        try (Connection connection = DriverManager.getConnection(this.dbConnection, mailConfigBean.getUserMySQL(), mailConfigBean.getPwdMySQL());
                PreparedStatement prepStatement = connection.prepareStatement(selectQuery);) {
            
            prepStatement.setString(1, folderName);
            try (ResultSet resultSet = prepStatement.executeQuery();) {
                if (resultSet.next()) {
                    folderKey = resultSet.getInt("FolderKey");
                }
            }
        }
        return new FolderTreeFXBean(folderKey, folderName);
    }
    
    
    
//    /**
//     * Gets all email records and converts it into a list of EmailDataBeans
//     * 
//     * @return List<EmailDataBean>
//     * @throws SQLException
//     * @throws IOException 
//     */
//    @Override
//    public List<EmailDataBean> findAll() throws SQLException, IOException {
//        List<EmailDataBean> emailDataBeanRows = new ArrayList<EmailDataBean>();
//        String selectQuery = "SELECT EmailId, FromEmailAddress, Subject, TextMessage, HtmlMessage, SentDate, ReceivedDate, FolderKey FROM EMAIL";
//        
//        try (Connection connection = DriverManager.getConnection(this.dbConnection, mailConfigBean.getUserMySQL(), mailConfigBean.getPwdMySQL());
//                PreparedStatement prepStatement = connection.prepareStatement(selectQuery);
//                ResultSet resultSet = prepStatement.executeQuery()) {
//            while (resultSet.next()) {
//                emailDataBeanRows.add(createEmailDataBean(resultSet));
//            }
//        }
//        LOG.info("All emailDataBeanRows returned: " + emailDataBeanRows.size());
//        return emailDataBeanRows;
//    }
    /**
     * Gets all email records and converts it into a list of EmailDataBeans
     * 
     * @return List<EmailDataBean>
     * @throws SQLException
     * @throws IOException 
     */
    @Override
    public ObservableList<EmailTableFXBean> findAll() throws SQLException, IOException {
        ObservableList<EmailTableFXBean> emailDataBeanRows = FXCollections.observableArrayList();
        String selectQuery = "SELECT EmailId, FromEmailAddress, Subject, TextMessage, HtmlMessage, SentDate, ReceivedDate, FolderKey FROM Email";
        
        try (Connection connection = DriverManager.getConnection(this.dbConnection, mailConfigBean.getUserMySQL(), mailConfigBean.getPwdMySQL());
                PreparedStatement prepStatement = connection.prepareStatement(selectQuery);
                ResultSet resultSet = prepStatement.executeQuery()) {
            while (resultSet.next()) {
                EmailTableFXBean emailFXBean = new EmailTableFXBean();
                
                emailFXBean.setEmailId(resultSet.getInt("EmailId"));
                emailFXBean.setFrom(resultSet.getString("FromEmailAddress"));
                emailFXBean.setSubject(resultSet.getString("Subject"));
                emailFXBean.setFolderKey(resultSet.getInt("FolderKey"));
                
                Timestamp timestampSent = resultSet.getTimestamp("SentDate");
                if (!resultSet.wasNull()){
                    emailFXBean.setDate(timestampSent.toLocalDateTime());
                }
                Timestamp timestampReceived = resultSet.getTimestamp("ReceivedDate");
                if (!resultSet.wasNull()) {
                    emailFXBean.setDate(timestampReceived.toLocalDateTime());
                }
                emailDataBeanRows.add(emailFXBean);
            }
        }
        LOG.info("All emailDataBeanRows returned: " + emailDataBeanRows.size());
        return emailDataBeanRows;
    }
    
    /**
     * Gets email record depending on the emailId param and returns it as an EmailDataBean object
     * 
     * @param emailId
     * @return EmailDataBean   according to the emailId param
     * @throws SQLException
     * @throws IOException
     */
    @Override
    public EmailDataBean findByID(int emailId) throws SQLException, IOException {
        EmailDataBean emailDataBean = new EmailDataBean();
        String findByIdQuery = "SELECT EmailId, FromEmailAddress, Subject, TextMessage, HtmlMessage, SentDate, ReceivedDate, FolderKey FROM EMAIL "
                + "WHERE EmailId = ?";
        
        try (Connection connection = DriverManager.getConnection(this.dbConnection, mailConfigBean.getUserMySQL(), mailConfigBean.getPwdMySQL());
                PreparedStatement prepStatement = connection.prepareStatement(findByIdQuery);) {
            
            prepStatement.setInt(1, emailId);
            try (ResultSet resultSet = prepStatement.executeQuery();) {
                if (resultSet.next()) {
                    emailDataBean = createEmailDataBean(resultSet);
                }
            }
        }
        LOG.info("Created EmailDataBean obj given id: " + emailId);
        return emailDataBean;
    }
    
    /**
     * Gets a list of email records given a String that is in an emails FromAddress, ToAddress, CCAddress, BCCAddress or subject field
     * and converts it into a list of EmailDataBean objects
     * 
     * @param search
     * @return List<EmailDataBean>
     * @throws SQLException
     * @throws IOException 
     */
    @Override
    public List<EmailDataBean> findSearchedEmail(String search) throws SQLException, IOException {
        List<EmailDataBean> emailDataBeans = new ArrayList<EmailDataBean>();
        String searchQuery = "SELECT e.EmailId, e.FromEmailAddress, e.Subject, e.TextMessage, e.HtmlMessage, e.SentDate, "
                + "e.ReceivedDate, e.FolderKey FROM EMAIL e INNER JOIN EmailToEmailAddress eta ON e.EmailId = eta.EmailId "
                + "INNER JOIN EmailAddresses ea ON eta.EmailAddressId = ea.EmailAddressId "
                + "WHERE e.FromEmailAddress LIKE (?) OR e.Subject LIKE (?) OR ea.EmailAddress LIKE (?) "
                + "GROUP BY e.EmailId ORDER BY e.ReceivedDate, e.SentDate;";
        
        try (Connection connection = DriverManager.getConnection(this.dbConnection, mailConfigBean.getUserMySQL(), mailConfigBean.getPwdMySQL());
                PreparedStatement prepStatement = connection.prepareStatement(searchQuery);) {
            
            fillPrepStatementSearch(prepStatement, search);
            try (ResultSet resultSet = prepStatement.executeQuery();) {
                while (resultSet.next()) {
                    emailDataBeans.add(createEmailDataBean(resultSet));
                }
            }
        }
        LOG.info("# of EmailDataBean found in search: " + emailDataBeans.size());
        return emailDataBeans;
    }
    
    /**
     * Retrives a list of emails in the given folder String and converts it into a list of EmailDataBean objects
     * 
     * @param folderName
     * @return List<EmailDataBean>
     * @throws SQLException
     * @throws IOException 
     */
    @Override
    public List<EmailDataBean> findByFolder(String folderName) throws SQLException, IOException {
        List<EmailDataBean> emailDataBeans = new ArrayList<EmailDataBean>();
        String folderQuery = "SELECT EmailId, FromEmailAddress, Subject, TextMessage, HtmlMessage, SentDate, ReceivedDate, e.FolderKey FROM Email e "
                + "INNER JOIN Folders f on f.FolderKey = e.FolderKey WHERE f.FolderName = ?";
        
        try (Connection connection = DriverManager.getConnection(this.dbConnection, mailConfigBean.getUserMySQL(), mailConfigBean.getPwdMySQL());
                PreparedStatement prepStatement = connection.prepareStatement(folderQuery);) {
            
            prepStatement.setString(1, folderName);
            try (ResultSet resultSet = prepStatement.executeQuery();) {
                while (resultSet.next()) {
                    emailDataBeans.add(createEmailDataBean(resultSet));
                }
            }
        }
        LOG.info("# of EmailDataBeans found in " + folderName + ": " + emailDataBeans.size());
        return emailDataBeans;
    }
    
    /**
     * Gets all the folder names in the Folder table and returns it into a list of String
     * 
     * @return List<String> 
     * @throws SQLException 
     */
    @Override
    public ObservableList<FolderTreeFXBean> findAllFolderNames() throws SQLException {
//        List<String> folderNames = new ArrayList<String>();
    ObservableList<FolderTreeFXBean> folderTreeFXBean = FXCollections.observableArrayList();
        String findQuery = "SELECT FolderKey, FolderName FROM Folders";
        
        try (Connection connection = DriverManager.getConnection(this.dbConnection, mailConfigBean.getUserMySQL(), mailConfigBean.getPwdMySQL());
                PreparedStatement prepStatement = connection.prepareStatement(findQuery);) {
            
            try (ResultSet resultSet = prepStatement.executeQuery()) {
                while (resultSet.next()) {
                    folderTreeFXBean.add(new FolderTreeFXBean(resultSet.getInt("FolderKey"), resultSet.getString("FolderName")));
                }
            }
        }
//        LOG.info("# of foldernames found: " + folderNames.size());
        return folderTreeFXBean;
    }
    
    /**
     * Edits a draft email, along with its relationships, given an EmailDataBean object
     * (When saving an already draft email again)
     * 
     * @param emailDataBean
     * @return int   number of updated email records
     * @throws SQLException
     * @throws ForbiddenEmailEditAttempException
     */
    @Override
    public int updateEditDraftEmail(EmailDataBean emailDataBean) throws SQLException, ForbiddenEmailEditAttempException {
        if (!checkInsideDraft(emailDataBean.getEmailID())) {
            LOG.error("Attempted to update email fields not in draft");
            throw new ForbiddenEmailEditAttempException("Cannot update email fields that are not in draft ");
        }
        // This query does not include sentDate/receivedDate/folderKey because this is just saving an edited 
        // draft that stays in the "DRAFT" folder and the FromAddress since the from address can never be update
        String updateQuery = "UPDATE Email SET Subject=?, TextMessage=?, HtmlMessage=? WHERE EmailId = ?";
        int numResultReturned = 0;
        
        try (Connection connection = DriverManager.getConnection(this.dbConnection, mailConfigBean.getUserMySQL(), mailConfigBean.getPwdMySQL());
                PreparedStatement prepStatement = connection.prepareStatement(updateQuery);) {
            fillPrepStatementUpdateEmail(prepStatement, emailDataBean);
            numResultReturned = prepStatement.executeUpdate();
        }
        updateRecipients(emailDataBean);
        updateAttachments(emailDataBean);
        LOG.info("# of edited draft emails: " + numResultReturned);
        return numResultReturned;
    }
    
    /**
     * Moves the folder location of an email record given a corresponding folderName and EmailDataBean object 
     * 
     * @param emailDataBean
     * @param folderName
     * @return int   number of updated email records
     * @throws SQLException
     * @throws ForbiddenFolderMoveException
     */
    @Override
    public int updateMoveEmailFolder(EmailDataBean emailDataBean, String folderName) throws SQLException, ForbiddenFolderMoveException {
        int emailId = emailDataBean.getEmailID();
        if (checkInsideDraft(emailId) || folderName.equals(("DRAFT")) || emailDataBean.getFolderKey() == findFolderKey("DRAFT")) {
            LOG.error("Attempted email move into/out of draft folder");
            throw new ForbiddenFolderMoveException("Attempted to move email IN or OUT of draft folder");
        }
        int folderKey = findFolderKey(folderName);
        emailDataBean.setFolderKey(folderKey);
        
        int numResReturned = 0;
        String updateQuery = "UPDATE Email SET FolderKey=? WHERE EmailId = ?";
        
        try (Connection connection = DriverManager.getConnection(this.dbConnection, mailConfigBean.getUserMySQL(), mailConfigBean.getPwdMySQL());
                PreparedStatement prepStatement = connection.prepareStatement(updateQuery);) {
            
            prepStatement.setInt(1, folderKey);
            prepStatement.setInt(2, emailId);
            numResReturned = prepStatement.executeUpdate();
            LOG.info("Changed folder of emailId " + emailId + " to folderKey: " + folderKey);
        }
        return numResReturned;
    }
    
    /**
     * Update a draft email into a sent email along with its new edited fields given an EmailDataBean object
     * (When sending a draft email)
     * 
     * @param emailDataBean
     * @return int   number of email records updated
     * @throws SQLException
     * @throws ForbiddenEmailEditAttempException
     */
    @Override
    public int updateSendDraftEmail(EmailDataBean emailDataBean) throws SQLException, ForbiddenEmailEditAttempException {
        if (!checkInsideDraft(emailDataBean.getEmailID())) {
            LOG.error("Attempted to send email not in draft");
            throw new ForbiddenEmailEditAttempException("Cannot send email that is not in draft");
        }
        String updateQuery = "UPDATE Email SET FromEmailAddress=?, Subject=?, TextMessage=?, HtmlMessage=?, SentDate=?, FolderKey=? WHERE EmailId = ?";
        int numResultReturned = 0;
        
        try (Connection connection = DriverManager.getConnection(this.dbConnection, mailConfigBean.getUserMySQL(), mailConfigBean.getPwdMySQL());
                PreparedStatement prepStatement = connection.prepareStatement(updateQuery);) {
            fillPrepStatementSendDraftEmail(prepStatement, emailDataBean);
            numResultReturned = prepStatement.executeUpdate();
        }
        emailDataBean.setFolderKey(findFolderKey("SENT"));
        updateRecipients(emailDataBean);
        updateAttachments(emailDataBean);
        LOG.info("# of sent draft emails: " + numResultReturned);
        return numResultReturned;
    }
    
    /**
     * Deletes an email record given its emailId
     * 
     * @param emailId
     * @return int   number of email records deleted
     * @throws SQLException
     */
    @Override
    public int deleteEmail(int emailId) throws SQLException {
        // Must delete foreign keys first
        String deleteAttachQuery = "DELETE FROM Attachments WHERE EmailId = ?";
        executeQueryEmailId(deleteAttachQuery, emailId);
        String deleteEmailToAddQuery = "DELETE FROM EmailToEmailAddress WHERE EmailId = ?";
        executeQueryEmailId(deleteEmailToAddQuery, emailId);
        
        String deleteQuery = "DELETE FROM Email WHERE EmailId = ?";
        return executeQueryEmailId(deleteQuery, emailId);
    }
    
    /**
     * Deletes a folder record given its folderName
     * 
     * @param folderName
     * @return int   number of deleted folder records
     * @throws SQLException
     * @throws DeleteMandatoryFolderException
     */
    @Override
    public int deleteFolder(String folderName) throws SQLException, DeleteMandatoryFolderException {
        // Validates if folder to delete is valid
        checkDeleteFolder(folderName);
        
        // Deletes all email in given folder
        deleteEmailsByFolder(folderName);
        String deleteQuery = "DELETE FROM Folders WHERE FolderName = ?";
        int numResults = 0;
        
        try (Connection connection = DriverManager.getConnection(this.dbConnection, mailConfigBean.getUserMySQL(), mailConfigBean.getPwdMySQL());
                PreparedStatement prepStatement = connection.prepareStatement(deleteQuery);) {
            prepStatement.setString(1, folderName);
            numResults = prepStatement.executeUpdate();
        }
        LOG.info("# of deleted folders: " + numResults);
        return numResults;
    }
    
    /**
     * Fills in the prepared statement for a sent email query
     * 
     * @param prepStatement
     * @param emailDataBean
     * @throws SQLException
     */
    private void fillPrepStatementSentEmail(PreparedStatement prepStatement, EmailDataBean emailDataBean) throws SQLException {
        Email email = emailDataBean.email;
        prepStatement.setString(1, email.from().getEmail());
        prepStatement.setString(2, email.subject());
        insertMessages(prepStatement, email.messages());
        prepStatement.setTimestamp(5, new Timestamp(email.sentDate().getTime()));
        prepStatement.setInt(6, findFolderKey("SENT"));
    }
    
    /**
     * Fills in the prepared statement for a received email query
     * 
     * @param prepStatement
     * @param receivedEmail
     * @throws SQLException
     */
    private void fillPrepStatementReceivedEmail(PreparedStatement prepStatement, ReceivedEmail receivedEmail) throws SQLException {
        prepStatement.setString(1, receivedEmail.from().getEmail());
        prepStatement.setString(2, receivedEmail.subject());
        insertMessages(prepStatement, receivedEmail.messages());
        prepStatement.setTimestamp(5, new Timestamp(receivedEmail.sentDate().getTime()));
        prepStatement.setTimestamp(6, new Timestamp(receivedEmail.receivedDate().getTime()));
        prepStatement.setInt(7, findFolderKey("INBOX"));
    }
    
    /**
     * Fills in the prepared statement for a save email query
     * 
     * @param prepStatement
     * @param emailDataBean
     * @throws SQLException
     */
    private void fillPrepStatementSaveEmail(PreparedStatement prepStatement, EmailDataBean emailDataBean) throws SQLException {
        prepStatement.setString(1, emailDataBean.email.from().getEmail());
        prepStatement.setString(2, emailDataBean.email.subject());
        insertMessages(prepStatement, emailDataBean.email.messages());
        prepStatement.setInt(5, findFolderKey("DRAFT"));
    }
    
    /**
     * Fills in the prepared statement for a sending draft email query
     * 
     * @param prepStatement
     * @param emailDataBean
     * @throws SQLException
     */
    private void fillPrepStatementSendDraftEmail(PreparedStatement prepStatement, EmailDataBean emailDataBean) throws SQLException {
        prepStatement.setString(1, emailDataBean.email.from().getEmail());
        prepStatement.setString(2, emailDataBean.email.subject());
        insertMessages(prepStatement, emailDataBean.email.messages());
        emailDataBean.email.currentSentDate();
        prepStatement.setTimestamp(5, new Timestamp(emailDataBean.email.sentDate().getTime()));
        prepStatement.setInt(6, findFolderKey("SENT"));
        prepStatement.setInt(7, emailDataBean.getEmailID());
    } 

    /**
     * Fills in the prepared statement for a update email query
     * 
     * @param prepStatement
     * @param emailDataBean
     * @throws SQLException 
     */
    private void fillPrepStatementUpdateEmail(PreparedStatement prepStatement, EmailDataBean emailDataBean) throws SQLException {
        prepStatement.setString(1, emailDataBean.email.subject());
        editMessages(prepStatement, emailDataBean.email.messages());
        prepStatement.setInt(4, emailDataBean.getEmailID());
    } 
    
    /**
     * Fills in the prepared statement for an email to email address bridging table query
     * 
     * @param prepStatement
     * @param emailId
     * @param emailAddressId
     * @param type
     * @throws SQLException 
     */
    private void fillPrepStatementEmailToEmailAddress(PreparedStatement prepStatement, int emailId, int emailAddressId, String type) throws SQLException {
        prepStatement.setInt(1, emailId);
        prepStatement.setInt(2, emailAddressId);
        prepStatement.setString(3, type);
    }
    
    /**
     * Fills in the prepared statement for an email search query
     * 
     * @param prepStatement
     * @param search
     * @throws SQLException 
     */
    private void fillPrepStatementSearch(PreparedStatement prepStatement, String search) throws SQLException {
        prepStatement.setString(1, "%"+search+"%");
        prepStatement.setString(2, "%"+search+"%");
        prepStatement.setString(3, "%"+search+"%");
    }
    
    /**
     * Converts a received email into a EmailDataBean
     * 
     * @param emailId
     * @return EmailDataBean
     * @throws SQLException
     * @throws IOException
     */
    private EmailDataBean convertReceivedToEmailDataBean(int emailId) throws SQLException, IOException {
        EmailDataBean emailDataBean = new EmailDataBean();
        String selectEmailQuery = "SELECT EmailId, FromEmailAddress, Subject, TextMessage, HtmlMessage, SentDate, ReceivedDate, FolderKey FROM Email WHERE EmailId = ?";
        
        try (Connection connection = DriverManager.getConnection(this.dbConnection, mailConfigBean.getUserMySQL(), mailConfigBean.getPwdMySQL());
                PreparedStatement prepStatement = connection.prepareStatement(selectEmailQuery);) {
            prepStatement.setInt(1, emailId);
            try (ResultSet resultSet = prepStatement.executeQuery()) {
                if (resultSet.next()) {
                     emailDataBean = createEmailDataBean(resultSet);
                }     
            } 
        }
        return emailDataBean;
    }
    
    /**
     * Inserts text and html messages into database
     * 
     * @param prepStatement
     * @param messageList
     * @throws SQLException 
     */
    private void insertMessages(PreparedStatement prepStatement, List<EmailMessage> messageList) throws SQLException {
        if (messageList.size() == 2) {
            prepStatement.setString(3, messageList.get(0).getContent());
            prepStatement.setString(4, messageList.get(1).getContent());
        }
        else if (messageList.size() == 1) {
            if (messageList.get(0).getMimeType().equals("text/html")) {
                prepStatement.setString(3, null);
                prepStatement.setString(4, messageList.get(0).getContent());
            }
            else {
                prepStatement.setString(3, messageList.get(0).getContent());
                prepStatement.setString(4, null);
            }
        }
        else if (messageList.size() == 0) {
            prepStatement.setString(3, null);
            prepStatement.setString(4, null);
        }
    }
    
    /**
     * Inserts email addresses into the database EmailAddress table
     * 
     * @param emailAddresses
     * @param recipientType
     * @param emailId
     * @throws SQLException 
     */
    private void insertEmailAddresses(EmailAddress[] emailAddresses, String recipientType, int emailId) throws SQLException {
        if (emailAddresses != null && emailAddresses.length > 0) {
            for (EmailAddress emailAdd : emailAddresses) {
                String emailAddress = emailAdd.getEmail();
                
                // getEmailAddressId either returns -1 representing no duplicate, or returns positive int representing emailAddressId
                int emailAddressId = getEmailAddressId(emailAddress);
                if (emailAddressId == -1) {
                    String insertEmailAddressesQuery = "INSERT INTO EmailAddresses (EmailAddress) VALUES (?)";

                    try (Connection connection = DriverManager.getConnection(this.dbConnection, mailConfigBean.getUserMySQL(), mailConfigBean.getPwdMySQL());
                            PreparedStatement prepStatement = connection.prepareStatement(insertEmailAddressesQuery, Statement.RETURN_GENERATED_KEYS);) {

                        prepStatement.setString(1, emailAddress);
                        prepStatement.executeUpdate();

                        try (ResultSet resultSet = prepStatement.getGeneratedKeys();) {
                            if (resultSet.next()) {
                                emailAddressId = resultSet.getInt(1);
                            }
                        }
                        LOG.info("New EmailAddressID: " + emailAddressId);
                    }
                }
                insertEmailToEmailAddress(emailId, emailAddressId, recipientType);
            }
        }
    }
    
    /**
     * Retrieves EmailAddressId given an emailAddress String
     * 
     * @param emailAddress
     * @return int    returns emailAddressId if there is a duplicate emailAddress record, returns -1 if there are no duplicates
     * @throws SQLException 
     */
    private int getEmailAddressId(String emailAddress) throws SQLException {
        String insertEmail_EmailAddressQuery = "SELECT EmailAddressId FROM EmailAddresses WHERE EmailAddress = ?";
        try (Connection connection = DriverManager.getConnection(this.dbConnection, mailConfigBean.getUserMySQL(), mailConfigBean.getPwdMySQL());
                PreparedStatement prepStatement = connection.prepareStatement(insertEmail_EmailAddressQuery);) {
            
            prepStatement.setString(1, emailAddress);
            int emailAddressId = -1;
            try (ResultSet resultSet = prepStatement.executeQuery();) {
                // Should only return one since EmailAddress field is UNIQUE
                if (resultSet.next()) {
                    emailAddressId = resultSet.getInt(1);
                }
            }
            if (emailAddressId == -1) {
                // -1 = no duplicates
                // So does not have a valid emailAddressId to return
                LOG.info("No duplicate email address");
                return emailAddressId;
            }
            LOG.info("Duplicate EmailAddressId: " + emailAddressId);
            return emailAddressId;
        }
    }
    
    /**
     * Inserts into EmailToEmailAddress table with emailId, emailAddressId, and the recipientType
     * 
     * @param emailId
     * @param emailAddressId
     * @param recipientType
     * @throws SQLException 
     */
    private void insertEmailToEmailAddress(int emailId, int emailAddressId, String recipientType) throws SQLException {
        String insertEmailToEmailAddressQuery = "INSERT INTO EmailToEmailAddress (EmailId, EmailAddressId, RecipientType) VALUES (?,?,?)";
        int numResultReturned = 0;
        try (Connection connection = DriverManager.getConnection(this.dbConnection, mailConfigBean.getUserMySQL(), mailConfigBean.getPwdMySQL());
                PreparedStatement prepStatement = connection.prepareStatement(insertEmailToEmailAddressQuery);) {

            fillPrepStatementEmailToEmailAddress(prepStatement, emailId, emailAddressId, recipientType);
            numResultReturned = prepStatement.executeUpdate();
        }
    }
    
    /**
     * Inserts into EmailAddress given an Email bean
     * 
     * @param emailId
     * @param email
     * @throws SQLException 
     */
    private void createEmailAddresses(int emailId, Email email) throws SQLException {
        insertEmailAddresses(email.to(), "TO", emailId);
        insertEmailAddresses(email.cc(), "CC", emailId);
        insertEmailAddresses(email.bcc(), "BCC", emailId);
    }
    
    /**
     * Creates an EmailDataBean object given a resultSet of email field values
     * 
     * @param resultSet
     * @return EmailDataBean 
     * @throws SQLException
     * @throws IOException 
     */
    private EmailDataBean createEmailDataBean(ResultSet resultSet) throws SQLException, IOException {
        EmailDataBean emailData = new EmailDataBean();
        emailData.setEmailID(resultSet.getInt("EmailId"));
        emailData.setFolderKey(resultSet.getInt("FolderKey"));
        
        Timestamp timestamp = resultSet.getTimestamp("ReceivedDate");
        if (!resultSet.wasNull()) {
            emailData.setReceivedDate(timestamp.toLocalDateTime());
        }
        emailData.email = generateEmail(resultSet);
        
        return emailData;
    }
    
    /**
     * Generates an Email Jodd object given a resultSet
     * 
     * @param resultSet
     * @return Email
     * @throws SQLException
     * @throws IOException 
     */
    private Email generateEmail(ResultSet resultSet) throws SQLException, IOException {
        Email email = Email.create().from(resultSet.getString("FromEmailAddress"));
        email.subject(resultSet.getString("Subject"));
        handleMsgs(resultSet.getString("TextMessage"), resultSet.getString("HtmlMessage"), email);
        handleAttachments(resultSet.getInt("EmailId"), email);
        handleRecipients(resultSet.getInt("EmailId"), email);
        if (resultSet.getTimestamp("SentDate") != null) {
            email.sentDate(resultSet.getTimestamp("SentDate"));
        }
        return email;
    }
    
    /**
     * Sets recipients from the database to Email object
     * 
     * @param emailId
     * @param email
     * @throws SQLException 
     */
    private void handleRecipients(int emailId, Email email) throws SQLException {
        String query = "SELECT EmailAddress, RecipientType FROM EmailToEmailAddress INNER JOIN EmailAddresses "
                + "ON EmailToEmailAddress.EmailAddressId = EmailAddresses.EmailAddressId WHERE EmailId = ?";
        
        try (Connection connection = DriverManager.getConnection(this.dbConnection, mailConfigBean.getUserMySQL(), mailConfigBean.getPwdMySQL());
                PreparedStatement prepStatement = connection.prepareStatement(query);) {
            
            prepStatement.setInt(1, emailId);
            try (ResultSet resultSet = prepStatement.executeQuery();) {
                while (resultSet.next()) {
                    assignByRecipientType(resultSet.getString("RecipientType"), resultSet.getString("EmailAddress"), email);
                }
            }
        }
    }
    
    /**
     * Given one of three recipient types, it will set it to the Email object accordingly
     * 
     * @param recipientType
     * @param emailAddress
     * @param email 
     */
    private void assignByRecipientType(String recipientType, String emailAddress, Email email) {
        switch (recipientType) {
            case "TO":
                email.to(emailAddress);
                break;
            case "CC":
                email.cc(emailAddress);
                break;
            case "BCC":
                email.bcc(emailAddress);
                break;
        }
    }
    
    /**
     * Sets text and html messages from the database to the given Email object
     * 
     * @param textMessage
     * @param htmlMessage
     * @param email
     * @throws SQLException 
     */
    private void handleMsgs(String textMessage, String htmlMessage, Email email) throws SQLException {
        boolean hasSetMessage = false;
        if (textMessage != null && !textMessage.isEmpty()) {
            email.textMessage(textMessage);
            hasSetMessage = true;
        }
        if (htmlMessage != null && !htmlMessage.isEmpty()) {
            email.textMessage(htmlMessage);
            hasSetMessage = true;
        }
        
        if (!hasSetMessage) {
            // Setting textMessage "" if no message was assigned to Email object because the Email object 
            // cannot have null in both TextMessage and HtmlMessage
            email.textMessage("");
        }
    }
    
    /**
     * Sets attachments from the database to the given Email object
     * 
     * @param emailId
     * @param email
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws IOException 
     */
    private void handleAttachments(int emailId, Email email) throws SQLException, FileNotFoundException, IOException {
        String attachmentQuery = "SELECT FileName, CID, BinaryData FROM Attachments WHERE EmailId = ?";
        
        try (Connection connection = DriverManager.getConnection(this.dbConnection, mailConfigBean.getUserMySQL(), mailConfigBean.getPwdMySQL());
                PreparedStatement prepStatement = connection.prepareStatement(attachmentQuery);) {
            
            prepStatement.setInt(1, emailId);
            try (ResultSet resultSetAttachment = prepStatement.executeQuery();) {
                while (resultSetAttachment.next()) {
                    String fileName = resultSetAttachment.getString("FileName");
                    File attachment = new File(fileName);
                    FileOutputStream fos = new FileOutputStream(attachment);
                    
                    byte[] buffer = new byte[1];
                    InputStream inputStream = resultSetAttachment.getBinaryStream("BinaryData");
                    if (!resultSetAttachment.wasNull()) {
                        while (inputStream.read(buffer) > 0) {
                            fos.write(buffer);
                        }
                        fos.close();
                        String cid = resultSetAttachment.getString("CID");
                        if (cid == null || cid.isEmpty()) {
                            email.attachment(EmailAttachment.with().content(attachment));
                        }
                        else {
                            email.embeddedAttachment(EmailAttachment.with().content(attachment));
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Edits messages of a "DRAFT" email in the database
     * 
     * @param prepStatement
     * @param messageList
     * @throws SQLException 
     */
    private void editMessages(PreparedStatement prepStatement, List<EmailMessage> messageList) throws SQLException {
        if (messageList.size() == 2) {
            prepStatement.setString(2, messageList.get(0).getContent());
            prepStatement.setString(3, messageList.get(1).getContent());
        }
        else if (messageList.size() == 1) {
            if (messageList.get(0).getMimeType().equals("text/html")) {
                prepStatement.setString(2, null);
                prepStatement.setString(3, messageList.get(0).getContent());
            }
            else {
                prepStatement.setString(2, messageList.get(0).getContent());
                prepStatement.setString(3, null);
            }
        }
        else if (messageList.size() == 0) {
            prepStatement.setString(2, null);
        }
    }
    
    /**
     * Checks if the email record given an emailId is in the "DRAFT" folder or not
     * 
     * @param emailDataBean
     * @param folderName
     * @return boolean   returns true if email record is in draft, returns false otherwise
     * @throws SQLException
     * @throws ForbiddenFolderMoveException
     */
    private boolean checkInsideDraft(int emailId) throws SQLException {
        String checkQuery = "SELECT FolderName FROM Folders f INNER JOIN Email e ON f.FolderKey = e.FolderKey WHERE e.EmailId = ?";
        try (Connection connection = DriverManager.getConnection(this.dbConnection, mailConfigBean.getUserMySQL(), mailConfigBean.getPwdMySQL());
                PreparedStatement prepStatement = connection.prepareStatement(checkQuery);) {
            
            prepStatement.setInt(1, emailId);
            try (ResultSet resultSet = prepStatement.executeQuery()) {
                if (resultSet.next()) {
                    if (resultSet.getString(1).equals(("DRAFT"))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Updates the records inside EmailToEmailAddress table
     * 
     * @param emailDataBean
     * @throws SQLException 
     */
    private void updateRecipients(EmailDataBean emailDataBean) throws SQLException {
        String deleteEmailToAddQuery = "DELETE FROM EmailToEmailAddress WHERE EmailId = ?";
        executeQueryEmailId(deleteEmailToAddQuery, emailDataBean.getEmailID());
        createEmailAddresses(emailDataBean.getEmailID(), emailDataBean.email);
    }
    
    /**
     * Updates the records inside Attachments table
     * 
     * @param emailDataBean
     * @throws SQLException 
     */
    private void updateAttachments(EmailDataBean emailDataBean) throws SQLException {
        String deleteAttachQuery = "DELETE FROM Attachments WHERE EmailId = ?";
        executeQueryEmailId(deleteAttachQuery, emailDataBean.getEmailID());
        insertAttachments(emailDataBean.email.attachments(), emailDataBean.getEmailID());
    }
    
    /**
     * Inserts into attachments table given a list of attachments
     * 
     * @param attachments
     * @param emailId
     * @throws SQLException 
     */
    private void insertAttachments(List<EmailAttachment<? extends DataSource>> attachments, int emailId) throws SQLException {
        String insertAttachmentQuery = "INSERT INTO Attachments (EmailId, FileName, CID, BinaryData) VALUES (?,?,?,?)";
        
        // Decision to not use a for each loop in this case, as we would have had to import multiple external classes(more complicated)
        for (int i = 0; i < attachments.size(); i++) {
            try (Connection connection = DriverManager.getConnection(this.dbConnection, mailConfigBean.getUserMySQL(), mailConfigBean.getPwdMySQL());
                    PreparedStatement prepStatement = connection.prepareStatement(insertAttachmentQuery);) {
                prepStatement.setInt(1, emailId);
                prepStatement.setString(2, attachments.get(i).getName());
                if (attachments.get(i).isEmbedded()) {
                    prepStatement.setString(3, attachments.get(i).getContentId());
                }
                else {
                    prepStatement.setString(3, null);
                }
                Blob blob = new SerialBlob(attachments.get(i).toByteArray());
                prepStatement.setBlob(4, blob);
                prepStatement.setBytes(4, attachments.get(i).toByteArray());
                
                prepStatement.executeUpdate();
            }
        }
    }
    
    /**
     * Gets folderkey of given foldername in Folders table
     * 
     * @param folderName
     * @return int   folderkey
     * @throws SQLException
     */
    private int findFolderKey(String folderName) throws SQLException {
        int folderKey = -1;
        String folderKeyQuery = ("SELECT FolderKey, FolderName FROM Folders WHERE FolderName = ?");
        
        try (Connection connection = DriverManager.getConnection(this.dbConnection, mailConfigBean.getUserMySQL(), mailConfigBean.getPwdMySQL());
                PreparedStatement prepStatement = connection.prepareStatement(folderKeyQuery);) {
            
            prepStatement.setString(1, folderName);
            try (ResultSet resultSet = prepStatement.executeQuery()) {
                while (resultSet.next()) {
                    if (folderName.equals(resultSet.getString("FolderName"))) {
                        folderKey = resultSet.getInt("FolderKey");
                    }
                }
            }
        }
        return folderKey;
    }
    
    /**
     * Executes a simple query that has been repeated multiple times 
     * 
     * @param query
     * @param emailId
     * @return int   number of records affected
     * @throws SQLException 
     */
    private int executeQueryEmailId(String query, int emailId) throws SQLException {
        try (Connection connection = DriverManager.getConnection(this.dbConnection, mailConfigBean.getUserMySQL(), mailConfigBean.getPwdMySQL());
                PreparedStatement prepStatement = connection.prepareStatement(query);) {
            prepStatement.setInt(1, emailId);
            return prepStatement.executeUpdate();
        }
    }
    
    /**
     * Validates if foldername is applicable, either it be invalid or already exists
     * 
     * @param folderName
     * @throws SQLException
     * @throws FolderNameAlreadyExistsException
     * @throws InvalidFolderNameException 
     */
    private void validateFolderName(String folderName) throws SQLException, FolderNameAlreadyExistsException, InvalidFolderNameException {
        if (folderName == null || folderName.isEmpty() || folderName.length() > 30) {
            LOG.error("Entered an invalid foldername of " + folderName);
            throw new InvalidFolderNameException("Invalid folder name: " + folderName);
        }
        // Default folders in lowercase can not be duplicated
        boolean hasDefaultLowercase = folderName.equals("sent") || folderName.equals("draft") ||folderName.equals("inbox");
        if (checkIfFolderExists(folderName) || hasDefaultLowercase) {
            LOG.error("Folder name " + folderName + " already exists");
            throw new FolderNameAlreadyExistsException("Folder name already exists: " + folderName);
        }
    }
    
    /**
     * Checks if folder exists in Folders table given folder string
     * 
     * @param folderName
     * @return boolean   returns true if folder name exists, false otherwise
     * @throws SQLException 
     */
    private boolean checkIfFolderExists(String folderName) throws SQLException {
        String checkQuery = "SELECT FolderKey FROM Folders WHERE FolderName = ?";
        
        try (Connection connection = DriverManager.getConnection(this.dbConnection, mailConfigBean.getUserMySQL(), mailConfigBean.getPwdMySQL());
                PreparedStatement prepStatement = connection.prepareStatement(checkQuery);) {
            
            prepStatement.setString(1, folderName);
            try (ResultSet resultSet = prepStatement.executeQuery()) {
                if (resultSet.next()) {
                    LOG.info("Folder " + folderName + " already exists");
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Checks if given folder can be deleted in the Folders table
     * 
     * @param folderName
     * @throws SQLException
     * @throws DeleteMandatoryFolderException
     */
    private void checkDeleteFolder(String folderName) throws SQLException, DeleteMandatoryFolderException {
        if (folderName.toUpperCase().equals("DRAFT") || folderName.toUpperCase().equals("INBOX") || folderName.toUpperCase().equals("SENT")) {
            LOG.error("Attempted to delete mandatory folder " + folderName);
            throw new DeleteMandatoryFolderException("Forbidden to delete folder: " + folderName);
        }
    }
    
    /**
     * Deletes all emails in a given folder
     * 
     * @param folderName
     * @throws SQLException
     */
    private void deleteEmailsByFolder(String folderName) throws SQLException{
        String selectQuery = "SELECT e.EmailId FROM Email e INNER JOIN Folders f ON e.FolderKey = f.FolderKey WHERE f.FolderName = ?";
        
        try (Connection connection = DriverManager.getConnection(this.dbConnection, mailConfigBean.getUserMySQL(), mailConfigBean.getPwdMySQL());
                PreparedStatement prepStatement = connection.prepareStatement(selectQuery);) {
                
            prepStatement.setString(1, folderName);
            try (ResultSet resultSet = prepStatement.executeQuery()) {
                while (resultSet.next()) {
                    deleteEmail(resultSet.getInt(1));
                }
            }
        }
    }
    
    /**
     * Returns all the names of attachments given an emailId
     * 
     * @param emailId
     * @return
     * @throws SQLException 
     */
    public List<String> findAttachmentNames(int emailId) throws SQLException {
        List<String> embAttachmenNames = new ArrayList<String>();
        String selectQuery = "SELECT FileName FROM attachments WHERE emailid = ?";
        
        try (Connection connection = DriverManager.getConnection(this.dbConnection, mailConfigBean.getUserMySQL(), mailConfigBean.getPwdMySQL());
                PreparedStatement prepStatement = connection.prepareStatement(selectQuery);) {
                
            prepStatement.setInt(1, emailId);
            try (ResultSet resultSet = prepStatement.executeQuery()) {
                while (resultSet.next()) {
                    embAttachmenNames.add(resultSet.getString(1));
                }
            }
        }
        return embAttachmenNames;
    }
}
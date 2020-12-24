package com.victorouy.manager;

import com.victorouy.business.MailSendingReceiving;
import com.victorouy.properties.MailConfigBean;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import java.nio.file.Path;
import static java.nio.file.Paths.get;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to manage properties
 *
 * @author Victor Ouy   1739282
 * 
 */
public class PropertiesManager {

    private final static Logger LOG = LoggerFactory.getLogger(MailSendingReceiving.class);
    
    /**
     * Updates a PropertBean object with the contents of the properties file
     *
     * @param propertyBean
     * @param path
     * @param propFileName
     * @return
     * @throws java.io.IOException
     */
    public final boolean loadTextProperties(final MailConfigBean mailConfigBean, final String path, final String propFileName) throws IOException {
        boolean found = false;
        Properties prop = new Properties();

        Path txtFile = get(path, propFileName + ".properties");

        // File must exist
        if (Files.exists(txtFile)) {
            try (InputStream propFileStream = newInputStream(txtFile);) {
                prop.load(propFileStream);
            }
            if (checkContainsAllProperties(prop)) {
                mailConfigBean.setUsername(prop.getProperty("username"));
                mailConfigBean.setEmailAddress(prop.getProperty("emailAddress"));
                mailConfigBean.setPwdEmailAddress(prop.getProperty("pwdEmailAddress"));
                mailConfigBean.setUrlIMAP(prop.getProperty("urlIMAP"));
                mailConfigBean.setUrlSMTP(prop.getProperty("urlSMTP"));
                mailConfigBean.setPortIMAP(prop.getProperty("portIMAP"));
                mailConfigBean.setPortSMTP(prop.getProperty("portSMTP"));
                mailConfigBean.setUrlMySQL(prop.getProperty("urlMySQL"));
                mailConfigBean.setDatabase(prop.getProperty("database"));
                mailConfigBean.setPortMySQL(prop.getProperty("portMySQL"));
                mailConfigBean.setUserMySQL(prop.getProperty("userMySQL"));
                mailConfigBean.setPwdMySQL(prop.getProperty("pwdMySQL"));

                found = true;
            }
        }
        return found;
    }
    
    /**
     * Checks if MailConfig properties file contains all necessary keys to fill MailConfigBean
     * 
     * @param prop
     * @return true if properties file contains all required keys, false otherwise
     */
    private boolean checkContainsAllProperties(Properties prop) {
        if (prop.containsKey("username") && prop.containsKey("emailAddress") && prop.containsKey("pwdEmailAddress") && prop.containsKey("urlIMAP")
                 && prop.containsKey("urlSMTP") && prop.containsKey("portIMAP") && prop.containsKey("portSMTP") && prop.containsKey("urlMySQL")
                 && prop.containsKey("database") && prop.containsKey("portMySQL") && prop.containsKey("userMySQL") && prop.containsKey("pwdMySQL")) {
            return true;
        }
        return false;
    }


    /**
     * Creates a plain text properties file based on the parameters
     *
     * @param path Must exist, will not be created
     * @param propFileName Name of the properties file
     * @param propertyBean The bean to store into the properties
     * @throws IOException
     */
    public final void writeTextProperties(final MailConfigBean mailConfigBean, final String path, final String propFileName) throws IOException {

        Properties prop = new Properties();
        
        if (mailConfigBean == null)
            LOG.error("mailConfigBean IS NULL");

        prop.setProperty("username", mailConfigBean.getUsername());
        prop.setProperty("emailAddress", mailConfigBean.getUserEmailAddress());
        prop.setProperty("pwdEmailAddress", mailConfigBean.getPwdEmailAddress());
        prop.setProperty("urlIMAP", mailConfigBean.getUrlIMAP());
        prop.setProperty("urlSMTP", mailConfigBean.getUrlSMTP());
        prop.setProperty("portIMAP", mailConfigBean.getPortIMAP());
        prop.setProperty("portSMTP", mailConfigBean.getPortSMTP());
        prop.setProperty("urlMySQL", mailConfigBean.getUrlMySQL());
        prop.setProperty("database", mailConfigBean.getDatabase());
        prop.setProperty("portMySQL", mailConfigBean.getPortMySQL());
        prop.setProperty("userMySQL", mailConfigBean.getUserMySQL());
        prop.setProperty("pwdMySQL", mailConfigBean.getPwdMySQL());
        
        Path txtFile = get(path, propFileName + ".properties");

        // Creates the file or if file exists it is truncated to length of zero
        // before writing
        try ( OutputStream propFileStream = newOutputStream(txtFile)) {
            prop.store(propFileStream, "SMTP Properties");
        }
      
    }
}

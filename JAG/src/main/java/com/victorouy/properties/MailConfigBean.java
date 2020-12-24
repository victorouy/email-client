package com.victorouy.properties;

import java.util.Objects;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Class to contain property configuration for an email account
 *
 * @author Victor Ouy   1739282
 *
 */
public class MailConfigBean {
    
    private final StringProperty username;
    private final StringProperty emailAddress;
    private final StringProperty pwdEmailAddress; 
    private final StringProperty urlIMAP; 
    private final StringProperty urlSMTP;
    private final StringProperty portIMAP;
    private final StringProperty portSMTP; 
    private final StringProperty urlMySQL; 
    private final StringProperty database;
    private final StringProperty portMySQL;
    private final StringProperty userMySQL;
    private final StringProperty pwdMySQL;

    /**
     * Default Constructor
     */
    public MailConfigBean() {
        this("", "", "", "", "", "993", "465", "", "", "3306", "", "");
    }

    /**
     * Non-default constructor
     * (USED FOR PHASE 1)
     * 
     * @param urlSMTP
     * @param urlIMAP
     * @param emailAddress
     * @param pwdEmailAddress
     */
    public MailConfigBean(final String urlSMTP, final String urlIMAP, final String emailAddress, final String pwdEmailAddress) {
        this("", emailAddress, pwdEmailAddress, urlIMAP, urlSMTP, "993", "465", "", "", "3306", "", "");
    }
    
    /**
     * Non-default constructor
     * (USED FOR PHASE 2)
     * 
     * @param urlSMTP
     * @param urlIMAP
     * @param emailAddress
     * @param pwdEmailAddress
     * @param urlMySQL
     * @param userMySQL
     * @param pwdMySQL
     */
    public MailConfigBean(final String urlSMTP, final String urlIMAP, final String emailAddress, final String pwdEmailAddress,
            final String urlMySQL, final String database, final String portMySQL, final String userMySQL, final String pwdMySQL) {
        this("", emailAddress, pwdEmailAddress, urlIMAP, urlSMTP, "993", "465", urlMySQL, database, portMySQL, userMySQL, pwdMySQL);
    }
    
    /**
     * Non-default constructor
     * 
     * @param username
     * @param userEmailAddress
     * @param pwdEmailAddress
     * @param urlIMAP
     * @param urlSMTP
     * @param portIMAP
     * @param portSMTP
     * @param urlMySQL
     * @param database
     * @param portMySQL
     * @param userMySQL
     * @param pwdMySQL
     */
    public MailConfigBean(final String username, final String emailAddress, final String pwdEmailAddress, final String urlIMAP, final String urlSMTP, final String portIMAP, 
            final String portSMTP, final String urlMySQL, final String database, final String portMySQL, final String userMySQL, final String pwdMySQL) {
        this.username = new SimpleStringProperty(username);
        this.emailAddress = new SimpleStringProperty(emailAddress);
        this.pwdEmailAddress = new SimpleStringProperty(pwdEmailAddress); 
        this.urlIMAP = new SimpleStringProperty(urlIMAP); 
        this.urlSMTP = new SimpleStringProperty(urlSMTP);
        this.portIMAP = new SimpleStringProperty(portIMAP);
        this.portSMTP = new SimpleStringProperty(portSMTP); 
        this.urlMySQL = new SimpleStringProperty(urlMySQL); 
        this.database = new SimpleStringProperty(database);
        this.portMySQL = new SimpleStringProperty(portMySQL); 
        this.userMySQL = new SimpleStringProperty(userMySQL);
        this.pwdMySQL = new SimpleStringProperty(pwdMySQL);
    }
    
    /**
     * @return the username
     */
    public final String getUsername() {
        return username.get();
    }

    /**
     * @param username the username to set
     */
    public final void setUsername(final String username) {
        this.username.set(username);
    }
    
    public final StringProperty usernameProperty() {
        return username;
    }

    /**
     * @return the emailAddress
     */
    public final String getUserEmailAddress() {
        return emailAddress.get();
    }

    /**
     * @param emailAddress
     */
    public final void setEmailAddress(final String userEmailAddress) {
        this.emailAddress.set(userEmailAddress);
    }
    
    public final StringProperty userEmailAddressProperty() {
        return emailAddress;
    }

    /**
     * @return the pwdEmailAddress
     */
    public final String getPwdEmailAddress() {
        return pwdEmailAddress.get();
    }

    /**
     * @param pwdEmailAddress the pwdEmailAddress to set
     */
    public final void setPwdEmailAddress(final String pwdEmailAddress) {
        this.pwdEmailAddress.set(pwdEmailAddress);
    }
    
    public final StringProperty pwdEmailAddressProperty() {
        return pwdEmailAddress;
    }
    
    /**
     * @return the urlIMAP
     */
    public final String getUrlIMAP() {
        return urlIMAP.get();
    }

    /**
     * @param urlIMAP the urlIMAP to set
     */
    public final void setUrlIMAP(final String urlIMAP) {
        this.urlIMAP.set(urlIMAP);
    }
    
    public final StringProperty urlIMAPProperty() {
        return urlIMAP;
    }
    
    /**
     * @return the urlSMTP
     */
    public final String getUrlSMTP() {
        return urlSMTP.get();
    }

    /**
     * @param urlSMTP the urlSMTP to set
     */
    public final void setUrlSMTP(final String urlSMTP) {
        this.urlSMTP.set(urlSMTP);
    }
    
    public final StringProperty urlSMTPProperty() {
        return urlSMTP;
    }
    
    /**
     * @return the portIMAP
     */
    public final String getPortIMAP() {
        return portIMAP.get();
    }
    
    /**
     * @param portIMAP the portIMAP to set
     */
    public final void setPortIMAP(final String portIMAP) {
        this.portIMAP.set(portIMAP);
    }
    
    public final StringProperty portIMAPProperty() {
        return portIMAP;
    }
    
    /**
     * @return the portSMTP
     */
    public final String getPortSMTP() {
        return portSMTP.get();
    }

    /**
     * @param portSMTP the portSMTP to set
     */
    public final void setPortSMTP(final String portSMTP) {
        this.portSMTP.set(portSMTP);
    }
    
    public final StringProperty portSMTPProperty() {
        return portSMTP;
    }
    
    /**
     * @return the urlMySQL
     */
    public final String getUrlMySQL() {
        return urlMySQL.get();
    }

    /**
     * @param urlMySQL the urlMySQL to set
     */
    public final void setUrlMySQL(final String urlMySQL) {
        this.urlMySQL.set(urlMySQL);
    }
    
    public final StringProperty urlMySQLProperty() {
        return urlMySQL;
    }
    
    /**
     * @return the database
     */
    public final String getDatabase() {
        return database.get();
    }

    /**
     * @param database the database to set
     */
    public final void setDatabase(final String database) {
        this.database.set(database);
    }
    
    public final StringProperty databaseProperty() {
        return database;
    }
    
    /**
     * @return the portMySQL
     */
    public final String getPortMySQL() {
        return portMySQL.get();
    }

    /**
     * @param portMySQL the portMySQL to set
     */
    public final void setPortMySQL(final String portMySQL) {
        this.portMySQL.set(portMySQL);
    }
    
    public final StringProperty portMySQLProperty() {
        return portMySQL;
    }
    
    /**
     * @return the userMySQL
     */
    public final String getUserMySQL() {
        return userMySQL.get();
    }

    /**
     * @param userMySQL the userMySQL to set
     */
    public final void setUserMySQL(final String userMySQL) {
        this.userMySQL.set(userMySQL);
    }
    
    public final StringProperty userMySQLProperty() {
        return userMySQL;
    }
    
    /**
     * @return the pwdMySQL
     */
    public final String getPwdMySQL() {
        return pwdMySQL.get();
    }

    /**
     * @param pwdMySQL the pwdMySQL to set
     */
    public final void setPwdMySQL(final String pwdMySQL) {
        this.pwdMySQL.set(pwdMySQL);
    }
    
    public final StringProperty pwdMySQLProperty() {
        return pwdMySQL;
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.username);
        hash = 29 * hash + Objects.hashCode(this.emailAddress);
        hash = 29 * hash + Objects.hashCode(this.pwdEmailAddress);
        hash = 29 * hash + Objects.hashCode(this.urlIMAP);
        hash = 29 * hash + Objects.hashCode(this.urlSMTP);
        hash = 29 * hash + Objects.hashCode(this.portIMAP);
        hash = 29 * hash + Objects.hashCode(this.portSMTP);
        hash = 29 * hash + Objects.hashCode(this.urlMySQL);
        hash = 29 * hash + Objects.hashCode(this.database);
        hash = 29 * hash + Objects.hashCode(this.portMySQL);
        hash = 29 * hash + Objects.hashCode(this.userMySQL);
        hash = 29 * hash + Objects.hashCode(this.pwdMySQL);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MailConfigBean other = (MailConfigBean) obj;
        if (!Objects.equals(this.username, other.username)) {
            return false;
        }
        if (!Objects.equals(this.emailAddress, other.emailAddress)) {
            return false;
        }
        if (!Objects.equals(this.pwdEmailAddress, other.pwdEmailAddress)) {
            return false;
        }
        if (!Objects.equals(this.urlIMAP, other.urlIMAP)) {
            return false;
        }
        if (!Objects.equals(this.urlSMTP, other.urlSMTP)) {
            return false;
        }
        if (!Objects.equals(this.portIMAP, other.portIMAP)) {
            return false;
        }
        if (!Objects.equals(this.portSMTP, other.portSMTP)) {
            return false;
        }
        if (!Objects.equals(this.urlMySQL, other.urlMySQL)) {
            return false;
        }
        if (!Objects.equals(this.database, other.database)) {
            return false;
        }
        if (!Objects.equals(this.portMySQL, other.portMySQL)) {
            return false;
        }
        if (!Objects.equals(this.userMySQL, other.userMySQL)) {
            return false;
        }
        if (!Objects.equals(this.pwdMySQL, other.pwdMySQL)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PropertyBean{userName=").append(username);
        sb.append(", emailAddress=").append(emailAddress);
        sb.append(", mailPassword=").append(pwdEmailAddress);
        sb.append(", imapURL=").append(urlIMAP);
        sb.append(", smtpURL=").append(urlSMTP);
        sb.append(", imapPort=").append(portIMAP);
        sb.append(", smtpPort=").append(portSMTP);
        sb.append(", mysqlURL=").append(urlMySQL);
        sb.append(", mysqlDatabase=").append(database);
        sb.append(", mysqlPort=").append(portMySQL);
        sb.append(", mysqlUser=").append(userMySQL);
        sb.append(", mysqlPassword=").append(pwdMySQL);
        sb.append('}');
        return sb.toString();
    }
}

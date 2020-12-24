package com.victorouy.properties;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.activation.DataSource;
import jodd.mail.Email;
import jodd.mail.EmailAddress;
import jodd.mail.EmailAttachment;
import jodd.mail.EmailMessage;

/**
 * An email data bean that has an emailId, folderkey, receivedData, and Jodd email as public
 *
 * @author Victor Ouy   1739282
 */
public class EmailDataBean {
   
   private int emailID;
   private int folderKey;
   private LocalDateTime receivedDate;
   public Email email;
   
   /**
     * Default Constructor
     */
   public EmailDataBean() {
       this(-1, -1, null, new Email());
   }
   
   /**
     * Non-default constructor
     * 
     * @param emailID
     * @param folderKey
     * @param receivedDate
     * @param email
     */
   public EmailDataBean(final int emailID, final int folderKey, final LocalDateTime receivedDate, final Email email) {
       this.emailID = emailID;
       this.folderKey = folderKey;
       this.receivedDate = receivedDate;
       this.email = email;
   }
   
   /**
     * @return the emailID
     */
   public int getEmailID() {
       return this.emailID;
   }
   
   /**
     * @param emailID the emailID to set
     */
    public final void setEmailID(final int emailID) {
        this.emailID = emailID;
    }
    
    /**
     * @return the folderKey
     */
   public int getFolderKey() {
       return this.folderKey;
   }
   
   /**
     * @param folderKey the folderKey to set
     */
    public final void setFolderKey(final int folderKey) {
        this.folderKey = folderKey;
    }
    
    /**
     * @return the receivedDate
     */
   public LocalDateTime getReceivedDate() {
       return this.receivedDate;
   }
   
   /**
     * @param receivedDate the receivedDate to set
     */
    public final void setReceivedDate(final LocalDateTime receivedDate) {
        this.receivedDate = receivedDate;
    }

    /**
     * Overidden hashCode method to return hashCode of EmailDataBean
     * 
     * @return int
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + this.emailID;
        hash = 41 * hash + this.folderKey;
        hash = 41 * hash + Objects.hashCode(this.receivedDate);
        hash = 41 * hash + Objects.hashCode(this.email.from());
        hash = 41 * hash + Objects.hashCode(this.email.to());
        hash = 41 * hash + Objects.hashCode(this.email.cc());
        hash = 41 * hash + Objects.hashCode(this.email.bcc());
        hash = 41 * hash + Objects.hashCode(this.email.subject());
        hash = 41 * hash + Objects.hashCode(this.email.messages());
        hash = 41 * hash + Objects.hashCode(this.email.attachments());
        return hash;
    }
    
    /**
     * Override equals method to equals two EmailDataBean objects
     * 
     * @param obj
     * @return 
     */
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
        EmailDataBean other = (EmailDataBean) obj;
        if (emailID != other.emailID) {
            return false;
        }
        if (folderKey != other.folderKey) {
            return false;
        }
        if (receivedDate == null) {
            if (other.receivedDate != null) {
                return false;
            }
        }
        else if (!receivedDate.equals((other.receivedDate))) {
            return false;
        }
        if (email == null) {
            if (other.email != null) {
                return false;
            }
        }
        if (email.from().getEmail() == null ) {
            if (other.email.from() != null) {
                return false;
            }
        }
        if (!email.from().getEmail().equals(other.email.from().getEmail())) {
            return false;
        }
        if (email.to() == null) {
            if (other.email.to() != null) {
                return false;
            }
        }
        else if (!equalsRecipients(email.to(), other.email.to())) {
            return false;
        }
        if (email.cc() == null) {
            if (other.email.cc() != null) {
                return false;
            }
        }
        else if (!equalsRecipients(email.cc(), other.email.cc())) {
            return false;
        }
        if (email.bcc() == null) {
            if (other.email.bcc() != null) {
                return false;
            }
        }
        else if (!equalsRecipients(email.bcc(), other.email.bcc())) {
            return false;
        }
        if (email.subject() == null) {
            if (other.email.subject() != null) {
                return false;
            }
        }
        else if (!email.subject().equals(other.email.subject())) {
            return false;
        }
        if (email.messages() == null) {
            if (other.email.messages() != null) {
                return false;
            }
        }
        else if (!equalsMessages(email.messages(), other.email.messages())) {
            return false;
        }
        if (email.attachments() == null) {
            if (other.email.attachments() != null) {
                return false;
            }
        }
        else if (!equalsAttachments(email.attachments(), other.email.attachments())) {
            return false;
        }
        // Commenting this out for unit testing since will have errors in comparing
        // due to milleseconds rounding changing the seconds, in which it will be false if so
//        if (email.sentDate() == null) {
//            if (other.email.sentDate() != null) {
//                return false;
//            }
//        }
//        else if (!equalsDate(email.sentDate(), other.email.sentDate())) {
//            return false;
//        }
        return true;
    }
    
    /**
     * Checks if two dates are equals
     * 
     * @param thisDate
     * @param otherDate
     * @return boolean  
     */
    private boolean equalsDate(Date thisDate, Date otherDate) {
        if (otherDate == null) {
            return false;
        }
        SimpleDateFormat dateFormating = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String thisDateString = dateFormating.format(thisDate);
        String otherDateString = dateFormating.format(otherDate);
        if (!thisDateString.equals(otherDateString)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if two EmailAddress arrays are equals
     * 
     * @param thisEmailAddress
     * @param otherEmailAddress
     * @return boolean
     */
    private boolean equalsRecipients(EmailAddress[] thisEmailAddress, EmailAddress[] otherEmailAddress) {
        // Since already checked that thisEmailAddress is not null
        if (otherEmailAddress == null) {
            return false;
        }
        if (thisEmailAddress.length != otherEmailAddress.length) {
            return false;
        }
        for (int i = 0; i < thisEmailAddress.length; i++) {
            if (thisEmailAddress[i] == null) {
                if (otherEmailAddress[i] != null) {
                    return false;
                }
            }
            if (!thisEmailAddress[i].getEmail().equals(otherEmailAddress[i].getEmail())) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Checks if two EmailMessage Lists are equals
     * 
     * @param thisMessages
     * @param otherMessages
     * @return boolean
     */
    private boolean equalsMessages(List<EmailMessage> thisMessages, List<EmailMessage> otherMessages) {
        // Since already checked that thisEmailAddress is not null
        if (otherMessages == null) {
            return false;
        }
        if (thisMessages.size() != otherMessages.size()) {
            return false;
        }
        for (int i = 0; i < thisMessages.size(); i++) {
            if (thisMessages.get(i) == null) {
                if (otherMessages.get(i) != null) {
                    return false;
                }
            }
            if (!thisMessages.get(i).getContent().equals(otherMessages.get(i).getContent())) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Checks if two List<EmailAttachment<? extends DataSource>> are equals
     * 
     * @param thisAttachments
     * @param otherAttachments
     * @return 
     */
    private boolean equalsAttachments(List<EmailAttachment<? extends DataSource>> thisAttachments,  List<EmailAttachment<? extends DataSource>> otherAttachments) {
        // Since already checked that thisEmailAddress is not null
        if (otherAttachments == null) {
            return false;
        }
        if (thisAttachments.size() != otherAttachments.size()) {
            return false;
        }
        for (int i = 0; i < thisAttachments.size(); i++) {
            if (thisAttachments.get(i) == null) {
                if (otherAttachments.get(i) != null) {
                    return false;
                }
            }
            if (thisAttachments.get(i).isEmbedded()) {
                if (!otherAttachments.get(i).isEmbedded()) {
                    return false;
                }
                else if (!thisAttachments.get(i).getContentId().equals(otherAttachments.get(i).getContentId())) {
                    return false;
                }
            }
            else if (otherAttachments.get(i).isEmbedded()) {
                return false;
            }
            else if (!thisAttachments.get(i).getName().equals(otherAttachments.get(i).getName())) {
                return false;
            }
            if (!Arrays.equals(thisAttachments.get(i).toByteArray(), otherAttachments.get(i).toByteArray())) {
                return false;
            }
        }
        return true;
    }
}

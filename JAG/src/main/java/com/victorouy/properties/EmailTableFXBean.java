/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.victorouy.properties;

import java.time.LocalDateTime;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Email Table FX Bean to bind with the TableView
 *
 * @author Victor Ouy   1730292
 */
public class EmailTableFXBean {
    
    private final IntegerProperty emailId;
    private final StringProperty from;
    private final StringProperty subject;
    private final ObjectProperty date;
    private final IntegerProperty folderKey;
    
    /**
     * Default Constructor
     */
    public EmailTableFXBean() {
        this(-1, "", "", null, -1);
    }
    
    /**
     * Constructor for EmailTableFXBean
     * 
     * @param emailId
     * @param from
     * @param subject
     * @param date 
     */
    public EmailTableFXBean(int emailId, String from, String subject, LocalDateTime date, int folderKey) {
        this.emailId = new SimpleIntegerProperty(emailId);
        this.from = new SimpleStringProperty(from);
        this.subject = new SimpleStringProperty(subject);
        this.date = new SimpleObjectProperty(date);
        this.folderKey = new SimpleIntegerProperty(folderKey);
    }
    
    public final int getEmailId() {
        return this.emailId.get();
    }
    
    public final void setEmailId(int emailId) {
        this.emailId.set(emailId);
    }
    
    public final IntegerProperty emailIdProperty() {
        return this.emailId;
    }
    
    public final String getFrom() {
        return this.from.get();
    }
    
    public final void setFrom(String from) {
        this.from.set(from);
    }
    
    public final StringProperty fromProperty() {
        return this.from;
    }
    
    public final String getSubject() {
        return this.subject.get();
    }
    
    public final void setSubject(String subject) {
        this.subject.set(subject);
    }
    
    public final StringProperty subjectProperty() {
        return this.subject;
    }
    
    public final Object getDate() {
        return this.date.get();
    }
    
    public final void setDate(LocalDateTime date) {
        this.date.set(date);
    }
    
    public final ObjectProperty dateProperty() {
        return this.date;
    }
    
    public final int getFolderKey() {
        return this.folderKey.get();
    }
    
    public final void setFolderKey(int folderKey) {
        this.folderKey.set(folderKey);
    }
    
    public final IntegerProperty folderKeyProperty() {
        return this.folderKey;
    }
}

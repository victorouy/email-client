/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.victorouy.properties;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * A JavaFX bean for as a form of an email to bind with its appropriate TextField
 *
 * @author Victor Ouy   1739282
 */
public class FormFXBean {
    
    private final StringProperty to;
    private final StringProperty cc;
    private final StringProperty bcc;
    private final StringProperty subject;
    
    /**
     * Default constructor
     */
    public FormFXBean() {
        this("", "", "", "");
    }
    
    /**
     * Constructor for FormFXBean
     * 
     * @param to
     * @param cc
     * @param bcc
     * @param subject 
     */
    public FormFXBean(String to, String cc, String bcc, String subject) {
        this.to = new SimpleStringProperty(to);
        this.cc = new SimpleStringProperty(cc);
        this.bcc = new SimpleStringProperty(bcc);
        this.subject = new SimpleStringProperty(subject);
    }
    
    public final String getTo() {
        return this.to.get();
    }
    
    public final void setTo(String to) {
        this.to.set(to);
    }
    
    public final StringProperty toProperty() {
        return this.to;
    }
    
    public final String getCc() {
        return this.cc.get();
    }
    
    public final void setCc(String cc) {
        this.cc.set(cc);
    }
    
    public final StringProperty ccProperty() {
        return this.cc;
    }
    
    public final String getBcc() {
        return this.bcc.get();
    }
    
    public final void setBcc(String bcc) {
        this.bcc.set(bcc);
    }
    
    public final StringProperty bccProperty() {
        return this.bcc;
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
}

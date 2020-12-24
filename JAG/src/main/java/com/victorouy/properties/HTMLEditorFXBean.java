package com.victorouy.properties;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * FX Bean for editor HTML
 *
 * @author Victor Ouy   1730292
 */
public class HTMLEditorFXBean {
    private final StringProperty messageHTML;
    
    /**
     * Default constructor
     */
    public HTMLEditorFXBean() {
        this("");
    }
     
    /**
     * Constructor for HTMLEditorFXBean
     */
    public HTMLEditorFXBean(String messageHTML) {
        this.messageHTML = new SimpleStringProperty(messageHTML);
    }
    
    public final String getMessageHTML() {
        return this.messageHTML.get();
    }
    
    public final void setMessageHTML(String messageHTML) {
        this.messageHTML.set(messageHTML);
    }
    
    public final StringProperty messageHTMLProperty() {
        return this.messageHTML;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.victorouy.properties;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * An FX Bean for the folder tree to bind with the TreeView
 *
 * @author Victor Ouy   1739282
 */
public class FolderTreeFXBean {
    
    private final IntegerProperty folderId;
    private final StringProperty folderName;
    
    /**
     * Default controller
     */
    public FolderTreeFXBean() {
        this(-1, "");
    }
    
    /**
     * Constructor for FolderTreeFXBean
     * 
     * @param folderId
     * @param folderName 
     */
    public FolderTreeFXBean(int folderId, String folderName) {
        this.folderId = new SimpleIntegerProperty(folderId);
        this.folderName = new SimpleStringProperty(folderName);
    }
    
    public final int getFolderId() {
        return folderId.get();
    }
    
    public final void setFolderId(int folderId) {
        this.folderId.set(folderId);
    }
    
    public final IntegerProperty folderIdProperty() {
        return folderId;
    }
    
    public final String getFolderName() {
        return folderName.get();
    }
    
    public final void setFolderName(String folderName) {
        this.folderName.set(folderName);
    }
    
    public final StringProperty folderNameProperty() {
        return folderName;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.victorouy.persistence;

import com.victorouy.exceptions.DeleteMandatoryFolderException;
import com.victorouy.exceptions.FolderNameAlreadyExistsException;
import com.victorouy.exceptions.ForbiddenEmailEditAttempException;
import com.victorouy.exceptions.ForbiddenFolderMoveException;
import com.victorouy.exceptions.InvalidFolderNameException;
import com.victorouy.properties.EmailDataBean;
import com.victorouy.properties.EmailTableFXBean;
import com.victorouy.properties.FolderTreeFXBean;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.ObservableList;
import jodd.mail.ReceivedEmail;

/**
 * Java interface EmailDAO
 *
 * @author Victor Ouy    1739282
 */
public interface EmailDAO {
    // Create
    public int createSendEmail(EmailDataBean emailDataBean) throws SQLException;
    
    public ArrayList<EmailDataBean> createReceivedEmail(ReceivedEmail[] receivedEmails) throws SQLException, IOException ;
    
    public int createSaveEmail(EmailDataBean emailDataBean) throws SQLException;
    
    public FolderTreeFXBean createFolder(String folder) throws SQLException, FolderNameAlreadyExistsException, InvalidFolderNameException;
    
    
    // Read
    public ObservableList<EmailTableFXBean> findAll() throws SQLException, IOException;
    
    public EmailDataBean findByID(int id) throws SQLException, IOException;
    
    public List<EmailDataBean> findSearchedEmail(String search) throws SQLException, IOException;
    
    public List<EmailDataBean> findByFolder(String folderName) throws SQLException, IOException;
    
    public ObservableList<FolderTreeFXBean> findAllFolderNames() throws SQLException;
    
    public List<String> findAttachmentNames(int emailId) throws SQLException;
    
    
    // Update
    public int updateEditDraftEmail(EmailDataBean emailDataBean) throws SQLException, ForbiddenEmailEditAttempException;
    
    public int updateMoveEmailFolder(EmailDataBean emailDataBean, String folderName) throws SQLException, ForbiddenFolderMoveException;
    
    public int updateSendDraftEmail(EmailDataBean emailDataBean) throws SQLException, ForbiddenEmailEditAttempException;
    
    
    // Delete
    public int deleteEmail(int emailId) throws SQLException;
    
    public int deleteFolder(String folderName) throws SQLException, DeleteMandatoryFolderException ;
}
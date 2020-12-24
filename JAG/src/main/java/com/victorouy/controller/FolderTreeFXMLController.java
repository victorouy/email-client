package com.victorouy.controller;

import com.victorouy.business.MailSendingReceiving;
import com.victorouy.exceptions.DeleteMandatoryFolderException;
import com.victorouy.exceptions.FolderNameAlreadyExistsException;
import com.victorouy.exceptions.ForbiddenFolderMoveException;
import com.victorouy.exceptions.InvalidFolderNameException;
import com.victorouy.persistence.EmailDAO;
import com.victorouy.properties.EmailDataBean;
import com.victorouy.properties.EmailTableFXBean;
import com.victorouy.properties.FolderTreeFXBean;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The folder tree controller in which contains the TreeView of the FolderTreeFXBean that
 * allows user to select a specific email folder as well as create and delete folders
 * 
 * @author Victor Ouy   1739282
 */
public class FolderTreeFXMLController {
    
    private final static Logger LOG = LoggerFactory.getLogger(MailSendingReceiving.class);
    
    private FolderTreeFXBean folderTreeFXBean;
    private EmailDAO emailDAO;
    private EmailTableFXMLController emailTableFXMLController;

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // fx:id="emailFXTreeLayout"
    private BorderPane emailFXTreeLayout; // Value injected by FXMLLoader

    @FXML // fx:id="folderFXTreeView"
    private TreeView<FolderTreeFXBean> folderFXTreeView; // Value injected by FXMLLoader
    
    @FXML // fx:id="folderName"
    private TextField folderName; // Value injected by FXMLLoader
    
    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        this.folderTreeFXBean = new FolderTreeFXBean();
        folderFXTreeView.setRoot(new TreeItem<>(folderTreeFXBean));
        folderFXTreeView.setCellFactory((e) -> new TreeCell<FolderTreeFXBean>() {
            @Override
            protected void updateItem(FolderTreeFXBean item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(item.getFolderName());
                    setGraphic(getTreeItem().getGraphic());
                } 
                else {
                    setText("");
                    setGraphic(null);
                }
            }
        });
        LOG.info("Folder Tree FXML Controller initalized");
    }

    /**
     * This adds a folder from the database and GUI given a string folder name the user has inputted
     * 
     * @param event
     * @throws SQLException 
     */
    @FXML
    void doAddFolder(ActionEvent event) throws SQLException {
        try {
            String addFolderName = folderName.getText();
            FolderTreeFXBean folderTreeFXBean = emailDAO.createFolder(addFolderName);
            
            TreeItem<FolderTreeFXBean> item = new TreeItem<>(folderTreeFXBean);
            item.setGraphic(new ImageView(getClass().getResource("/images/folder.png").toExternalForm()));
            folderFXTreeView.getRoot().getChildren().add(item);
            LOG.info("Added folder: " + addFolderName);
        }
        catch (InvalidFolderNameException ex) {
            LOG.error("Invalid folder name");
            errorAlert("errorFolderInvalid", "errorFolder");
        }
        catch (FolderNameAlreadyExistsException ex){
            LOG.error("Folder name already exists");
            errorAlert("errorFolderExists", "errorFolder");
        }
    }
    
    /**
     * This deletes a folder from the database and GUI given a string folder name the user has inputted
     * 
     * @param event
     * @throws SQLException 
     */
    @FXML
    void doDeleteFolder(ActionEvent event) throws SQLException {
        String alertMsg = ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()).getString("alertMsgDel");
        String removeFolderName = folderName.getText();
        Alert alert = new Alert(AlertType.CONFIRMATION, alertMsg + removeFolderName + " ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            if (removeFolderName.isEmpty()) {
                LOG.error("Cannot remove folder with no name");
                errorAlert("errorFolderEmpty", "errorFolder");
            }
            else {
                for (TreeItem<FolderTreeFXBean> treeItemFolderBean : folderFXTreeView.getRoot().getChildren()) {
                    if (treeItemFolderBean.getValue().getFolderName().equals(removeFolderName)) {
                        try {
                            emailDAO.deleteFolder(removeFolderName);
                            folderFXTreeView.getRoot().getChildren().remove(treeItemFolderBean);
                            LOG.info("Deleted folder: " + removeFolderName);
                        }
                        catch (DeleteMandatoryFolderException ex) {
                            LOG.error("Cannot delete mandatory folder");
                            errorAlert("errorFolderRequired", "errorFolder");
                        }
                        return;
                    }
                }
                LOG.error("Folder to delete does not exists");
                errorAlert("errorFolderNotExists", "errorFolder");
            }
        }
    }
    
    /**
     * This enables a email to be dragged in a folder
     * 
     * @param event 
     */
    @FXML
    void dragDropped(DragEvent event) throws SQLException, IOException, ForbiddenFolderMoveException {
        Dragboard db = event.getDragboard();
        boolean success = false;
        Text folderDrop = (Text) event.getTarget();
        if (db.hasString()) {
            // Decision to check catch exception here instead of making it not a valid area to drag a folder in 
            // because it allows the user to know why an email can or cannot be dragged in or out of the draft folder
            try {
                int emailId = Integer.parseInt(db.getString());
                EmailDataBean emailDataBean = emailDAO.findByID(emailId);
                int folderKey = emailDataBean.getFolderKey();
                this.emailDAO.updateMoveEmailFolder(emailDataBean, folderDrop.getText());
                success = true;
                showTreeDetails(folderKey);
            }
            catch (ForbiddenFolderMoveException e) {
                errorAlert("errorFolderMove", "errorDraftHeader");
                return;
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

    /**
     * This shows and enables if an email is allowed to be dragged over a valid area
     * 
     * @param event 
     */
    @FXML
    void dragOver(DragEvent event) {
        try {
            Text folderDrop = (Text) event.getTarget();
            boolean checkValidDrop = folderDrop.getText() != "" && folderDrop.getText() != null;
            if (checkValidDrop && event.getGestureSource() != folderFXTreeView && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        }
        catch (ClassCastException e) {
            // This ClassCastException simply occurs when not dragging over folder name in FolderTreeFXML controller
        }
    }
    
    /**
     * Gets the folder key of the selected folder
     * 
     * @return folderId
     */
    public int getCurrentFolderKey() {
        return this.folderTreeFXBean.getFolderId();
    }
    
    /**
     * The RootEmailFXMLController calls this method to provide a reference to the
     * EmailDAO object.
     *
     * @param emailDAO
     */
    public void setEmailDAO(EmailDAO emailDAO) {
        this.emailDAO = emailDAO;
    }
    
    /**
     * The RootEmailFXMLController calls this method to provide a reference to the
     * EmailTableFXMLController from which it can request a reference to the
     * TreeView. With the TreeView reference it can change the selection in the
     * TableView.
     *
     * @param fishFXTableController
     */
    public void setTableController(EmailTableFXMLController emailTableFXMLController) {
        this.emailTableFXMLController = emailTableFXMLController;
    }
    
    /**
     * Build the tree items from the database
     *
     * @throws SQLException
     */
    public void displayTree() throws SQLException {
        ObservableList<FolderTreeFXBean> folders = emailDAO.findAllFolderNames();

        // Build an item for each folder and add it to the root
        if (folders != null) {
            for (FolderTreeFXBean folder : folders) {
                TreeItem<FolderTreeFXBean> item = new TreeItem<>(folder);
                item.setGraphic(new ImageView(getClass().getResource("/images/folder.png").toExternalForm()));
                folderFXTreeView.getRoot().getChildren().add(item);
                LOG.info("Added tree item folder: " + item.getValue().getFolderName());
            }
        }
        folderFXTreeView.getRoot().setExpanded(true);
        
        folderFXTreeView
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {
            try {
                showTreeDetails(newValue.getValue().getFolderId());
                folderTreeFXBean.setFolderId(newValue.getValue().getFolderId());
            } catch (SQLException ex) {
                java.util.logging.Logger.getLogger(FolderTreeFXMLController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(FolderTreeFXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    
    /**
     * Displays the emails in the clicked/chosen folder but filtering the returned emailTableFXBean 
     * from calling method getEmailDataTable of emailTableFXMLController
     * 
     * @param newValue
     * @throws SQLException
     * @throws IOException 
     */
    public void showTreeDetails(int folderId) throws SQLException, IOException {
        emailTableFXMLController.displayTable();
        TableView<EmailTableFXBean> emailTableBean = emailTableFXMLController.getEmailDataTable();
        
        FilteredList<EmailTableFXBean> filteredListEmailTable = emailTableBean.getItems().filtered(email -> email.getFolderKey() == folderId);
        emailTableBean.setItems(filteredListEmailTable);
        LOG.info("Added emails for folder: " + folderId);
    }
    
    /**
     * Error message popup dialog
     *
     * @param msg
     */
    private void errorAlert(String msg, String header) {
        Alert dialog = new Alert(Alert.AlertType.ERROR);
        dialog.setTitle(ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()).getString("warn"));
        dialog.setHeaderText(ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()).getString(header));
        dialog.setContentText(ResourceBundle.getBundle("MessagesBundle", Locale.getDefault()).getString(msg));
        dialog.show();
    }
}

package com.victorouy.controller;

import com.victorouy.business.MailSendingReceiving;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to for the About WebView that displays an HTML file
 * 
 * @author Victor Ouy   1739282
 */
public class AboutWebViewFXMLController {

    private final static Logger LOG = LoggerFactory.getLogger(MailSendingReceiving.class);
    
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="aboutFXWebView"
    private WebView aboutFXWebView; // Value injected by FXMLLoader

    /**
     * Initializes the controller class. This method is called when you click the About menu item
     */
    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        final String html = "about.html";
        final java.net.URI uri = java.nio.file.Paths.get(html).toAbsolutePath().toUri();
        LOG.info("uri= " + uri.toString());

        // create WebView with specified local content
        aboutFXWebView.getEngine().load(uri.toString());
    }
}

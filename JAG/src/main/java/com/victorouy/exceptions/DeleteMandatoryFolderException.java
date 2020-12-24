/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.victorouy.exceptions;

/**
 * Custom exception when trying to delete a default folder 
*
 * @author Victor Ouy   1739282
 */
public class DeleteMandatoryFolderException extends Exception{
    public DeleteMandatoryFolderException(String errorMessage) {
        super(errorMessage);
    }
    
}

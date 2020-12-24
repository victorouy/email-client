/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.victorouy.exceptions;

/**
 * Custom exception when attempting create a folder using an already existing name
 *
 * @author Victor Ouy   1739282
 */
public class FolderNameAlreadyExistsException extends Exception{
    public FolderNameAlreadyExistsException(String errorMessage) {
        super(errorMessage);
    }
}

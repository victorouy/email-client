/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.victorouy.exceptions;

/**
 * Custom exception when attempting to move an email into draft folder
 *
 * @author Victor Ouy   1739282
 */
public class ForbiddenFolderMoveException extends Exception{
    public ForbiddenFolderMoveException(String errorMessage) {
        super(errorMessage);
    }
    
}

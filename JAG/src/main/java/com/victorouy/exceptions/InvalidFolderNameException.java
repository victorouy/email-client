/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.victorouy.exceptions;

/**
 * Custom exception for attempting to create a folder with an invalid name
 *
 * @author Victor Ouy   1739282
 */
public class InvalidFolderNameException extends Exception{
    public InvalidFolderNameException(String errorMessage) {
        super(errorMessage);
    }
}

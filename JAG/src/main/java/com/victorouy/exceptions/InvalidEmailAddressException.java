/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.victorouy.exceptions;

/**
 * Custom exception for illegal/invalid email address
 *
 * @author Victor Ouy   1739282
 */
public class InvalidEmailAddressException extends Exception{
    public InvalidEmailAddressException(String errorMessage) {
        super(errorMessage);
    }
}

package com.victorouy.exceptions;

/**
 * Custom exception for no email address entered to be sent
 *
 * @author Victor Ouy   1739282
 */
public class AbsentEmailAddressException extends Exception{
    public AbsentEmailAddressException(String errorMessage) {
        super(errorMessage);
    }
}

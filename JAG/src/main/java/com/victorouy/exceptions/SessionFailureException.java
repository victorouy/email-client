package com.victorouy.exceptions;

/**
 * Custom exception for session failure
 *
 * @author Victor Ouy   1739282
 */
public class SessionFailureException extends Exception {
    public SessionFailureException(String errorMessage) {
        super(errorMessage);
    }
}

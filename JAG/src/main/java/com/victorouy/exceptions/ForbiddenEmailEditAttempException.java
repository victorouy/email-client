/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.victorouy.exceptions;

/**
 * Custom exception when attempting to edit email not in "DRAFT" folder
 *
 * @author Victor Ouy   1739282
 */
public class ForbiddenEmailEditAttempException extends Exception{
    public ForbiddenEmailEditAttempException(String errorMessage) {
        super(errorMessage);
    }
}

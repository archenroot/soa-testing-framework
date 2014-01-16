/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.soatf.config;

import com.ibm.soatf.flow.FrameworkExecutionException;

/**
 *
 * @author Ladislav Jech <archenroot@gmail.com>
 */
public class FTPServerNotFoundException extends FrameworkExecutionException {

    /**
     *
     */
    public FTPServerNotFoundException() {
    }

    /**
     *
     * @param message
     */
    public FTPServerNotFoundException(final String message) {
        super(message);
    }

    /**
     *
     * @param cause
     */
    public FTPServerNotFoundException(final Throwable cause) {
        super(cause);
    }

    /**
     *
     * @param message
     * @param cause
     */
    public FTPServerNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

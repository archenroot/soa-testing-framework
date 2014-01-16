/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.soatf.xml;

import com.ibm.soatf.FrameworkException;

/**
 * XML Subsystem exception is directly under highest class because it can be
 * called in any framework space.
 *
 * @author Ladislav Jech <archenroot@gmail.com>
 */
public class XMLSubsystemException extends FrameworkException {

    /**
     * General constructor.
     */
    public XMLSubsystemException() {
        super();
    }

    /**
     * Exception constructor which passes error message.
     *
     * @param message Exception description.
     */
    public XMLSubsystemException(final String message) {
        super(message);
    }

    /**
     * Exception constructor which passes error cause.
     *
     * @param cause Exception cause.
     */
    public XMLSubsystemException(final Throwable cause) {
        super(cause);
    }

    /**
     * Exception constructor which passes both error description and cause as
     * well.
     *
     * @param message Exception cause.
     * @param cause Exception cause.
     */
    public XMLSubsystemException(
            final String message,
            final Throwable cause) {
        super(message, cause);
    }
}

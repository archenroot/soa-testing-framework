/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ibm.soatf.flow;

/**
 *
 * @author user
 */
class FlowExecutionRuntimeException extends RuntimeException {

    public FlowExecutionRuntimeException() {
    }

    public FlowExecutionRuntimeException(String message) {
        super(message);
    }

    public FlowExecutionRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public FlowExecutionRuntimeException(Throwable cause) {
        super(cause);
    }

    public FlowExecutionRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    
    
}

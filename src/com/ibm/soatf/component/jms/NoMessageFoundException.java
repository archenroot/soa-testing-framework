package com.ibm.soatf.component.jms;

import com.ibm.soatf.component.jms.JmsComponentException;

public class NoMessageFoundException extends JmsComponentException {

        public NoMessageFoundException() {}
        
        public NoMessageFoundException(final String message) {
            super(message);
        }
        
        public NoMessageFoundException(final Throwable cause){
            super(cause);
        }
        
        public NoMessageFoundException(final String message, final Throwable cause)
        {
            super(message, cause);
        }
    }

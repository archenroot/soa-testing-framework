package ibm.soatest.jms;

public class NoMessageFoundException extends Exception {

        public NoMessageFoundException() {}
        
        public NoMessageFoundException(String message) {
            super(message);
        }
        
        public NoMessageFoundException(Throwable cause){
            super(cause);
        }
        
        public NoMessageFoundException(String message, Throwable cause)
        {
            super(message, cause);
        }
    }

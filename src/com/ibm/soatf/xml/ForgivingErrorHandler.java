package com.ibm.soatf.xml;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author zANGETSu
 */
public class ForgivingErrorHandler implements ErrorHandler {

    @Override
    public void warning(SAXParseException ex) {
        System.err.println(ex.getMessage());
    }

    @Override
    public void error(SAXParseException ex) {
        System.err.println(ex.getMessage());
    }

    @Override
    public void fatalError(SAXParseException ex) throws SAXException {
        throw ex;
    }

}
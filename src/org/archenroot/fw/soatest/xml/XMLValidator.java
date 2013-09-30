/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archenroot.fw.soatest.xml;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author zANGETSu
 */
public class XMLValidator {

    private ValidatorResult validatorResult = null;
    //private List<ValidatorResult> validatorResults = null;

    public boolean validateXMLFiles(String xmlFilesFolder, String xsdFileName) {
        //List<ValidatorResult> lvr;
        //lvr = new List<ValidatorResult>() {};
        for (final File fileEntry : new File(xmlFilesFolder).listFiles()) {
            if (fileEntry.isDirectory()) {
                //listFilesForFolder(fileEntry);
            } else if(!fileEntry.getName().contains(".xsd")){
                try {
                    System.out.println("going to validate file: " + fileEntry.getName());
                    SchemaFactory factory
                            = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                    Schema schema = factory.newSchema(new File(xsdFileName));

                    // Parse an XML document into a DOM tree.
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setSchema(schema);
                    dbf.setValidating(false);
                    dbf.setNamespaceAware(true);
                    DocumentBuilder parser
                            = dbf.newDocumentBuilder();
                    Document document = parser.parse(new File("test/jms/" + fileEntry.getName()));
                    Validator validator = schema.newValidator();

                    // Validate the DOM tree.
                    validator.validate(new DOMSource(document));
                    System.out.println("The XML file " + fileEntry.getName() + " is valid.");
                    //validatorResult.valid = true;
                    /*validatorResult.validatorMessage = "XML document "
                            + fileEntry.getName()
                            + " is valid when compared against "
                            + xsdFileName
                            + " XML schema file.";*/
                } catch (ParserConfigurationException e) {
                    // exception handling
                    //validatorResult.valid = false;
                    //validatorResult.validatorMessage = e.getLocalizedMessage();
                    System.out.println("XML file " + fileEntry.getName() + " is not valid.");
                    System.out.println(e.getLocalizedMessage());
                } catch (SAXException e) {
                    System.out.println("XML file " + fileEntry.getName() + " is not valid.");
                    System.out.println(e.getLocalizedMessage());
                    // exception handling - document not valid!
                    //validatorResult.valid = false;
                    //validatorResult.validatorMessage = e.getLocalizedMessage();
                } catch (IOException e) {
                    System.out.println("XML file " + fileEntry.getName() + " is not valid.");
                    System.out.println(e.getLocalizedMessage());
                    // exception handling
                    //validatorResult.valid = false;
                    //validatorResult.validatorMessage = e.getLocalizedMessage();
                }
            }
        }

        return true;

    }

    public ValidatorResult validateXMLFile(String xmlFileName, String xsdFileName) {

        try {

            SchemaFactory factory
                    = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(xsdFileName));

            // Parse an XML document into a DOM tree.
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setSchema(schema);
            dbf.setValidating(false);
            dbf.setNamespaceAware(true);
            DocumentBuilder parser
                    = dbf.newDocumentBuilder();

            Document document = parser.parse(new File(xmlFileName));
            // Create a Validator object, which can be used to validate
            // an instance document.
            Validator validator = schema.newValidator();

            // Validate the DOM tree.
            validator.validate(new DOMSource(document));
            System.out.println("The XML file is valid.");
            validatorResult.valid = true;
            validatorResult.validatorMessage = "XML document "
                    + xmlFileName
                    + " is valid when compared against "
                    + xsdFileName
                    + " XML schema file.";
            System.out.println("alfa");

        } catch (ParserConfigurationException e) {
            // exception handling
            validatorResult.valid = false;
            validatorResult.validatorMessage = e.getLocalizedMessage();
        } catch (SAXException e) {
            // exception handling - document not valid!
            validatorResult.valid = false;
            validatorResult.validatorMessage = e.getLocalizedMessage();
        } catch (IOException e) {
            // exception handling
            validatorResult.valid = false;
            validatorResult.validatorMessage = e.getLocalizedMessage();
        }

        return this.validatorResult;
    }

    public void validateXMLMessage() {
    }

    public class ValidatorResult {

        private String messageFileName = null;
        private boolean valid = false;
        private String validatorMessage = null;
        
        ValidatorResult(){
            
        }
        public ValidatorResult(boolean valid, String validatorMessage) {
            this.valid = valid;
            this.validatorMessage = validatorMessage;
        }

        public ValidatorResult(String messageFileName, boolean valid, String validatorMessage) {
            this.messageFileName = messageFileName;
            this.valid = valid;
            this.validatorMessage = validatorMessage;
        }

        public String getMessageFileName() {
            return messageFileName;
        }

        public boolean isValid() {
            return valid;
        }

        public String getValidatorMessage() {
            return validatorMessage;
        }

        public void setMessageFileName(String messageFileName) {
            this.messageFileName = messageFileName;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public void setValidatorMessage(String validatorMessage) {
            this.validatorMessage = validatorMessage;
        }

    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.soatf.xml;

import com.ibm.soatf.component.soap.builder.XmlUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xmlbeans.XmlException;
import org.w3c.dom.Document;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

/**
 *
 * @author zANGETSu
 */
public class XMLValidator {

    //private ValidatorResult validatorResult = null;
    //private List<ValidatorResult> validatorResults = null;

    public static boolean validateXMLFiles(String xmlFilesFolder, String xsdFileName) {
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

    public static boolean validateXMLFile(String xmlFileName, String xsdFileName) throws SAXException, ParserConfigurationException, IOException, XmlException {
        final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        factory.setResourceResolver(new MySchemaResolver());        
        File schemaFile = new File(xsdFileName);
        if (!schemaFile.exists()){
            throw new NullPointerException(xsdFileName+" schema file was not found.");
            //System.out.println("Alfa");
        }
        
        final InputStream is = XmlUtils.fixSchemaFile(new File(xsdFileName));        
        final Source schemaSource = new StreamSource(is);
        schemaSource.setSystemId(xsdFileName);
        Schema schema = factory.newSchema(schemaSource);        

        // Parse an XML document into a DOM tree.
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setSchema(schema);
        dbf.setValidating(false);
        dbf.setNamespaceAware(true);
        DocumentBuilder parser = dbf.newDocumentBuilder();

        Document document = parser.parse(new File(xmlFileName));
        // Create a Validator object, which can be used to validate
        // an instance document.
        Validator validator = schema.newValidator();

        // Validate the DOM tree.
        validator.validate(new DOMSource(document));
        try {
            if (is != null) is.close();
        } catch(IOException e) {;}

        return true;
    }


    /*public class ValidatorResult {

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

    }*/
    
    public static class MySchemaResolver implements LSResourceResolver {
        @Override
        public LSInput resolveResource(final String type,
                final String namespaceURI, final String publicId, String systemId,
                final String baseURI) {

            try {
                final File resourcePath = new File(new URL(baseURI).toURI()).getParentFile();
                final File resourceFile = new File(resourcePath, systemId);
                final InputStream is = XmlUtils.fixSchemaFile(resourceFile);                
                final LSInput input = new DOMInputImpl();
                input.setPublicId(publicId);
                input.setSystemId(systemId);                
                input.setBaseURI(resourceFile.toURI().toString());
                input.setByteStream(is);
                return input;                
            } catch (  MalformedURLException ex) {
                System.out.println(ex);
            } catch (    URISyntaxException | XmlException | IOException ex) {
                System.out.println(ex);
            }
            return null;
        }
    }
}

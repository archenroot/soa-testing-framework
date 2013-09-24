/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archenroot.fw.soatest.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author zANGETSu
 */
public class XMLValidator {

    public static boolean validateXMLFile(String xmlFileName, String xsdFileName) {
        try {

            // Parse an XML document into a DOM tree.
            DocumentBuilder parser
                    = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = parser.parse(new File("soa-testing-framework-config.xml"));
            System.out.println(new File(".").getCanonicalPath());
            System.out.println(new File("soa-testing-framework-config.xml").exists());
            StreamSource source = new StreamSource(new File("soa-testing-framework-config.xml"));
            
            // Create a SchemaFactory capable of understanding WXS schemas.
            SchemaFactory factory
                    = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // Load a WXS schema, represented by a Schema instance.
            Source schemaFile = new StreamSource(new File("EndPointType.xsd"));
            Schema schema = factory.newSchema(schemaFile);
            

            // Create a Validator object, which can be used to validate
            // an instance document.
            Validator validator = schema.newValidator();
            
            // Validate the DOM tree.
            StreamResult streamResult = new StreamResult();
            //domResult.
            validator.validate(source,streamResult);
            Writer w = streamResult.getWriter();
            StringWriter sw = (StringWriter) w;
            System.out.println(streamResult.toString());
            
            
            System.out.println("alfa");
        } catch (ParserConfigurationException e) {
            // exception handling
        } catch (SAXException e) {
            // exception handling - document not valid!
        } catch (IOException e) {
            // exception handling
        }
        return false;
    }
    public void validateXMLMessage(){
        
    }
}

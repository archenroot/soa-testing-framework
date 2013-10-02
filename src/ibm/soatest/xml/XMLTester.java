/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ibm.soatest.xml;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author zANGETSu
 */
public class XMLTester {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            XMLValidator xmlVal = new XMLValidator();
            //xmlVal.validateXMLFile();
            SchemaFactory factory
                    = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File("PremiseContact.xsd"));
            
            // Parse an XML document into a DOM tree.
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            //Source schemaFile = new StreamSource(new File("PremiseContact.xsd"));
            dbf.setSchema(schema);
            dbf.setValidating(false);
            dbf.setNamespaceAware(true);
            DocumentBuilder parser
                   = dbf.newDocumentBuilder();
            
            Document document = parser.parse(new File("PremiseContact.xml"));
            //Source source = new StreamSource(new File("PremiseContact.xml"));
            // Create a SchemaFactory capable of understanding WXS schemas.
            
            // Load a WXS schema, represented by a Schema instance.
        
            // Create a Validator object, which can be used to validate
            // an instance document.
            Validator validator = schema.newValidator();
            
            // Validate the DOM tree.
            
            validator.validate(new DOMSource(document));
            System.out.println("The XML file is valid.");
        } catch (SAXException ex) {
            Logger.getLogger(XMLTester.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(XMLTester.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(XMLTester.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

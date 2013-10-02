/*
 * Copyright (C) 2013 zANGETSu
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package ibm.soatest.soap;

import ibm.soatest.xml.XPathNamespaceContext;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Scanner;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument.Schema;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author zANGETSu
 */
public class JAXPProcessor {

    public static Object getSoapBodyContent(String soapMessage) throws SAXException, IOException, XPathExpressionException, ParserConfigurationException {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this!
        domFactory.setValidating(false);
        domFactory.setSchema(null);
        domFactory.setIgnoringComments(true);
        domFactory.setIgnoringElementContentWhitespace(true);
       
        //domFactory.setSchema(null);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        //Document doc = builder.parse("books.xml");
        boolean exists = (new File (soapMessage)).exists();
        Document doc = builder.parse(soapMessage);
        
        String content = new Scanner(new File(soapMessage)).useDelimiter("\\Z").next();
        System.out.println("File content: " + content);
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        XPathNamespaceContext nsContext = new XPathNamespaceContext();
        nsContext.addNamespace("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");
        nsContext.addNamespace("sen", "http://www.example.org/SendJMS");
        xpath.setNamespaceContext(nsContext);
        XPathExpression expr = xpath.compile("/soapenv:Envelope/soapenv:Body/sen:sendJMSMessage/sen:messageContent");
        
        Object result = expr.evaluate(doc, XPathConstants.NODE);
        System.out.println("Namespace URI: " + ((Node) result).getNamespaceURI());
        System.out.println("Prefix: " + ((Node) result).getPrefix());
        System.out.println(((Node) result).getLocalName());
        XMLOutputter xmlOutputter = new XMLOutputter();
        //xmlOutputter.outputString((Element) result);
        System.out.println(expr.evaluate(doc));
        return result;
    }
    
    
}

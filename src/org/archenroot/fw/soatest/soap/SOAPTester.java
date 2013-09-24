/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archenroot.fw.soatest.soap;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.support.components.RequestXmlDocument;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessagePart;
import com.eviware.soapui.model.iface.MessagePart.PartType;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.AbstractModelItem;
import com.eviware.soapui.support.SoapUIException;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import org.apache.xmlbeans.XmlException;

/**
 *
 * @author zANGETSu
 */
public class SOAPTester {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Request.SubmitException {
        // create new project
        WsdlProject project;
        // import amazon wsdl
        WsdlInterface iface;
        WsdlOperation operation;
        // create a new empty request for that operation
        WsdlRequest request;
        final ModelItem mi = new ModelItem() {

            @Override
            public String getName() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public String getId() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public ImageIcon getIcon() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public String getDescription() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Settings getSettings() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public List<? extends ModelItem> getChildren() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public ModelItem getParent() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void addPropertyChangeListener(String string, PropertyChangeListener pl) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener pl) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener pl) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void removePropertyChangeListener(String string, PropertyChangeListener pl) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        WsdlSubmit submit;
        Response response;
        String content;
        try {

            project = new WsdlProject();
            iface = WsdlInterfaceFactory.importWsdl(project, "http://prometheus:11001/HudsonDemo/proxy/SendJMSQueue?wsdl", true)[0];
            // get desired operation
            operation = (WsdlOperation) iface.getOperationByName("sendJMSMessage");

            // create a new empty request for that operation
            request = operation.addNewRequest("My request");

            /*MessagePart mp[] =  request.getRequestParts();
            PartType pt = mp[0].getPartType().CONTENT;
            
            System.out.println("Part 0: "+ mp[0].getPartType().CONTENT.toString());
            System.out.println("Part 0: "+ mp[0].getPartType().CONTENT);
            */
            
            
            // generate the request content from the schema
            request.setRequestContent(operation.createRequest(true));
            //request.setRequestContent("AA");
            
            System.out.println("SOAP Request: " + request.toString());
            RequestXmlDocument rXmlDocument = new RequestXmlDocument(request);
            System.out.println("Request content: " + rXmlDocument.getXml());
            // submit the request
            submit = (WsdlSubmit) request.submit(new WsdlSubmitContext(mi), false);

            // wait for the response
            response = submit.getResponse();

            // print the response
            content = response.getContentAsString();
            String xmlContent = response.getContentAsXml();
            System.out.println(content);

            //assertNotNull(content);
            //assertTrue(content.indexOf("404 Not Found") > 0);
            //tssb.getSample(com.bea.wli.sb.typesystem.config.SchemaDocument.Schema schema)
            operation.release();
            iface.release();
            project.release();

        } catch (XmlException ex) {
            Logger.getLogger(SOAPTester.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SOAPTester.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SoapUIException ex) {
            Logger.getLogger(SOAPTester.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            content = null;
            response = null;
            submit = null;
            request = null;
        }
    }

    private static void assertNotNull(String content) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static void assertTrue(boolean b) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

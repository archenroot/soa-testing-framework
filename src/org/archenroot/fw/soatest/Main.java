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
package org.archenroot.fw.soatest;

import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.support.SoapUIException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.ListIterator;
import javax.wsdl.WSDLException;
import org.apache.xmlbeans.XmlException;
import org.archenroot.fw.soatest.database.DatabaseTestComponent;
import org.archenroot.fw.soatest.database.DatabaseTestComponent.CRUDType;
import static org.archenroot.fw.soatest.database.DatabaseTestComponent.CRUDType.INSERT;
import org.archenroot.fw.soatest.database.UnknownCRUDTypeException;
import org.archenroot.fw.soatest.soap.SoapTestComponent;
import org.archenroot.fw.soatest.soap.UnknownFlowDirectionTypeException;
import org.ow2.easywsdl.schema.api.Element;
import org.ow2.easywsdl.schema.api.Schema;
import org.ow2.easywsdl.schema.api.SchemaException;
import org.ow2.easywsdl.schema.api.SchemaReader;
import org.ow2.easywsdl.schema.api.Type;
import org.ow2.easywsdl.schema.api.extensions.NamespaceMapperImpl;
import org.ow2.easywsdl.wsdl.WSDLFactory;
import org.ow2.easywsdl.wsdl.api.Description;
import org.ow2.easywsdl.wsdl.api.WSDLReader;
import org.ow2.easywsdl.wsdl.impl.wsdl11.WSDLReaderImpl;
import org.w3c.dom.Document;

/**
 *
 * @author zANGETSu
 */
public class Main {

    public static void main(String[] args) throws IOException, UnknownCRUDTypeException, WSDLException, Request.SubmitException, SoapUIException, XmlException, UnknownFlowDirectionTypeException, org.ow2.easywsdl.wsdl.api.WSDLException, URISyntaxException, SchemaException {
        String path = new File(".").getCanonicalPath().toString()
                + "\\xml-resources\\jaxb\\SOATFConfiguration\\soa-testing-framework-config.xml";
        boolean fileExists = new File(path).exists();
        System.out.println(new File(new File(".").getCanonicalPath().toString()
                + "\\xml-resources\\jaxb\\SOATFConfiguration\\soa-testing-framework-config.xml").exists());
        SOATestingFramework soaTF = new SOATestingFramework("\\xml-resources\\jaxb\\SOATFConfiguration\\soa-testing-framework-config.xml");
        SOATestingFrameworkConfiguration soaTFConfig
                = new SOATestingFrameworkConfiguration(
                new File(".").getCanonicalPath().toString()
                + "\\xml-resources\\jaxb\\SOATFConfiguration\\soa-testing-framework-config.xml");
        //soaTFConfig.getDatabaseType();
        //DatabaseTestComponent dtc = new DatabaseTestComponent(soaTFConfig.getDatabaseType());
        //dtc.generateSQLStatement(CRUDType.INSERT);

        //WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
        /*
        WSDLReaderImpl r = new WSDLReaderImpl();
        Description mydesc = r.read(new URL("file:///c:/Dev/svn-rep/trunk/osb/ContractsMaintainFromEBS/Resources/WSDLs/ContractMaintainReceiveFromEBSForMaximo.wsdl"));
        SchemaReader sreader = r.getSchemaReader();
        //Listmydesc.getImports()
        Schema schema = sreader.read(new URL("file:///c:/Dev/svn-rep/trunk/osb/ContractsMaintainFromEBS/Resources/WSDLs/ContractMaintainReceiveFromEBSForMaximo.wsdl"));
        NamespaceMapperImpl impl = schema.getAllNamespaces();
        URI uri = schema.getDocumentURI();
        ListIterator types = schema.getTypes().listIterator();
        while (types.hasNext()){
            Object type = types.next();
            System.out.println(((Type) type).toString());
        }
        /*
        
        ListIterator elements = schema.getElements().listIterator();
        int i = 0;
        List mylist = schema.getElements();
        while (elements.hasNext()) {
            Object element = elements.next();
            System.out.println(((Element) element).toString());
        }
        */
        //Description desc = reader.read(new URL("file:///c:/Dev/svn-rep/trunk/osb/ContractsMaintainFromEBS/Resources/WSDLs/ContractMaintainReceiveFromEBSForMaximo.wsdl"));
        // Write a WSDL 1.1 or 2.0 (depend of desc version)
        //Document doc = WSDLFactory.newInstance().newWSDLWriter().getDocument(desc);
        //String s= doc.getTextContent();
        SoapTestComponent stc = new SoapTestComponent("SendJMSQueue",
                // "http://prometheus:11001/HudsonDemo/proxy/SendJMSQueue", 
                "http://prometheus:11001/HudsonDemo/proxy/SendJMSQueue",
                "sendJMSMessage",
                "soapRequest.xml",
                "soapResponse.xml");
        //stc.getAndSaveXmlSchemaFromWsdl();
        stc.generateSoapEnvelopeRequest();
        stc.invokeService();
        stc.validateMessage(SoapTestComponent.FlowDirectionType.INBOUND);
        stc.validateMessage(SoapTestComponent.FlowDirectionType.OUTBOUND);
        System.out.println("completed.");
    }
}

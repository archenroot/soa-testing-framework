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

import com.eviware.soapui.impl.wsdl.support.UrlSchemaLoader;
import com.eviware.soapui.impl.wsdl.support.wsdl.AbstractWsdlDefinitionLoader;
import com.eviware.soapui.impl.wsdl.support.wsdl.CachedWsdlLoader;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlDefinitionLoader;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlLoader;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.x.dialogs.XProgressMonitor;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.wsdl.WSDLException;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.archenroot.fw.soatest.database.UnknownCRUDTypeException;
import org.archenroot.fw.soatest.soap.JAXPProcessor;
import org.archenroot.fw.soatest.soap.SoapTestComponent;
import org.archenroot.fw.soatest.soap.UnknownFlowDirectionTypeException;
import org.ow2.easywsdl.schema.api.SchemaException;
import org.xml.sax.InputSource;

/**
 *
 * @author zANGETSu
 */
public class Main {

    public static void main(String[] args) throws IOException, UnknownCRUDTypeException, WSDLException, Request.SubmitException, SoapUIException, XmlException, UnknownFlowDirectionTypeException, org.ow2.easywsdl.wsdl.api.WSDLException, URISyntaxException, SchemaException, Exception {
        String path = new File(".").getCanonicalPath().toString()
                + "\\xml-resources\\jaxb\\SOATFConfiguration\\soa-testing-framework-config.xml";
        boolean fileExists = new File(path).exists();
        System.out.println(new File(new File(".").getCanonicalPath().toString()
                + "\\xml-resources\\jaxb\\SOATFConfiguration\\soa-testing-framework-config.xml").exists());
        SoaTestingFramework soaTF = new SoaTestingFramework("\\xml-resources\\jaxb\\SOATFConfiguration\\soa-testing-framework-config.xml");
        SoaTestingFrameworkConfiguration soaTFConfig
                = new SoaTestingFrameworkConfiguration(
                new File(".").getCanonicalPath().toString()
                + "\\xml-resources\\jaxb\\SOATFConfiguration\\soa-testing-framework-config.xml");
        //soaTFConfig.getDatabaseType();
        //DatabaseTestComponent dtc = new DatabaseTestComponent(soaTFConfig.getDatabaseType());
        //dtc.generateSQLStatement(CRUDType.INSERT);
        /*
        WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
        
        WSDLReaderImpl r = new WSDLReaderImpl();
        //Description mydesc = r.read(new URL("file:///c:/Dev/svn-rep/trunk/osb/ContractsMaintainFromEBS/Resources/WSDLs/ContractMaintainReceiveFromEBSForMaximo.wsdl"));
        Description desc = r.read(new URL("http://prometheus:11001/HudsonDemo/proxy/SendJMSQueue"));
        
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
        */
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
        //stc.getAndSaveSoapEnvelopeRequest();
        //stc.invokeService();
        JAXPProcessor.getSoapBodyContent("soapRequest.xml");
        
        
        stc.isSoapRequestEnvelopeValid();
        stc.validateMessage(SoapTestComponent.FlowDirectionType.INBOUND);
        stc.validateMessage(SoapTestComponent.FlowDirectionType.OUTBOUND);
        System.out.println("completed.");
        UrlSchemaLoader sl = new UrlSchemaLoader(stc.getSoapEndPointUri());
        XmlObject xo = sl.loadXmlObject(stc.getSoapEndPointUri() + "?wsdl", null);
        xo.save(new File("testXMLOject.xml"));
        CachedWsdlLoader cwl = new CachedWsdlLoader(stc.getWsdlInterface());
        cwl.saveDefinition(".");
        System.out.println("Latest import: " + cwl.getLatestImportURI());
;        System.out.println( stc.getWsdlContext().hasSchemaTypes());
        SchemaTypeSystem sts = stc.getWsdlContext().getSchemaTypeSystem();
        SchemaType st[] = sts.documentTypes();
        
       List allSeenTypes = new ArrayList();
 allSeenTypes.addAll(Arrays.asList(sts.documentTypes()));
 //allSeenTypes.addAll(Arrays.asList(sts.attributeTypes()));
 //allSeenTypes.addAll(Arrays.asList(sts.globalTypes()));
 for (int i = 0; i < allSeenTypes.size(); i++)
 {
     SchemaType sType = (SchemaType)allSeenTypes.get(i);
     System.out.println("Visiting " + sType.toString());
     
     allSeenTypes.addAll(Arrays.asList(sType.getAnonymousTypes()));
 }
        
        
        
        System.exit(0);
        
        
    }
}

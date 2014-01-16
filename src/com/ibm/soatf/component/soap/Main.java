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
package com.ibm.soatf.component.soap;

import com.ibm.soatf.component.soap.builder.SoapContext;
import com.ibm.soatf.component.soap.builder.SoapLegacyFacade;
import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;

/**
 *
 * @author Ladislav Jech <archenroot@gmail.com>
 */
public class Main {
    public static void main(String[] args) {
        try {
            //vytvorenie reqeustu (v msg)
            SoapLegacyFacade facade = new SoapLegacyFacade(new URL("http://localdev:7003/osb/PurchaseRequisitionConnectorMaximo/PurchaseRequisitionConnectorMaximoPS?wsdl"));
            Binding binding = facade.getBindingByName(new QName("http://www.example.org/OEBS_BGIPRIInterface/","OEBS_BGIPRIInterfaceSOAP"));
            List<QName> list = facade.getBindingNames();
            for (QName n : list) {
                System.out.println(">>>>>>");
                System.out.println(n.toString());
            }
            BindingOperation operation = binding.getBindingOperation("PurchaseRequisition", null, null);
            String msg = facade.buildSoapMessageFromInput(binding, operation, SoapContext.DEFAULT);
            System.out.println(msg);
            System.out.println("----------------------------------------------------------");       
            //validacia requestu
            facade.validateSoapRequestMessage(binding, operation, msg, false);
            System.out.println("----------------------------------------------------------");
            //poslanie requestu a ziskanie response v resMsg
            JAXWSDispatch jaxwsDispatch = new JAXWSDispatch();
            SOAPMessage res = jaxwsDispatch.invoke("http://localdev:7003/osb/PurchaseRequisitionConnectorMaximo/PurchaseRequisitionConnectorMaximoPS", msg);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            res.writeTo(baos );
            String resMsg = baos.toString();            
            System.out.println(resMsg);
            System.out.println("----------------------------------------------------------");  
            //validacia response
            facade.validateSoapResponseMessage(binding, operation, resMsg, false);
            
            
            /*
            SoapLegacyFacade facade = new SoapLegacyFacade(new URL("http://localdev:7003/osb/PurchaseRequisitionConnectorMaximo/PurchaseRequisitionConnectorMaximoPS?wsdl"));
            Binding binding = facade.getBindingByName(new QName("http://www.example.org/OEBS_BGIPRIInterface/","OEBS_BGIPRIInterfaceSOAP"));
            SoapBuilder builder = new SoapBuilder(facade, binding, SoapContext.DEFAULT);
            SoapOperationFinder finder = new SoapOperationFinder(builder, binding);
            finder.name("PurchaseRequisition");
            SoapOperationBuilder operationBuilder = finder.find();
            String msg = builder.buildInputMessage(operationBuilder);
            System.out.println(msg);
            builder.validateInputMessage(operationBuilder, msg);    
            */
            
        } catch (WSDLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   
}

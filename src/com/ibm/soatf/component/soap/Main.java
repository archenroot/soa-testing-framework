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
import org.apache.xmlbeans.XmlObject;

/**
 *
 * @author Ladislav Jech <archenroot@gmail.com>
 */
public class Main {
    public static void main(String[] args) {
        try {
            String xmlmsg = "<max:PublishIWWPRNPOINTS xmlns:max=\"http://www.ibm.com/maximo\" creationDateTime=\"2008-09-29T02:49:45\" baseLanguage=\"aeoliam venit\" transLanguage=\"ventos tempestatesque\" messageID=\"temperat iras\" maximoVersion=\"turbine corripuit\" event=\"0\">\n" +
"         <max:IWWPRNPOINTSSet>\n" +
"            <max:LOCATIONS action=\"Delete\" relationship=\"nimborum in\" deleteForInsert=\"foedere certo\" transLanguage=\"profundum quippe ferant\">\n" +
"               <max:MAXINTERRORMSG>et carcere</max:MAXINTERRORMSG>\n" +
"               <max:BGADDRESSLINE1 changed=\"1\">speluncis abdidit</max:BGADDRESSLINE1>\n" +
"               <max:BGADDRESSLINE2 changed=\"1\">flammas turbine</max:BGADDRESSLINE2>\n" +
"               <max:BGADDRESSLINE3 changed=\"1\">ac vinclis</max:BGADDRESSLINE3>\n" +
"               <max:BGADDRESSLINE4 changed=\"1\">aris imponet honorem</max:BGADDRESSLINE4>\n" +
"               <max:BGADDRESSLINE5 changed=\"1\">claustra fremunt</max:BGADDRESSLINE5>\n" +
"               <max:BGADDRESSLINE6 changed=\"1\">quisquam numen</max:BGADDRESSLINE6>\n" +
"               <max:BGCOUNTRY changed=\"1\">ac vinclis</max:BGCOUNTRY>\n" +
"               <max:BGENDUSSEC changed=\"1\">pectore flammas</max:BGENDUSSEC>\n" +
"               <max:BGGISBUILDINGID changed=\"1\">certo et</max:BGGISBUILDINGID>\n" +
"               <max:BGGISDATE changed=\"1\">2004-12-06T03:41:44+00:00</max:BGGISDATE>\n" +
"               <max:BGGISLOCALAUTHORITY changed=\"1\">ac terras</max:BGGISLOCALAUTHORITY>\n" +
"               <max:BGGISPROJECTNUMBER changed=\"1\">infixit acuto</max:BGGISPROJECTNUMBER>\n" +
"               <max:BGGPCONNTYPE changed=\"1\">montis insuper</max:BGGPCONNTYPE>\n" +
"               <max:IWMETERREADDAY changed=\"1\">mollitque animos</max:IWMETERREADDAY>\n" +
"               <max:IWNACEID changed=\"1\">soror et coniunx</max:IWNACEID>\n" +
"               <max:IWPOSTAIM152 changed=\"1\">adorat praeterea</max:IWPOSTAIM152>\n" +
"               <max:IWPREMDEPT changed=\"1\">aris imponet honorem</max:IWPREMDEPT>\n" +
"               <max:IWPREMISECLASS changed=\"1\">premere et</max:IWPREMISECLASS>\n" +
"               <max:IWPREMISEID changed=\"1\">ferant rapidi secum</max:IWPREMISEID>\n" +
"               <max:IWPREMISETYPE changed=\"1\">vasto rex</max:IWPREMISETYPE>\n" +
"               <max:IWPREMORGNAME changed=\"1\">tempestatesque sonoras</max:IWPREMORGNAME>\n" +
"               <max:LOCATION changed=\"1\">premit ac</max:LOCATION>\n" +
"               <max:SITEID changed=\"1\">nubibus ignem</max:SITEID>\n" +
"               <max:STATUS maxvalue=\"austris aeoliam venit\" changed=\"1\">atris hoc metuens</max:STATUS>\n" +
"               <max:STATUSDATE changed=\"1\">2010-04-16T23:33:23+01:00</max:STATUSDATE>\n" +
"            </max:LOCATIONS>\n" +
"         </max:IWWPRNPOINTSSet>\n" +
"      </max:PublishIWWPRNPOINTS>";
            XmlObject xmlObject = XmlObject.Factory.parse(xmlmsg);

            /*String namespaces = declareXPathNamespaces(xmlObject);
            if (namespaces != null && namespaces.trim().length() > 0)
                xPath = namespaces + xPath;*/
            String xqNamespace = "declare namespace max='http://www.ibm.com/maximo'";

            XmlObject[] path = xmlObject.selectPath(xqNamespace+" $this//*[local-name()=\"PublishIWWPRNPOINTS\"]");
            if (path == null || path.length != 1 || path[0].getDomNode() == null) {
                return;
            }
            System.out.println(path[0]);
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
            SOAPMessage res = jaxwsDispatch.invoke(new URL("http://localdev:7003/osb/PurchaseRequisitionConnectorMaximo/PurchaseRequisitionConnectorMaximoPS"), msg);
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

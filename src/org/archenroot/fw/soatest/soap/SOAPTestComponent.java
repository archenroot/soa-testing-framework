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
package org.archenroot.fw.soatest.soap;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.support.components.RequestXmlDocument;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.impl.wsdl.submit.transports.http.WsdlResponse;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.support.SoapUIException;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import javax.swing.ImageIcon;
import org.apache.xmlbeans.XmlException;

/**
 *
 * @author zANGETSu
 */

public class SOAPTestComponent {

    // Variables coming from configuration xml file
    private String serviceName = "";
    private String endPointUri = "";
    private String operationName = "";
    private String requestMessageXmlFile = "";
    private String responseMessageXmlFile = "";

    // Soap related dynamic variables
    private WsdlProject project = null;
    private WsdlInterface wsdlInterface = null;
    private WsdlOperation wsdlOperation = null;
    private WsdlSubmit wsdlSubmit = null;
    private WsdlRequest soapRequest = null;
    private WsdlResponse soapResponse = null;
    private RequestXmlDocument soapRequestXmlDocument = null;
    private ModelItem modelItem = null;
    private static String wsdlUriSuffix = "?wsdl";
        
    // Other minor variables
    private static String newRequestDefaultName = "New SOAP Request";
    private String responseMessageContent = "";

    private enum FlowDirectionType{INBOUND, OUTBOUND};
    
    private SOAPTestComponent() {
    }
    
    public SOAPTestComponent(
            String serviceName,
            String endPointUri,
            String operationName,
            String requestMessageXmlFile,
            String responseMessageXmlFile) {
        this.serviceName = serviceName;
        this.endPointUri = endPointUri;
        this.operationName = operationName;
        this.requestMessageXmlFile = requestMessageXmlFile;
        this.responseMessageXmlFile = responseMessageXmlFile;
    }

    public void generateSoapEnvelopeRequest() throws Request.SubmitException, XmlException, IOException, SoapUIException {
        
        this.modelItem = this.getDefaultModelItem();
        this.project = new WsdlProject();
        this.project.setName(this.serviceName);
        this.wsdlInterface = WsdlInterfaceFactory.importWsdl(this.project,
                this.endPointUri + this.wsdlUriSuffix,
                true)[0];
        // get desired operation
        this.wsdlOperation = (WsdlOperation) wsdlInterface.getOperationByName(this.operationName);

        // create a new empty request for that operation
        this.soapRequest = wsdlOperation.addNewRequest(this.newRequestDefaultName);

        // generate the request content from the schema
        this.soapRequest.setRequestContent(wsdlOperation.createRequest(true));
        //request.setRequestContent("AA");
        this.soapRequestXmlDocument = new RequestXmlDocument(this.soapRequest);
        saveContentToFile(this.soapRequestXmlDocument.getXml(),this.requestMessageXmlFile);
     
    }

    
    public void invokeService() throws Request.SubmitException, IOException {
           // submit the request
        wsdlSubmit = (WsdlSubmit) this.soapRequest.submit(new WsdlSubmitContext(this.modelItem), false);

        // wait for the response
        soapResponse = (WsdlResponse) wsdlSubmit.getResponse();

        // print the response
        responseMessageContent = soapResponse.getContentAsXml();
        
        saveContentToFile(responseMessageContent, this.responseMessageXmlFile);

        //assertNotNull(content);
        //assertTrue(content.indexOf("404 Not Found") > 0);
        //tssb.getSample(com.bea.wli.sb.typesystem.config.SchemaDocument.Schema schema)
        wsdlOperation.release();
        wsdlInterface.release();
        project.release();

    }

    private ModelItem getDefaultModelItem() {
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
        return mi;

    }
    
    public boolean validateMessage(FlowDirectionType fdt) throws UnknownFlowDirectionTypeException{
        switch(fdt){
            case INBOUND:
                break;
            case OUTBOUND:
                break;
            default:
                throw new UnknownFlowDirectionTypeException("Unknown FlowDirectionType value: supported values are "
                        + FlowDirectionType.INBOUND.toString() + ", "
                        + FlowDirectionType.OUTBOUND.toString() + ", "
                        );
        } 
        
        return true;
    }
    
    private void saveContentToFile(String content, String fileName) throws IOException{
        FileWriter fw = new FileWriter(new File(fileName));
        fw.write(content);
        fw.flush();
        fw.close();
    }

}

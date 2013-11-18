package com.ibm.soatf.soap;


import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.support.components.RequestXmlDocument;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.impl.wsdl.submit.transports.http.WsdlResponse;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlLoader;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlValidator;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.support.SoapUIException;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import javax.swing.ImageIcon;
import javax.wsdl.WSDLException;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import com.ibm.soatf.soap.UnknownFlowDirectionTypeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class SOAP{
    
    private static final Logger logger = LogManager.getLogger(SOAP.class);
    
// Variables coming from configuration xml file
    private String soapServiceName = "";
    private String soapEndPointUri = "";
    private String soapOperationName = "";
    private String requestMessageXmlFile = "";
    private String responseMessageXmlFile = "";

    // Soap related dynamic variables
    private WsdlProject wsdlProject = null;
    private WsdlLoader wsdlLoader = null;
    private WsdlContext wsdlContext = null;
    private WsdlInterface wsdlInterface = null;
    private WsdlOperation wsdlOperation = null;
    private WsdlSubmit wsdlSubmit = null;
    private WsdlRequest wsdlRequest = null;
    private WsdlResponse wsdlResponse = null;
    private WsdlValidator wsdlValidator = null;
    private RequestXmlDocument soapRequestXmlDocument = null;
    private ModelItem modelItem = null;
    private static String wsdlUriSuffix = "?wsdl";

    private static String newRequestDefaultName = "New SOAP Request";
    private String responseMessageContent = "";
    private List<XmlError> validatorErrors = null;

    public SOAP(
            String serviceName,
            String endPointUri,
            String operationName,
            String requestMessageXmlFile,
            String responseMessageXmlFile) throws SoapUIException, XmlException, IOException {
        logger.debug("Start SOAP class.");
        this.soapServiceName = serviceName;
        this.soapEndPointUri = endPointUri;
        this.soapOperationName = operationName;
        this.requestMessageXmlFile = requestMessageXmlFile;
        this.responseMessageXmlFile = responseMessageXmlFile;
        
        
        this.modelItem = this.getDefaultModelItem();
        this.wsdlProject = new WsdlProject();
        this.wsdlProject.setName(this.soapServiceName);
        this.wsdlInterface = WsdlInterfaceFactory.importWsdl(this.wsdlProject,
                this.soapEndPointUri + SOAP.wsdlUriSuffix,
                true)[0];
        this.wsdlContext = new WsdlContext(this.soapEndPointUri, this.wsdlInterface);
        // get desired operation
        this.wsdlValidator = new WsdlValidator(wsdlContext);
        this.wsdlOperation = (WsdlOperation) wsdlInterface.getOperationByName(this.soapOperationName);
        logger.debug("exiting system.");
        
        // create a new empty request for that operation
        this.wsdlRequest = wsdlOperation.addNewRequest(SOAP.newRequestDefaultName);
        // generate the request content from the schema
        this.wsdlRequest.setRequestContent(wsdlOperation.createRequest(true));
        //request.setRequestContent("AA");
        this.soapRequestXmlDocument = new RequestXmlDocument(this.wsdlRequest);

    }

    public void getAndSaveSoapEnvelopeRequest() throws Request.SubmitException, XmlException, IOException, SoapUIException {
        this.saveContentToFile(this.soapRequestXmlDocument.getXml(), this.requestMessageXmlFile);
    }

    public boolean isSoapRequestEnvelopeValid() throws FileNotFoundException, IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(this.requestMessageXmlFile));
        StringBuffer stringBuffer = new StringBuffer();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuffer.append(line).append("\n");
        }
        RequestXmlDocument rxd = new RequestXmlDocument(wsdlRequest);
        rxd.setXml(stringBuffer.toString());

        //this.wsdlValidator.validateXml(rxd.getXml(), this.validatorErrors);
        this.wsdlValidator.validateXml("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:sen=\"http://www.example.org/SendJMS\">\n" +
"   <soapenv:Header/>\n" +

"</soapenv:Envelope>", this.validatorErrors);

        if (this.validatorErrors == null) {
            return true;
        } else {
            return false;
        }
    }

    public List<XmlError> getXmlErrors() {
        return this.validatorErrors;
    }

    public void invokeService() throws Request.SubmitException, IOException {
        // submit the request
        wsdlSubmit = (WsdlSubmit) this.wsdlRequest.submit(new WsdlSubmitContext(this.modelItem), false);
        // wait for the response
        wsdlResponse = (WsdlResponse) wsdlSubmit.getResponse();
        // print the response
        responseMessageContent = wsdlResponse.getContentAsXml();
        saveContentToFile(responseMessageContent, this.responseMessageXmlFile);
        //assertNotNull(content);
        //assertTrue(content.indexOf("404 Not Found") > 0);
        //tssb.getSample(com.bea.wli.sb.typesystem.config.SchemaDocument.Schema schema)
        wsdlOperation.release();
        wsdlInterface.release();
        wsdlProject.release();

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

    public void getAndSaveXmlSchemaFromWsdl() throws WSDLException, IOException {
        
        
        /*
        javax.wsdl.xml.WSDLReader wsdlReader11 = javax.wsdl.factory.WSDLFactory.newInstance().newWSDLReader();
        Definition def = wsdlReader11.readWSDL(this.soapEndPointUri + this.wsdlUriSuffix);
        org.w3c.dom.Element elt = null;
        //org.w3c.dom.Node node = null;
        javax.xml.bind.Element elt1 = null;

        Map messMap = def.getMessages();
        SchemaDocument myschema;
        for (Object o : def.getTypes().getExtensibilityElements()) {
            if (o instanceof javax.wsdl.extensions.schema.Schema) {
                elt = ((javax.wsdl.extensions.schema.Schema) o).getElement();

// Navigate in the DOM model of the schema
                // You can use Schema#getImport() to work with imports
                saveContentToFile(elt.getTextContent(), "schema.xsd");

                System.out.println("alfa");
            }
        }*/
    }

    //public boolean validateMessage(FlowDirectionType fdt) throws UnknownFlowDirectionTypeException {
        /*
        switch (fdt) {
            case INBOUND:
                //XMLValidator.validateXMLFile(this.requestMessageXmlFile, "schema.xsd");
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
                */
    

    private void saveContentToFile(String content, String fileName) throws IOException {
        FileWriter fw = new FileWriter(new File(fileName));
        fw.write(content);
        fw.flush();
        fw.close();
    }

    public String getSoapServiceName() {
        return soapServiceName;
    }

    public String getSoapEndPointUri() {
        return soapEndPointUri;
    }

    public String getSoapOperationName() {
        return soapOperationName;
    }

    public String getRequestMessageXmlFile() {
        return requestMessageXmlFile;
    }

    public String getResponseMessageXmlFile() {
        return responseMessageXmlFile;
    }

    public WsdlProject getWsdlProject() {
        return wsdlProject;
    }

    public WsdlContext getWsdlContext() {
        return wsdlContext;
    }

    public WsdlInterface getWsdlInterface() {
        return wsdlInterface;
    }

    public WsdlOperation getWsdlOperation() {
        return wsdlOperation;
    }

    public WsdlSubmit getWsdlSubmit() {
        return wsdlSubmit;
    }

    public WsdlRequest getWsdlRequest() {
        return wsdlRequest;
    }

    public WsdlResponse getWsdlResponse() {
        return wsdlResponse;
    }

    public WsdlValidator getWsdlValidator() {
        return wsdlValidator;
    }

    public RequestXmlDocument getSoapRequestXmlDocument() {
        return soapRequestXmlDocument;
    }

    public ModelItem getModelItem() {
        return modelItem;
    }

    public static String getWsdlUriSuffix() {
        return wsdlUriSuffix;
    }

    public static String getNewRequestDefaultName() {
        return newRequestDefaultName;
    }

    public String getResponseMessageContent() {
        return responseMessageContent;
    }

    public List<XmlError> getValidatorErrors() {
        return validatorErrors;
    }
    
    
}
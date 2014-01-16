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

import com.ibm.soatf.FrameworkException;
import com.ibm.soatf.flow.FrameworkExecutionException;
import com.ibm.soatf.component.AbstractSoaTFComponent;
import com.ibm.soatf.component.SOATFCompType;
import com.ibm.soatf.component.soa.ServiceManager;
import com.ibm.soatf.component.soap.builder.SoapContext;
import com.ibm.soatf.component.soap.builder.SoapLegacyFacade;
import com.ibm.soatf.component.soap.builder.SoapValidationException;
import com.ibm.soatf.component.soap.builder.XmlUtils;
import com.ibm.soatf.config.iface.soap.EnvelopeConfig;
import com.ibm.soatf.config.iface.soap.SOAPConfig;
import com.ibm.soatf.config.master.Operation;
import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance;
import com.ibm.soatf.flow.OperationResult;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

/**
 * Class responsible for any operations related to work by using SOAP protocol or
 * even management on objects which are just SOAP based.
 * 
 * @author Ladislav Jech <archenroot@gmail.com>
 */
public class SOAPComponent extends AbstractSoaTFComponent {

    private final static Logger logger = LogManager.getLogger(SOAPComponent.class);
    
    public static final String URI_SEPARATOR = "/";
    public static final String DEFAULT_PROTO = "http";
    public static final String WSDL_SUFFIX = "?wsdl";
    public static final String NAME_DELIMITER = "_";
    public static final String REQUEST_FILE_SUFFIX = "req.xml";
    public static final String RESPONSE_FILE_SUFFIX = "res.xml";
    
    private OracleFusionMiddlewareInstance masterOFMConfig;
    private SOAPConfig soapIfaceConfig;

    private String identificator;
    private String serviceName;
    private String operationName;
    
    private String servicetype; //need to be added to configuration schema
    private boolean status;
    private String serviceURI;
    private String adminHost;
    private int adminPort;
    private String username;
    private String password;
    private String jmxProtocol;
    private String requestContent;

    private OracleFusionMiddlewareInstance.AdminServer adminServer;
    private OracleFusionMiddlewareInstance.Cluster cluster;
    private OracleFusionMiddlewareInstance.Cluster.ManagedServer managedServer;

    private String serviceLocactionType;
    private String serviceSOAType;
    
    private final Map<String, SOAPConfig.EnvelopeConfig.Element> customValues = new HashMap<>();
    
    private final OperationResult cor;
    private List<EnvelopeConfig.Element> envelopeElements;

    public SOAPComponent(
            OracleFusionMiddlewareInstance soapMasterConfig,
            SOAPConfig soapIfaceConfig,
            File workingDir,
            List<EnvelopeConfig.Element> envelopeElements) {
        super(SOATFCompType.SOAP);
        this.masterOFMConfig = soapMasterConfig;
        this.soapIfaceConfig = soapIfaceConfig;
        this.workingDir = workingDir;
        this.envelopeElements = envelopeElements;
        cor = OperationResult.getInstance();
        constructComponent();
    }

    @Override
    protected final void constructComponent() {
        this.adminServer = this.masterOFMConfig.getAdminServer();
        this.serviceURI = this.soapIfaceConfig.getEndPointUri();
        this.servicetype = this.soapIfaceConfig.getServiceSOAType();
        this.adminHost = this.masterOFMConfig.getAdminServer().getHost();
        this.adminPort = this.masterOFMConfig.getAdminServer().getPort();
        this.managedServer = this.masterOFMConfig.getCluster().getManagedServer();
        this.username = this.masterOFMConfig.getAdminServer().getSecurityPrincipal();
        this.password = this.masterOFMConfig.getAdminServer().getSecurityCredentials();
        //this.requestContent = this.soapIfaceConfig.getRequestContent();

        this.serviceName = soapIfaceConfig.getServiceName();
        this.operationName = soapIfaceConfig.getOperationName();
        this.serviceLocactionType = soapIfaceConfig.getServiceLocationType();
        this.serviceSOAType = soapIfaceConfig.getServiceSOAType();

        for (EnvelopeConfig.Element el : envelopeElements) {
            customValues.put(el.getElementXpath(), el);
        }
                
    }

    @Override
    protected void executeOperation(Operation operation) throws FrameworkExecutionException  {
        /*if (!SOAP_OPERATIONS.contains(operation.getName())) {
         final String msg = "Unsupported operation: " + operation.getName() + ". Valid operations are: " + SOAP_OPERATIONS;
         logger.error(msg);
         cor.addMsg(msg);
         cor.setOverallResultSuccess(false);
         } else {
         */
        switch (operation.getName()) {
            case SOAP_DISABLE_SERVICE:
                disableService();
                break;
            case SOAP_ENABLE_SERVICE:
                enableService();
                break;
            case SOAP_GENERATE_ENVELOPE:
                generateEnvelope();
                break;
            case SOAP_INVOKE_SERVICE:
                //generateEnvelope();
                invokeServiceWithProvidedSOAPRequest();
                break;
            case SOAP_VALIDATE_REQUEST:
                validateSOAPMessage(false);
                break;
            case SOAP_VALIDATE_RESPONSE:
                validateSOAPMessage(true);
                break;
            case SOAP_RESPONSE_OK:
                checkSOAPMessage(true);
                break;
            case SOAP_RESPONSE_FAULT:
                checkSOAPMessage(false);
                break;
            default:
                logger.info("Operation execution not yet implemented: " + operation.getName());
                cor.addMsg("Operation: " + operation.getName() + " is valid, but not yet implemented");
        }
    }

    private void disableService() throws FrameworkExecutionException {
        try {
            boolean result = ServiceManager.changeServiceStatus(
                    servicetype, false,
                    serviceURI, adminHost, adminPort, username, password);
            if (result) {
                cor.addMsg("OSB " + servicetype + " " + serviceURI + " has been disabled trough admin server " + adminHost + ":" + adminPort + ".");
                cor.markSuccessful();
            } else {
                cor.addMsg("OSB " + servicetype + " " + serviceURI + " cannot be desibled been disabled trough admin server" + adminHost + ":" + adminPort + ".");
            }
        } catch (FrameworkExecutionException ex) {
            String msg = "OSB Service cannot be disabled:\n" + ExceptionUtils.getFullStackTrace(ex);
            cor.addMsg(msg);
            throw new FrameworkExecutionException(ex);
        }
    }

    private void enableService() throws FrameworkExecutionException {
        try {
            boolean result = ServiceManager.changeServiceStatus(
                    servicetype, true,
                    serviceURI, adminHost, adminPort, username, password);
            cor.addMsg("OSB " + servicetype + " " + serviceURI + " running at " + adminHost + " has been enabled.");
            if (result) {
                cor.addMsg("Service succesfully enabled.");
                cor.markSuccessful();
            } else {
                cor.addMsg("Failed to enable the service.");
            }
        } catch (FrameworkExecutionException ex) {
            String msg = "OSB Service cannot be enabled:\n" + ExceptionUtils.getFullStackTrace(ex);
            cor.addMsg(msg);
            throw new FrameworkExecutionException(ex);
        }
    }
    
    private Binding findBindingForOperationName(SoapLegacyFacade facade, String operationName) throws SoapComponentException {
        if(operationName == null || facade == null) {
            throw new SoapComponentException("Operation name or endpoint wsdl is missing or corrupted.");
        }
        final List<QName> list = facade.getBindingNames();
        if (list != null && list.size() > 0) {
            Binding binding;
            for (QName bindingName: list) {
                binding = facade.getBindingByName(bindingName);
                if (binding.getBindingOperation(operationName, null, null) != null) {
                    return binding;
                }
            }
        }
        throw new SoapComponentException("Can't find binding for provided operation name: " + operationName); 
    }
    
    public static String getValueFromGeneratedEnvelope(File workingDir, String serviceName, String operationName, String xPath) throws SoapComponentException {
        final String filename = new StringBuilder(serviceName).append(NAME_DELIMITER).append(operationName).append(NAME_DELIMITER).append(REQUEST_FILE_SUFFIX).toString();
        final File file = new File(workingDir, filename);
        try {
            logger.trace("retrieving element "+xPath);
            final String xmlText = FileUtils.readFileToString(file);
            return XmlUtils.getXPathContent(xmlText, xPath);
        } catch (IOException ex) {
            throw new SoapComponentException("Can't find generated envelope file to extract values");
        }
    }

    private void generateEnvelope() throws FrameworkExecutionException {
        try {
            final String filename = new StringBuilder(serviceName).append(NAME_DELIMITER).append(operationName).append(NAME_DELIMITER).append(REQUEST_FILE_SUFFIX).toString();
            final File file = new File(workingDir, filename);
            
            String delimiter = "";
            if (!serviceURI.startsWith("/")) {
                delimiter = "/";
            }
            String envelope;
            if (requestContent != null && requestContent.length() > 0) {
                envelope = requestContent;
            } else {
                final URL url = new URL(DEFAULT_PROTO, managedServer.getHostName(), managedServer.getPort(), delimiter+serviceURI+WSDL_SUFFIX);
                final SoapLegacyFacade facade = new SoapLegacyFacade(url);
                final Binding binding = findBindingForOperationName(facade, operationName);
                final BindingOperation operation = binding.getBindingOperation(operationName, null, null);
                envelope = facade.buildSoapMessageFromInput(binding, operation, SoapContext.DEFAULT);
            }
            for (Entry<String, SOAPConfig.EnvelopeConfig.Element> xpath: customValues.entrySet()) {
                Element e = XmlUtils.getElementForXPath(envelope, xpath.getKey());
                envelope = XmlUtils.setTextToElement(envelope, xpath.getKey(), xpath.getValue().getElementValue());
                List<SOAPConfig.EnvelopeConfig.Element.Attribute> attrs = xpath.getValue().getAttribute();
                if (attrs != null) {
                    for (SOAPConfig.EnvelopeConfig.Element.Attribute attr: attrs) {
                        envelope = XmlUtils.setTextToElementAttribute(envelope, xpath.getKey(), attr.getAttrName(), attr.getAttrValue());
                    }
                }
            }
            
            String msg = "Successfuly generated request envelope for operation: " + operationName;
            cor.addMsg(msg);
   
            if (file.exists()) {
                FileUtils.forceDelete(file);
            }
            
            FileUtils.writeStringToFile(file, envelope);
            msg = "Successfuly stored request envelope for operation: " + operationName + " in file: " + file.getCanonicalPath();
            cor.addMsg(msg);
            cor.markSuccessful();
        } catch (Throwable ex) {
            throw new FrameworkExecutionException(ex);
        }
    }
    
    private void invokeServiceWithProvidedSOAPRequest() throws FrameworkExecutionException {
        try {
            final String filename = new StringBuilder(serviceName).append(NAME_DELIMITER).append(operationName).append(NAME_DELIMITER).toString();
            final File requestFile = new File(workingDir, filename + REQUEST_FILE_SUFFIX);
            
            String delimiter = "";
            if (!serviceURI.startsWith("/")) {
                delimiter = "/";
            }
                       
            final URL url = new URL(DEFAULT_PROTO, managedServer.getHostName(), managedServer.getPort(), delimiter+serviceURI);
            final String requestEnvelope = FileUtils.readFileToString(requestFile);
            JAXWSDispatch jaxwsDispatch = new JAXWSDispatch();
            final SOAPMessage res = jaxwsDispatch.invoke(url.toString(), requestEnvelope);
            
            String msg = "Successfuly received response of operation: " + operationName;
            logger.debug(msg);
            cor.addMsg(msg);
            
            final File responseFile = new File(workingDir, filename + RESPONSE_FILE_SUFFIX);
   
            if (responseFile.exists()) {
                FileUtils.forceDelete(responseFile);
            }
            try (FileOutputStream fos = new FileOutputStream(responseFile)) {
                res.writeTo(fos);
            }
            msg = "Successfuly stored response of operation: " + operationName + " in file: " + responseFile.getCanonicalPath();
            logger.debug(msg);
            cor.addMsg(msg);
            cor.markSuccessful();
        } catch (Throwable ex) {
            throw new FrameworkExecutionException(ex);
        }
    }

    @Override
    protected void destructComponent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void validateSOAPMessage(boolean response) throws FrameworkExecutionException {
        String filename = new StringBuilder(serviceName).append(NAME_DELIMITER).append(operationName).append(NAME_DELIMITER).toString();
        String result;
        if (response) {
            filename += RESPONSE_FILE_SUFFIX;
            result = "Response message of operation: " + operationName;
        } else {
            filename += REQUEST_FILE_SUFFIX;
            result = "Request envelope for operation: " + operationName;
        }        
        try {
            final File file = new File(workingDir, filename);
            final String xmlMessage = FileUtils.readFileToString(file);
            
            String delimiter = "";
            if (!serviceURI.startsWith("/")) {
                delimiter = "/";
            }
            
            final URL url = new URL(DEFAULT_PROTO, managedServer.getHostName(), managedServer.getPort(), delimiter+serviceURI+WSDL_SUFFIX);
            final SoapLegacyFacade facade = new SoapLegacyFacade(url);
            final Binding binding = findBindingForOperationName(facade, operationName);
            final BindingOperation operation = binding.getBindingOperation(operationName, null, null);
            
            if (response) {
                facade.validateSoapResponseMessage(binding, operation, xmlMessage, false);
            } else {
                facade.validateSoapRequestMessage(binding, operation, xmlMessage, false);
            }
            
            result += " is valid.";
            logger.debug(result);
            cor.addMsg(result);
            cor.markSuccessful();
        } catch (SoapValidationException e) {
            result += " is invalid.";
            logger.debug(result);
            cor.addMsg(result);
            throw new FrameworkExecutionException(result, e);
        } catch (Throwable ex) {
            throw new FrameworkExecutionException(ex);
        }       
    }

    private void checkSOAPMessage(boolean ok) throws SoapComponentException {
        String filename = new StringBuilder(serviceName).append(NAME_DELIMITER).append(operationName).append(NAME_DELIMITER).append(RESPONSE_FILE_SUFFIX).toString();
        final File file = new File(workingDir, filename);
        InputStream is = null;
        try {
            final byte[] xmlMessage = FileUtils.readFileToByteArray(file);
            is = new ByteArrayInputStream(xmlMessage);            
            SOAPMessage response = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL).createMessage(new MimeHeaders(), is);
            response.removeAllAttachments();
            SOAPEnvelope envp = response.getSOAPPart().getEnvelope();
            SOAPBody someBody = envp.getBody();
            if (ok) {
                if (someBody.getFault() == null) {
                    cor.addMsg("soap body is OK");
                    cor.markSuccessful();
                } else {
                    cor.addMsg("found soap fault in response body");
                    cor.addMsg(new String(xmlMessage));
                }
            } else {
                if (someBody.getFault() != null) {
                    cor.addMsg("found soap fault in response body");
                    cor.markSuccessful();
                } else {
                    cor.addMsg("response body doesn't contain soap fault");
                    cor.addMsg(new String(xmlMessage));
                }
            }
        } catch (IOException | SOAPException ex) {
            logger.debug(ex);
            throw new SoapComponentException("error while trying to parse response");
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    throw new SoapComponentException(ex);
                }
            }
        }
    }
}

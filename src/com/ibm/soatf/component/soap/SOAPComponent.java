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
import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance.Clusters.Cluster.ManagedServer;
import com.ibm.soatf.flow.FrameworkExecutionException;
import com.ibm.soatf.flow.OperationResult;
import com.ibm.soatf.gui.ProgressMonitor;
import com.ibm.soatf.tool.FileSystem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
<<<<<<< HEAD
import java.util.AbstractMap;
import java.util.ArrayList;
=======
>>>>>>> 7c2802d5d20e30d5191a0f8f327cacd09e189422
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    
    private final OracleFusionMiddlewareInstance masterOFMConfig;
    private final SOAPConfig soapIfaceConfig;

    //private String identificator;
    private String serviceName;
    private String operationName;
    
    //private boolean status;
    private String serviceURI;
    private String adminHost;
    private int adminPort;
    private String username;
    private String password;

    private OracleFusionMiddlewareInstance.AdminServer adminServer;
    private OracleFusionMiddlewareInstance.Clusters.Cluster osbCluster;
    private OracleFusionMiddlewareInstance.Clusters.Cluster soaCluster;
    //private OracleFusionMiddlewareInstance.Cluster.ManagedServer managedServer;

    private String serviceLocactionType;
    private String serviceSOAType;
    
<<<<<<< HEAD
    //private final Map<String, SOAPConfig.EnvelopeConfig.Element> customValues = new HashMap<>();
    private final List<Entry<String, SOAPConfig.EnvelopeConfig.Element>> customValues = new ArrayList<>();
=======
    private final Map<String, SOAPConfig.EnvelopeConfig.Element> customValues = new HashMap<>();
>>>>>>> 7c2802d5d20e30d5191a0f8f327cacd09e189422
    
    private final OperationResult cor;
    private final List<EnvelopeConfig.Element> envelopeElements;

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
        this.adminHost = this.masterOFMConfig.getAdminServer().getHost();
        this.adminPort = this.masterOFMConfig.getAdminServer().getPort();
        //this.managedServer = this.masterOFMConfig.getCluster().getManagedServer();
        this.username = this.masterOFMConfig.getAdminServer().getSecurityPrincipal();
        this.password = this.masterOFMConfig.getAdminServer().getSecurityCredentials();
        //this.requestContent = this.soapIfaceConfig.getRequestContent();
        
        final List<OracleFusionMiddlewareInstance.Clusters.Cluster> clusters = this.masterOFMConfig.getClusters().getCluster();
        for (OracleFusionMiddlewareInstance.Clusters.Cluster cluster: clusters) {
            if ("OSB".equals(cluster.getType())) {
                osbCluster = cluster;
            }
            if ("SOA".equals(cluster.getType())) {
                soaCluster = cluster;
            }
        }
        // if only one cluster is set, suppose it is used for both osb and soa operations
        if (soaCluster == null) soaCluster = osbCluster;
        if (osbCluster == null) osbCluster = soaCluster;

        this.serviceName = soapIfaceConfig.getServiceName();
        this.operationName = soapIfaceConfig.getOperationName();
        this.serviceLocactionType = soapIfaceConfig.getServiceLocationType();
        this.serviceSOAType = soapIfaceConfig.getServiceSOAType();

<<<<<<< HEAD
        if (envelopeElements != null) {
            for (EnvelopeConfig.Element el : envelopeElements) {
                //customValues.put(el.getElementXpath(), el);
                customValues.add(new AbstractMap.SimpleEntry<>(el.getElementXpath(), el));
            }
=======
        for (EnvelopeConfig.Element el : envelopeElements) {
            customValues.put(el.getElementXpath(), el);
>>>>>>> 7c2802d5d20e30d5191a0f8f327cacd09e189422
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
                String msg = "Invalid operation name: " + operation.getName();
                logger.error(msg);
                cor.addMsg(msg);
                throw new SoapComponentException(msg);
        }
    }

    private void disableService() throws SoapComponentException {
        boolean result;
        if ("SOACompositeApplication".equals(serviceSOAType)) {
            final ManagedServer managedServer = soaCluster.getManagedServer().get(0);
            result = ServiceManager.changeSoaCompositeApplicationStatus(false,
                serviceURI, managedServer.getHostName(), managedServer.getPort(), username, password);
            if (result) {
                cor.addMsg("SOA Composite Application " + serviceName + " has been disabled trough server " + managedServer.getHostName() + ":" + managedServer.getPort() + ".");
                cor.markSuccessful();
            } else {
                final String msg = "SOA Composite Application " + serviceName + " cannot be disabled trough server" + managedServer.getHostName() + ":" + managedServer.getPort() + ".";
                cor.addMsg(msg);
                throw new SoapComponentException(msg);
            }                
        } else {
            result = ServiceManager.changeOsbServiceStatus(serviceSOAType, false,
                serviceURI, adminHost, adminPort, username, password);
            if (result) {
                cor.addMsg("OSB " + serviceSOAType + " " + serviceURI + " has been disabled trough admin server " + adminHost + ":" + adminPort + ".");
                cor.markSuccessful();
            } else {
                final String msg = "OSB " + serviceSOAType + " " + serviceURI + " cannot be disabled trough admin server" + adminHost + ":" + adminPort + ".";
                cor.addMsg(msg);
                throw new SoapComponentException(msg);
            }                
        }
    }

    private void enableService() throws SoapComponentException {
        boolean result;
        if ("SOACompositeApplication".equals(serviceSOAType)) {
            final ManagedServer managedServer = soaCluster.getManagedServer().get(0);
            result = ServiceManager.changeSoaCompositeApplicationStatus(true,
                serviceURI, managedServer.getHostName(), managedServer.getPort(), username, password);
            if (result) {
                cor.addMsg("SOA Composite Application " + serviceName + " has been enabled trough server " + managedServer.getHostName() + ":" + managedServer.getPort() + ".");
                cor.markSuccessful();
            } else {
                final String msg = "SOA Composite Application " + serviceName + " cannot be enabled trough server" + managedServer.getHostName() + ":" + managedServer.getPort() + ".";
                cor.addMsg(msg);
                throw new SoapComponentException(msg);
            }                 
        } else {
            result = ServiceManager.changeOsbServiceStatus(serviceSOAType, true,
                serviceURI, adminHost, adminPort, username, password);
            if (result) {
                cor.addMsg("OSB " + serviceSOAType + " " + serviceURI + " has been enabled trough admin server " + adminHost + ":" + adminPort + ".");
                cor.markSuccessful();
            } else {
                final String msg = "OSB " + serviceSOAType + " " + serviceURI + " cannot be enabled trough admin server" + adminHost + ":" + adminPort + ".";
                cor.addMsg(msg);
                throw new SoapComponentException(msg);
            }                
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
    
    /*public static String getValueFromGeneratedEnvelope(File workingDir, String serviceName, String operationName, String xPath) throws SoapComponentException {
        final String filename = new StringBuilder(serviceName).append(NAME_DELIMITER).append(operationName).append(NAME_DELIMITER).append(REQUEST_FILE_SUFFIX).toString();
        final File file = new File(workingDir, filename);
        try {
            logger.trace("retrieving element "+xPath);
            final String xmlText = FileUtils.readFileToString(file);
            return XmlUtils.getXPathContent(xmlText, xPath);
        } catch (IOException ex) {
            throw new SoapComponentException("Can't find generated envelope file to extract values.", ex);
        }
    }*/

    private void generateEnvelope() throws SoapComponentException {
        try {
            final String filename = new StringBuilder(serviceName).append(NAME_DELIMITER).append(operationName).append(NAME_DELIMITER).append(REQUEST_FILE_SUFFIX).toString();
            final File file = new File(workingDir, filename);
            
            String delimiter = "";
            if (!serviceURI.startsWith("/")) {
                delimiter = "/";
            }
            final ManagedServer managedServer = osbCluster.getManagedServer().get(0);
            final URL url = new URL(DEFAULT_PROTO, managedServer.getHostName(), managedServer.getPort(), delimiter+serviceURI+WSDL_SUFFIX);
            ProgressMonitor.init(6, "Reading WSDL from the server..."); //there are 2 ProgressMonitor events inside SoapLegacyFacade
            final SoapLegacyFacade facade = new SoapLegacyFacade(url);
            final Binding binding = findBindingForOperationName(facade, operationName);
            final BindingOperation operation = binding.getBindingOperation(operationName, null, null);
            ProgressMonitor.increment("Building envelope...");
            String envelope = facade.buildSoapMessageFromInput(binding, operation, SoapContext.DEFAULT);
            ProgressMonitor.increment("Setting custom values...");
<<<<<<< HEAD
            //for (Entry<String, SOAPConfig.EnvelopeConfig.Element> xpath: customValues.entrySet()) {
            for (Entry<String, SOAPConfig.EnvelopeConfig.Element> xpath: customValues) {
=======
            for (Entry<String, SOAPConfig.EnvelopeConfig.Element> xpath: customValues.entrySet()) {
>>>>>>> 7c2802d5d20e30d5191a0f8f327cacd09e189422
                //Element e = XmlUtils.getElementForXPath(envelope, xpath.getKey());
                envelope = XmlUtils.setTextToElement(envelope, transformXPath(xpath.getKey()), xpath.getValue().getElementValue());
                List<SOAPConfig.EnvelopeConfig.Element.Attribute> attrs = xpath.getValue().getAttribute();
                if (attrs != null) {
                    for (SOAPConfig.EnvelopeConfig.Element.Attribute attr: attrs) {
                        envelope = XmlUtils.setTextToElementAttribute(envelope, transformXPath(xpath.getKey()), attr.getAttrName(), attr.getAttrValue());
                    }
                }
            }
            
            String msg = "Successfuly generated request envelope for operation: " + operationName;
            cor.addMsg(msg);
            ProgressMonitor.increment("Writing file...");
            if (file.exists()) {
                FileUtils.forceDelete(file);
            }
            
            FileUtils.writeStringToFile(file, envelope);
            msg = "Request envelope for operation: " + operationName + " was stored in [FILE: %s]";
            logger.debug(String.format(msg, file.getAbsolutePath()));
            cor.addMsg(msg,"<a href='file://"+file.getAbsolutePath()+"'>" + file.getAbsolutePath() + "</a>", FileSystem.getRelativePath(file));
            cor.markSuccessful();
        } catch (IOException | WSDLException ex) {
            throw new SoapComponentException(ex);
        }
    }
    
    private void invokeServiceWithProvidedSOAPRequest() throws SoapComponentException {
        try {
            final String filename = new StringBuilder(serviceName).append(NAME_DELIMITER).append(operationName).append(NAME_DELIMITER).toString();
            final File requestFile = new File(workingDir, filename + REQUEST_FILE_SUFFIX);
            final File responseFile = new File(workingDir, filename + RESPONSE_FILE_SUFFIX);
            
            String delimiter = "";
            if (!serviceURI.startsWith("/")) {
                delimiter = "/";
            }
            final ManagedServer managedServer = osbCluster.getManagedServer().get(0);           
            final URL url = new URL(DEFAULT_PROTO, managedServer.getHostName(), managedServer.getPort(), delimiter+serviceURI);
            final String requestEnvelope = FileUtils.readFileToString(requestFile);
            JAXWSDispatch jaxwsDispatch = new JAXWSDispatch();
            ProgressMonitor.init(3, "Connecting to service..."); //1 progress event in jaxwsDispatch.invoke()
            final SOAPMessage res = jaxwsDispatch.invoke(url, requestEnvelope);
            
            String msg = "Successfuly received response of operation: " + operationName;
            logger.debug(msg);
            cor.addMsg(msg);
   
            ProgressMonitor.increment("Saving response to disk...");
            if (responseFile.exists()) {
                FileUtils.forceDelete(responseFile);
            }
            try (FileOutputStream fos = new FileOutputStream(responseFile)) {
                res.writeTo(fos);   
            }
            msg = "Response of operation: " + operationName + " was stored in [FILE: %s]";
            logger.debug(String.format(msg, responseFile.getAbsolutePath()));
            cor.addMsg(msg,"<a href='file://" + responseFile.getAbsolutePath()+"'>"+responseFile.getAbsolutePath()+"</a>", FileSystem.getRelativePath(responseFile));
            cor.markSuccessful();
        } catch (IOException | SOAPException ex) {
            throw new SoapComponentException(ex);
        }
    }

    @Override
    protected void destructComponent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void validateSOAPMessage(boolean response) throws SoapComponentException {
        String filename = new StringBuilder(serviceName).append(NAME_DELIMITER).append(operationName).append(NAME_DELIMITER).toString();
        String result;
        if (response) {
            filename += RESPONSE_FILE_SUFFIX;
            result = "Response message of operation '" + operationName;
        } else {
            filename += REQUEST_FILE_SUFFIX;
            result = "Request envelope for operation '" + operationName;
        }        
        try {
            final File file = new File(workingDir, filename);
            final String xmlMessage = FileUtils.readFileToString(file);
            
            String delimiter = "";
            if (!serviceURI.startsWith("/")) {
                delimiter = "/";
            }
            final ManagedServer managedServer = osbCluster.getManagedServer().get(0);
            final URL url = new URL(DEFAULT_PROTO, managedServer.getHostName(), managedServer.getPort(), delimiter+serviceURI+WSDL_SUFFIX);
            ProgressMonitor.init(6, "Reading WSDL from the server..."); //there are 2 ProgressMonitor events inside SoapLegacyFacade
            final SoapLegacyFacade facade = new SoapLegacyFacade(url);
            final Binding binding = findBindingForOperationName(facade, operationName);
            final BindingOperation operation = binding.getBindingOperation(operationName, null, null);
            
            ProgressMonitor.increment("Validating " + (response ? "response" : "request") + "...");
            if (response) {
                facade.validateSoapResponseMessage(binding, operation, xmlMessage, false);
            } else {
                facade.validateSoapRequestMessage(binding, operation, xmlMessage, false);
            }
            
            result += "' is valid.";
            logger.debug(result);
            cor.addMsg(result);
            cor.markSuccessful();
        } catch (SoapValidationException e) {
            result += "' is invalid.";
            logger.debug(result);
            cor.addMsg(result);
            cor.addMsg("Validator result: "+e.getMessage());
            throw new SoapComponentException(result, e);
        } catch (IOException | WSDLException ex) {
            cor.addMsg(ex.getMessage());
            throw new SoapComponentException(ex);
        }       
    }

    private void checkSOAPMessage(boolean ok) throws SoapComponentException {
        ProgressMonitor.init(2, "Loading message from file...");
        String filename = new StringBuilder(serviceName).append(NAME_DELIMITER).append(operationName).append(NAME_DELIMITER).append(RESPONSE_FILE_SUFFIX).toString();
        final File file = new File(workingDir, filename);
        InputStream is = null;
        try {
            final byte[] xmlMessage = FileUtils.readFileToByteArray(file);
            is = new ByteArrayInputStream(xmlMessage);            
            SOAPMessage response = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL).createMessage(new MimeHeaders(), is);
            ProgressMonitor.increment("Checking for fault...");
            response.removeAllAttachments();
            SOAPEnvelope envp = response.getSOAPPart().getEnvelope();
            SOAPBody someBody = envp.getBody();
            if (ok) {
                if (someBody.getFault() == null) {
                    cor.addMsg("soap body is OK");
                    cor.markSuccessful();
                } else {
                    final String msg = "found soap fault in response body:\n" + new String(xmlMessage);
                    cor.addMsg(msg);
                    throw new SoapComponentException(msg);
                }
            } else {
                if (someBody.getFault() != null) {
                    cor.addMsg("found soap fault in response body");
                    cor.markSuccessful();
                } else {
                    final String msg = "response body doesn't contain soap fault:\n" + new String(xmlMessage);
                    cor.addMsg(msg);
                    throw new SoapComponentException(msg);
                }
            }
        } catch (IOException | SOAPException ex) {
            throw new SoapComponentException("error while trying to parse response", ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    logger.debug("Not able to close input stream. ", ex);
                }
            }
        }
    }
    
    public static String transformXPath(String xPath) {
        if (xPath == null) {
            return null;
        }
        String path;
        if (xPath.startsWith("$this") || xPath.startsWith("$body")) {
            path = xPath.substring(5);
        } else {
            path = xPath;
        }
<<<<<<< HEAD
        path = path.replaceAll("\\*:([^/\\[\\]]+)([^/])?", "*[local-name()=\"$1\"]$2");
=======
        path = path.replaceAll("\\*:([^/]+)", "*[local-name()=\"$1\"]");
>>>>>>> 7c2802d5d20e30d5191a0f8f327cacd09e189422
        StringBuilder s = new StringBuilder("$this");
        if (path.startsWith("/")) {
            s.append(path);
        } else {
            s.append("//").append(path);
        }
        return s.toString();
    }    
}

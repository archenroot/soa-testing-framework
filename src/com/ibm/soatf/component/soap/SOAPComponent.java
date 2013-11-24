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

import com.ibm.soatf.component.osb.ServiceManager;
import com.ibm.soatf.component.ComponentResult;
import com.ibm.soatf.flow.FlowPatternCompositeKey;
import com.ibm.soatf.component.SOATFComponent;
import com.ibm.soatf.component.SOATFCompType;
import com.ibm.soatf.config.iface.soap.SOAPConfig;
import com.ibm.soatf.config.master.Operation;
import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public class SOAPComponent extends SOATFComponent {

    private static final Logger logger = LogManager.getLogger(SOAPComponent.class);
    private OracleFusionMiddlewareInstance masterOFMConfig;
    private SOAPConfig soapIfaceConfig;

    private String identificator;
    private String serviceName;
    private String operationName;
    private String endPointURI;

    private String servicetype; //need to be added to configuration schema
    private boolean status;
    private String serviceURI;
    private String adminHost;
    private int adminPort;
    private String username;
    private String password;
    private String jmxProtocol;

    private OracleFusionMiddlewareInstance.AdminServer adminServer;
    private OracleFusionMiddlewareInstance.Cluster cluster;
    private OracleFusionMiddlewareInstance.Cluster.ManagedServer managedServer;

    private FlowPatternCompositeKey fpck;
    private String serviceLocactionType;
    private String serviceSOAType;

    public SOAPComponent(
            OracleFusionMiddlewareInstance soapMasterConfig,
            SOAPConfig soapIfaceConfig,
            ComponentResult componentOperationResult,
            FlowPatternCompositeKey ifpck) {
        super(SOATFCompType.SOAP, componentOperationResult);
        this.masterOFMConfig = soapMasterConfig;
        this.soapIfaceConfig = soapIfaceConfig;
        this.fpck = ifpck;
        constructComponent();

    }

    @Override
    protected void constructComponent() {
        this.adminServer = this.masterOFMConfig.getAdminServer();
        //this.managedServer = new ManagedServer(this.osbConfiguration.getCluster().getManagedServer());
        this.serviceURI = this.soapIfaceConfig.getEndPointUri();
        this.servicetype = this.soapIfaceConfig.getServiceSOAType();
        this.adminHost = this.masterOFMConfig.getAdminServer().getHost();
        this.adminPort = this.masterOFMConfig.getAdminServer().getPort();
        this.username = this.masterOFMConfig.getAdminServer().getSecurityPrincipal();
        this.password = this.masterOFMConfig.getAdminServer().getSecurityCredentials();

        this.serviceName = soapIfaceConfig.getServiceName();
        this.operationName = soapIfaceConfig.getOperationName();
        this.endPointURI = soapIfaceConfig.getEndPointUri();
        this.serviceLocactionType = soapIfaceConfig.getServiceLocationType();
        this.serviceSOAType = soapIfaceConfig.getServiceSOAType();

    }

    @Override
    protected void executeOperation(Operation operation) {
        /*if (!SOAP_OPERATIONS.contains(operation.getName())) {
         final String msg = "Unsupported operation: " + operation.getName() + ". Valid operations are: " + SOAP_OPERATIONS;
         logger.error(msg);
         compOperResult.setResultMessage(msg);
         compOperResult.setOverallResultSuccess(false);
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
                generateDynamicallySOAPRequest();
                break;
            case SOAP_INVOKE_SERVICE:
                invokeServiceWithProvidedSOAPRequest();
                break;
            default:
                logger.info("Operation execution not yet implemented: " + operation.getName());
                compOperResult.setResultMessage("Operation: " + operation.getName() + " is valid, but not yet implemented");
                compOperResult.setOverallResultSuccess(false);
        }

    }

    private void disableService() {
        boolean result = ServiceManager.changeServiceStatus(
                servicetype, false,
                serviceURI, adminHost, adminPort, username, password);
        this.getComponentOperationResult().setResultMessage("OSB " + servicetype + " " + serviceURI + " running at " + adminHost + " has been disabled.");
        this.getComponentOperationResult().setOverallResultSuccess(result);
        if (result) {
            this.getComponentOperationResult().addMsg("Service succesfully disabled.");
        } else {
            this.getComponentOperationResult().addMsg("Failed to disable the service.");
        }
    }

    private void enableService() {
        boolean result = ServiceManager.changeServiceStatus(
                servicetype, true,
                serviceURI, adminHost, adminPort, username, password);
        this.getComponentOperationResult().setResultMessage("OSB " + servicetype + " " + serviceURI + " running at " + adminHost + " has been enabled.");
        this.getComponentOperationResult().setOverallResultSuccess(result);
        if (result) {
            this.getComponentOperationResult().addMsg("Service succesfully enabled.");
        } else {
            this.getComponentOperationResult().addMsg("Failed to enable the service.");
        }
    }

    private void generateDynamicallySOAPRequest() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void invokeServiceWithProvidedSOAPRequest() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

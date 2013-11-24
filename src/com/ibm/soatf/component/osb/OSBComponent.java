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
package com.ibm.soatf.component.osb;

import com.ibm.soatf.component.CompOperType;
import com.ibm.soatf.component.ComponentResult;
import com.ibm.soatf.flow.FlowPatternCompositeKey;
import com.ibm.soatf.component.SOATFCompType;
import com.ibm.soatf.component.SOATFComponent;
import com.ibm.soatf.UnsupportedComponentOperationException;
import com.ibm.soatf.config.iface.soap.SOAPConfig;
import com.ibm.soatf.config.master.Operation;


import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance;
import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance.AdminServer;
import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance.Cluster;
import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance.Cluster.ManagedServer;

import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public final class OSBComponent extends SOATFComponent {
    
    private static final Logger logger = LogManager.getLogger(OSBComponent.class.getName());
    
    public static Set<CompOperType> supportedOperations = CompOperType.OSB_OPERATIONS;

    private String servicetype; //need to be added to configuration schema
    private boolean status;
    private String serviceURI;
    private String adminHost;
    private int adminPort;
    private String username;
    private String password;
    private String jmxProtocol;

    private AdminServer adminServer;
    private Cluster cluster;
    private ManagedServer managedServer;
    
    private final SOAPConfig soapConfig;
    private final OracleFusionMiddlewareInstance masterOFMConfig;

    //private ComponentResult componentOperationResult = new ComponentResult();
    
    public OSBComponent(
            OracleFusionMiddlewareInstance masterOFMConfig,
            SOAPConfig soapConfig, 
            ComponentResult componentOperationResutlt,
            FlowPatternCompositeKey ifaceFlowPatternCompositeKey) {
        
        super(SOATFCompType.OSB, componentOperationResutlt);
        this.masterOFMConfig = masterOFMConfig;
        this.soapConfig = soapConfig;
        
        constructComponent();
    }

    @Override
    protected void constructComponent() {
        
        
        /*ListIterator<Service> i = this.osbConfiguration.getService().getService().listIterator();
        Service service = null;
        while (i.hasNext()) {
            service = i.next();
            if (service.getIdentificator().equals(this.getIdentificator())) {
                break;
            }
        }
        if (service == null) {
            try {
                throw new com.ibm.soatf.FrameworkConfigurationException();
            } catch (    com.ibm.soatf.FrameworkConfigurationException ex) {
                logger.error("Cannot find OSB service configuration element with specific identificator provided while creating component. Identificator lookup value: " + this.getIdentificator() + ". " + ex.getLocalizedMessage());
                this.componentOperationResult.setOverallResultSuccess(false);
                return;
            }
        }*/
        this.adminServer = this.masterOFMConfig.getAdminServer();
        //this.managedServer = new ManagedServer(this.osbConfiguration.getCluster().getManagedServer());
        this.serviceURI = this.soapConfig.getEndPointUri();
        this.servicetype = this.soapConfig.getServiceSOAType();
        this.adminHost = this.masterOFMConfig.getAdminServer().getHost();
        this.adminPort = this.masterOFMConfig.getAdminServer().getPort();
        this.username = this.masterOFMConfig.getAdminServer().getSecurityPrincipal();
        this.password = this.masterOFMConfig.getAdminServer().getSecurityCredentials();

    }

    @Override
    public void executeOperation(Operation operation) {
        
        this.getComponentOperationResult().setOperation(operation);
        try {
            if (!supportedOperations.contains(operation)) {
                throw new UnsupportedComponentOperationException();
            }            
            switch (operation.getName()) {
                case SOAP_DISABLE_SERVICE:
                    disableService();
                    break;
                case SOAP_ENABLE_SERVICE:
                    enableService();
                    break;
                default:
                    throw new UnsupportedComponentOperationException();
            }
        } catch (UnsupportedComponentOperationException ex) {
            logger.fatal("Component operation is not supported." + ex);
            this.getComponentOperationResult().addMsg("Component operation is not supported.");
            this.getComponentOperationResult().setOverallResultSuccess(false);
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
}

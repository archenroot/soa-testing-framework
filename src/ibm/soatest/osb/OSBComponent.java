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
package ibm.soatest.osb;

import ibm.soatest.CompOperResult;
import ibm.soatest.CompOperType;
import ibm.soatest.MissingXMLConfigurationException;
import ibm.soatest.SOATFCompType;
import ibm.soatest.SOATFComponent;
import ibm.soatest.UnsupportedComponentOperation;
import ibm.soatest.config.AdminServer;
import ibm.soatest.config.Cluster;
import ibm.soatest.config.ManagedServer;
import ibm.soatest.config.OSBConfiguration;
import ibm.soatest.config.Service;
import java.io.IOException;
import java.util.ListIterator;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public class OSBComponent extends SOATFComponent {
    
    private static final Logger logger = LogManager.getLogger(OSBComponent.class.getName());
    
    public static Set<CompOperType> supportedOperations = CompOperType.osbOperations;

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
    
    private OSBConfiguration osbConfiguration;

    //private CompOperResult componentOperationResult = new CompOperResult();
    
    public OSBComponent(OSBConfiguration osbConfiguration, CompOperResult componentOperationResutlt, String identificator) {
        
        super(SOATFCompType.OSB, componentOperationResutlt, identificator);
        this.osbConfiguration = osbConfiguration;
        
        constructComponent();
    }

    @Override
    protected void constructComponent() {
        ListIterator<Service> i = this.osbConfiguration.getServices().getService().listIterator();
        Service service = null;
        while (i.hasNext()) {
            service = i.next();
            if (service.getIdentificator().equals(this.getIdentificator())) {
                break;
            }
        }
        if (service == null) {
            try {
                throw new ibm.soatest.MissingXMLConfigurationException();
            } catch (ibm.soatest.MissingXMLConfigurationException ex) {
                logger.error("Cannot find OSB service configuration element with specific identificator provided while creating component. Identificator lookup value: " + this.getIdentificator() + ". " + ex.getLocalizedMessage());
                this.componentOperationResult.setOverallResultSuccess(false);
                return;
            }
        }
        this.adminServer = this.osbConfiguration.getAdminServer();
        //this.managedServer = new ManagedServer(this.osbConfiguration.getCluster().getManagedServer());
        this.serviceURI = service.getServiceURI();
        this.servicetype = service.getServiceType();
        this.adminHost = this.osbConfiguration.getAdminServer().getHost();
        this.adminPort = this.osbConfiguration.getAdminServer().getPort();
        this.username = this.osbConfiguration.getAdminServer().getSecurityPrincipal();
        this.password = this.osbConfiguration.getAdminServer().getSecurityCredentials();

    }

    @Override
    public void executeOperation(CompOperType componentOperation) {
        
        this.getComponentOperationResult().setCompOperType(componentOperation);
        try {
            if (supportedOperations.contains(componentOperation)) {
                throw new UnsupportedComponentOperation();
            }            
            switch (componentOperation) {
                case OSB_DISABLE_SERVICE:
                    disableService();
                    break;
                case OSB_ENABLE_SERVICE:
                    enableService();
                    break;
                default:
                    throw new UnsupportedComponentOperation();
            }
        } catch (UnsupportedComponentOperation ex) {
            logger.fatal("Component operation is not supported." + ex);
            this.getComponentOperationResult().addMsg("Component operation is not supported.");
            this.getComponentOperationResult().setOverallResultSuccess(false);
        }        

    }

    private void disableService() {
        boolean result = ServiceManager.changeServiceStatus(          
                servicetype, false, 
                serviceURI, adminHost, adminPort, username, password);
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
        this.getComponentOperationResult().setOverallResultSuccess(result);
        if (result) {
            this.getComponentOperationResult().addMsg("Service succesfully enabled.");
        } else {
            this.getComponentOperationResult().addMsg("Failed to enable the service.");
        }        
    }
}

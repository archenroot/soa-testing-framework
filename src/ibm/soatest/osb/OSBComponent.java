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
import ibm.soatest.SOATFCompType;
import ibm.soatest.SOATFComponent;
import ibm.soatest.config.osbconfiguration.OsbConfiguration;
import ibm.soatest.database.UnsupportedComponentOperation;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author zANGETSu
 */
public class OSBComponent extends SOATFComponent {

    private final String servicetype = "ProxyService"; //need to be added to configuration schema
    private boolean status;
    private String serviceURI;
    private String host;
    private BigInteger port;
    private String username;
    private String password;
    private String jmxProtocol;

    private AdminServer adminServer;;
    private Cluster cluster;
    private ManagedServer managedServer;;
    
    private OsbConfiguration osbConfiguration;

    private CompOperResult componentOperationResult = new CompOperResult();
    
    public OSBComponent(OsbConfiguration osbConfiguration) {
        super(SOATFCompType.OSB);
        this.osbConfiguration = osbConfiguration;
        constructComponent();
    }

    @Override
    protected void constructComponent() {
        this.adminServer = new AdminServer(this.osbConfiguration.getAdminServer());
        //this.managedServer = new ManagedServer(this.osbConfiguration.getCluster().getManagedServer());
        this.serviceURI = "HudsonDemo/proxy/JMSListener";
        this.host = this.osbConfiguration.getAdminServer().getHost();
        this.username = this.osbConfiguration.getAdminServer().getSecurityPrincipal();
        this.password = this.osbConfiguration.getAdminServer().getSecurityCredentials();

    }

    @Override
    public CompOperResult executeOperation(CompOperType componentOperation) {
        //changeProxyServiceStatus(servicetype, status, serviceURI, host, port, username, password)
        //changeProxyServiceStatus("ProxyService", false, "HudsonDemo/proxy/JMSListener", "prometheus", 7001, "weblogic", "Weblogic123"));
        Set<CompOperType> supportedOperations 
                = CompOperType.osbOperations;

        switch (componentOperation) {
            case OSB_DISABLE_PROXY_SERVICE:
        try {
            disableProxyService();
        } catch (IOException ex) {
            Logger.getLogger(OSBComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
                break;
            case OSB_ENABLE_PROXY_SERVICE:
        try {
            enableProxyService();
        } catch (IOException ex) {
            Logger.getLogger(OSBComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
                break;
            default:
                try {
                    throw new UnsupportedComponentOperation();
                } catch (UnsupportedComponentOperation ex) {
                    Logger.getLogger(OSBComponent.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        this.componentOperationResult.setOverallResult(true);
        this.componentOperationResult.setResultMessage("Everythings OK.");
        return componentOperationResult;

    }

    private void disableProxyService() throws IOException {
        ServiceManager.changeProxyServiceStatus(
                
                servicetype, false, 
                serviceURI, host, 7001, username, password);
    }

    private void enableProxyService() throws IOException {
        ServiceManager.changeProxyServiceStatus(
                
                servicetype, true, 
                serviceURI, host, 7001, username, password);
    }
}

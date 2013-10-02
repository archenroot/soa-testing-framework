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
package com.ibm.fm.soatest.osb;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.ibm.fm.soatest.SoaTestingFrameworkComponent;
import com.ibm.fm.soatest.SoaTestingFrameworkComponentType;
import com.ibm.fm.soatest.configuration.OsbConfiguration;
import com.ibm.fm.soatest.database.DatabaseComponent;
import com.ibm.fm.soatest.database.UnsupportedComponentOperation;

/**
 *
 * @author zANGETSu
 */
public class OsbComponent extends SoaTestingFrameworkComponent {

    private final String servicetype = "ProxyService"; //need to be added to configuration schema
    private boolean status = false;
    private String serviceURI = null;
    private String host = null;
    private BigInteger port = null;
    private String username = null;
    private String password = null;
    private String jmxProtocol = null;

    private AdminServer adminServer = null;
    private Cluster cluster = null;
    private ManagedServer managedServer = null;
    
    private OsbConfiguration osbConfiguration = null;

    public OsbComponent(OsbConfiguration osbConfiguration) {
        super(SoaTestingFrameworkComponentType.OSB);
        this.osbConfiguration = osbConfiguration;
        constructComponent();
    }

    @Override
    protected void constructComponent() {
        this.adminServer = new AdminServer(this.osbConfiguration.getAdminServer());
        //this.managedServer = new ManagedServer(this.osbConfiguration.getCluster().getManagedServer());
        this.serviceURI = "HudsonDemo/proxy/JMSListener";
        //this.host = this.osbConfiguration.getAdminServer().getHostName();
        //this.username = this.osbConfiguration.getAdminServer().getUserName();
        //this.password = this.osbConfiguration.getAdminServer().getPassword();

    }

    @Override
    public void executeOperation(SoaTestingFrameworkComponentType.ComponentOperation componentOperation) {
        //changeProxyServiceStatus(servicetype, status, serviceURI, host, port, username, password)
        //changeProxyServiceStatus("ProxyService", false, "HudsonDemo/proxy/JMSListener", "prometheus", 7001, "weblogic", "Weblogic123"));
        Set<SoaTestingFrameworkComponentType.ComponentOperation> supportedOperations 
                = SoaTestingFrameworkComponentType.ComponentOperation.osbOperations;

        switch (componentOperation) {
            case OSB_DISABLE_PROXY_SERVICE:
        try {
            disableProxyService();
        } catch (IOException ex) {
            Logger.getLogger(OsbComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
                break;
            case OSB_ENABLE_PROXY_SERVICE:
        try {
            enableProxyService();
        } catch (IOException ex) {
            Logger.getLogger(OsbComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
                break;
            default:
                try {
                    throw new UnsupportedComponentOperation();
                } catch (UnsupportedComponentOperation ex) {
                    Logger.getLogger(OsbComponent.class.getName()).log(Level.SEVERE, null, ex);
                }
        }

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

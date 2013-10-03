/*
 * Copyright (C) 2013 Ladislav Jech <archenroot@gmail.com>
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean;

/**
 *
 * @author Ladislav Jech <archenroot@gmail.com>
 */
public class AdminServer {
    
    private String managementMBeanServer = null;
    private String managementMBeanService = null;
    private String hostName = null;
    private String userName = null;
    private String password = null;
    private JMXConnector jmxConnector = null;
              
    
    public AdminServer(ibm.soatest.config.osbconfiguration.AdminServer adminServer){
        this.managementMBeanServer = adminServer.getManagementMBeanServer();
        this.managementMBeanService = adminServer.getManagementMBeanService();
        //this.hostName = adminServer.getHostName();
        //this.userName = adminServer.getUserName();
        //this.password = adminServer.getPassword();
        //this.jmxConnector = this.initConnection();
        
    }
    
    
    
    public String getManagementMBeanServer() {
        return managementMBeanServer;
    }

    public String getManagementMBeanService() {
        return managementMBeanService;
    }

    public String getHostName() {
        return hostName;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public JMXConnector getJmxConnector() {
        return jmxConnector;
    }
    
    
    public void setManagementMBeanServer(String managementMBeanServer) {
        this.managementMBeanServer = managementMBeanServer;
    }

    public void setManagementMBeanService(String managementMBeanService) {
        this.managementMBeanService = managementMBeanService;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    
    
}

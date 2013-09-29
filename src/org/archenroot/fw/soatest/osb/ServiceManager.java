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

package org.archenroot.fw.soatest.osb;

import com.bea.wli.config.Ref;
import com.bea.wli.sb.management.configuration.ALSBConfigurationMBean;
import com.bea.wli.sb.management.configuration.BusinessServiceConfigurationMBean;
import com.bea.wli.sb.management.configuration.ProxyServiceConfigurationMBean;
import com.bea.wli.sb.management.configuration.SessionManagementMBean;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.remote.JMXConnector;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import weblogic.management.jmx.MBeanServerInvocationHandler;
import weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean;

/**
 *
 * @author Ladislav Jech <archenroot@gmail.com>
 */
public class ServiceManager {
    
    
    
    private static Ref constructRef(String refType, String serviceuri) {
        Ref ref = null;
        String[] uriData = serviceuri.split("/");
        ref = new Ref(refType, uriData);
        return ref;
    }
    
    
    public static JMXConnector initConnection(String hostName, String userName, String password) throws MalformedURLException, IOException  {

            JMXServiceURL serviceUrl = new JMXServiceURL("t3", hostName, 7001, "/jndi/" + DomainRuntimeServiceMBean.MBEANSERVER_JNDI_NAME);
            HashMap<String, String> h = new HashMap<String, String>();
            h.put(Context.SECURITY_PRINCIPAL, userName);
            h.put(Context.SECURITY_CREDENTIALS, password);
            h.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, "weblogic.management.remote");
            return JMXConnectorFactory.connect(serviceUrl, h);
        
    }
     public static String changeProxyServiceStatus(String servicetype, boolean status, String serviceURI, String host, int port, String username, String password) throws IOException {
        
        SessionManagementMBean sm = null;
        String sessionName = "mysession";
        String statusmsg = "";
        JMXConnector conn = initConnection(host, username, password);
        try {
        
            MBeanServerConnection mbconn = conn.getMBeanServerConnection();
            DomainRuntimeServiceMBean clusterService = (DomainRuntimeServiceMBean) MBeanServerInvocationHandler.
                    newProxyInstance(mbconn, new ObjectName(DomainRuntimeServiceMBean.OBJECT_NAME));
            sm = (SessionManagementMBean) clusterService.findService(SessionManagementMBean.NAME, SessionManagementMBean.TYPE, null);
            sm.createSession(sessionName);
            ALSBConfigurationMBean alsbSession = (ALSBConfigurationMBean) clusterService.
                    findService(ALSBConfigurationMBean.NAME + "." + "mysession", ALSBConfigurationMBean.TYPE, null);

            if (servicetype.equals("ProxyService")) {
                Ref ref = constructRef("ProxyService", serviceURI);
                ProxyServiceConfigurationMBean proxyConfigMBean = (ProxyServiceConfigurationMBean) clusterService.
                        findService(ProxyServiceConfigurationMBean.NAME + "." + sessionName, ProxyServiceConfigurationMBean.TYPE, null);
                if (status) {
                    proxyConfigMBean.enableService(ref);
                    statusmsg = "Enabled the Service : " + serviceURI;
                } else {
                    proxyConfigMBean.disableService(ref);
                    statusmsg = "Disabled the Service : " + serviceURI;
                }
            } else if (servicetype.equals("BusinessService")) {
                Ref ref = constructRef("BusinessService", serviceURI);
                BusinessServiceConfigurationMBean businessConfigMBean = (BusinessServiceConfigurationMBean) clusterService.
                        findService(BusinessServiceConfigurationMBean.NAME + "." + sessionName, BusinessServiceConfigurationMBean.TYPE, null);
                if (status) {
                    businessConfigMBean.enableService(ref);
                    statusmsg = "Enabled the Service : " + serviceURI;
                } else {
                    businessConfigMBean.disableService(ref);
                    statusmsg = "Disabled the Service : " + serviceURI;
                }
            }
            sm.activateSession(sessionName, statusmsg);
            conn.close();
        } catch (Exception ex) {
            if (null != sm) {
                try {
                    sm.discardSession(sessionName);
                } catch (Exception e) {
                    System.out.println("able to discard the session");

                }
            }
            statusmsg = "Not able to perform the operation";
            ex.printStackTrace();
        } finally {
            if (null != conn) {
                try {
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return statusmsg;
    }
}

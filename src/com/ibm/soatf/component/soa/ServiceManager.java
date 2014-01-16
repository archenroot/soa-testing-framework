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
package com.ibm.soatf.component.soa;

import com.bea.wli.config.Ref;
import com.bea.wli.sb.management.configuration.ALSBConfigurationMBean;
import com.bea.wli.sb.management.configuration.BusinessServiceConfigurationMBean;
import com.bea.wli.sb.management.configuration.ProxyServiceConfigurationMBean;
import com.bea.wli.sb.management.configuration.SessionManagementMBean;
import com.ibm.soatf.flow.FrameworkExecutionException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import javax.management.remote.JMXConnector;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import weblogic.management.jmx.MBeanServerInvocationHandler;
import weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean;

/**
 * Class provides management of the services and SOA composite application running
 * within fusion middle-ware product stack.
 * 
 * @author Ladislav Jech <archenroot@gmail.com>
 */
public class ServiceManager {
    
    private static final Logger logger = LogManager.getLogger(ServiceManager.class.getName());

    public static final String DEFAULT_PROTO = "t3";
    public static final String DEFAULT_PROTO_PROVIDER_PACKAGES = "weblogic.management.remote";
    public static final String URI_SEPARATOR = "/";
    public static final String JNDI_PREFIX = "/jndi/";
    
    // ! TODO - Generate unique session name
    public static final String SESSION_NAME = "mysession";

    private static Ref constructRef(String refType, String serviceuri) {
        Ref ref;
        String[] uriData = serviceuri.split(URI_SEPARATOR);
        ref = new Ref(refType, uriData);
        return ref;
    }

    public static JMXConnector initConnection(
            String hostName, 
            int port, 
            String userName, 
            String password) throws MalformedURLException, IOException {

        JMXServiceURL serviceUrl = new JMXServiceURL(DEFAULT_PROTO, hostName, port, JNDI_PREFIX + DomainRuntimeServiceMBean.MBEANSERVER_JNDI_NAME);
        HashMap<String, String> h = new HashMap<String, String>();
        h.put(Context.SECURITY_PRINCIPAL, userName);
        h.put(Context.SECURITY_CREDENTIALS, password);
        h.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, DEFAULT_PROTO_PROVIDER_PACKAGES);
        return JMXConnectorFactory.connect(serviceUrl, h);

    }

    public static boolean changeServiceStatus(
            String servicetype, 
            boolean status, 
            String serviceURI, 
            String host, 
            int port, 
            String username, 
            String password) throws FrameworkExecutionException {

        SessionManagementMBean sm = null;
        JMXConnector conn = null;
        boolean result = true;
        String statusMsg = "";
        try {
            conn = initConnection(host, port, username, password);
            MBeanServerConnection mbconn = conn.getMBeanServerConnection();
            DomainRuntimeServiceMBean clusterService = (DomainRuntimeServiceMBean) MBeanServerInvocationHandler.newProxyInstance(mbconn, new ObjectName(DomainRuntimeServiceMBean.OBJECT_NAME));
            sm = (SessionManagementMBean) clusterService.findService(SessionManagementMBean.NAME, SessionManagementMBean.TYPE, null);
            sm.createSession(SESSION_NAME);
            ALSBConfigurationMBean alsbSession = (ALSBConfigurationMBean) clusterService.findService(ALSBConfigurationMBean.NAME + "." + SESSION_NAME, ALSBConfigurationMBean.TYPE, null);

            if (servicetype.equals("ProxyService")) {
                Ref ref = constructRef("ProxyService", serviceURI);
                ProxyServiceConfigurationMBean proxyConfigMBean = (ProxyServiceConfigurationMBean) clusterService.findService(ProxyServiceConfigurationMBean.NAME + "." + SESSION_NAME, ProxyServiceConfigurationMBean.TYPE, null);
                if (status) {
                    proxyConfigMBean.enableService(ref);
                    statusMsg="Enable the ProxyService : " + serviceURI;
                    logger.info(statusMsg);
                } else {
                    proxyConfigMBean.disableService(ref);
                    statusMsg="Disable the ProxyService : " + serviceURI;
                    logger.info(statusMsg);
                }
            } else if (servicetype.equals("BusinessService")) {
                try{
                    Ref ref = constructRef("BusinessService", serviceURI);
                    BusinessServiceConfigurationMBean businessConfigMBean = (BusinessServiceConfigurationMBean) clusterService.
                            findService(BusinessServiceConfigurationMBean.NAME + "." + SESSION_NAME, BusinessServiceConfigurationMBean.TYPE, null);
                    if (status) {
                        businessConfigMBean.enableService(ref);
                        statusMsg="Enable the BusinessService : " + serviceURI;
                        logger.info(statusMsg);
                    } else {
                        businessConfigMBean.disableService(ref);
                        statusMsg="Disable the BusinessService : " + serviceURI;
                        logger.info(statusMsg);
                    }
                } catch (IllegalArgumentException ex){
                    logger.fatal(ExceptionUtils.getStackTrace(ex));
                }
            }
            sm.activateSession(SESSION_NAME, statusMsg);
            conn.close();
        } catch (Exception ex) {
            if (null != sm) {
                try {
                    sm.discardSession(SESSION_NAME);
                } catch (Exception e) {
                    logger.debug("Not able to discard the session. "+e.getLocalizedMessage());
                    throw new FrameworkExecutionException(e);
                }
            }
            result = false;
            logger.error("Error in MBean Server connection. "+ex.getLocalizedMessage());
            ex.printStackTrace();
        } finally {
            if (null != conn) {
                try {
                    conn.close();
                } catch (Exception e) {
                    logger.debug("Not able to close the JMX connection. "+e.getLocalizedMessage());
                    throw new FrameworkExecutionException(e);
                            
                    
                }
            }
        }

        return result;
    }
}

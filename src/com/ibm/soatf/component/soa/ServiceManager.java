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
import com.bea.wli.sb.management.configuration.BusinessServiceConfigurationMBean;
import com.bea.wli.sb.management.configuration.ProxyServiceConfigurationMBean;
import com.bea.wli.sb.management.configuration.SessionManagementMBean;
import com.ibm.soatf.component.soap.SoapComponentException;
import com.ibm.soatf.flow.OperationResult;
import com.ibm.soatf.gui.ProgressMonitor;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import oracle.soa.management.util.CompositeManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import weblogic.management.jmx.MBeanServerInvocationHandler;
import weblogic.management.mbeanservers.Service;
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
        HashMap<String, String> h = new HashMap<>();
        h.put(Context.SECURITY_PRINCIPAL, userName);
        h.put(Context.SECURITY_CREDENTIALS, password);
        h.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, DEFAULT_PROTO_PROVIDER_PACKAGES);
        return JMXConnectorFactory.connect(serviceUrl, h);

    }
    
    public static boolean changeOsbServiceStatus(
            String servicetype, 
            boolean status, 
            String serviceURI, 
            String host, 
            int port, 
            String username, 
            String password) throws SoapComponentException {
        final OperationResult cor = OperationResult.getInstance();
        SessionManagementMBean sm = null;
        JMXConnector conn = null;
        String statusMsg = "";
        try {
            ProgressMonitor.init(3, "Connecting to server...");
            conn = initConnection(host, port, username, password);
            MBeanServerConnection mbconn = conn.getMBeanServerConnection();
            DomainRuntimeServiceMBean clusterService = (DomainRuntimeServiceMBean) MBeanServerInvocationHandler.newProxyInstance(mbconn, new ObjectName(DomainRuntimeServiceMBean.OBJECT_NAME));
            sm = (SessionManagementMBean) clusterService.findService(SessionManagementMBean.NAME, SessionManagementMBean.TYPE, null);
            sm.createSession(SESSION_NAME);
            //ALSBConfigurationMBean alsbSession = (ALSBConfigurationMBean) clusterService.findService(ALSBConfigurationMBean.NAME + "." + SESSION_NAME, ALSBConfigurationMBean.TYPE, null);
            
            /*
            //this can be used to retrieve polling interval for util waits ... or maybe force the polling
            Service findService = clusterService.findService(, , );
            findService.getParentAttribute()
            */
                    
            if (servicetype.equals("ProxyService")) {
                Ref ref = constructRef("ProxyService", serviceURI);
                ProxyServiceConfigurationMBean proxyConfigMBean = (ProxyServiceConfigurationMBean) clusterService.findService(ProxyServiceConfigurationMBean.NAME + "." + SESSION_NAME, ProxyServiceConfigurationMBean.TYPE, null);
                if (status) {
                    ProgressMonitor.increment("Enabling proxy service...");
                    proxyConfigMBean.enableService(ref);

                    statusMsg="Enable the ProxyService : " + serviceURI;
                    logger.info(statusMsg);
                } else {
                    ProgressMonitor.increment("Disabling proxy service...");
                    proxyConfigMBean.disableService(ref);
                    statusMsg="Disable the ProxyService : " + serviceURI;
                    logger.info(statusMsg);
                }
            } else if (servicetype.equals("BusinessService")) {
                Ref ref = constructRef("BusinessService", serviceURI);
                BusinessServiceConfigurationMBean businessConfigMBean = (BusinessServiceConfigurationMBean) clusterService.
                        findService(BusinessServiceConfigurationMBean.NAME + "." + SESSION_NAME, BusinessServiceConfigurationMBean.TYPE, null);
                if (status) {
                    ProgressMonitor.increment("Enabling business service...");
                    businessConfigMBean.enableService(ref);
                    statusMsg="Enable the BusinessService : " + serviceURI;
                    logger.info(statusMsg);
                } else {
                    ProgressMonitor.increment("Disabling business service...");
                    businessConfigMBean.disableService(ref);
                    statusMsg="Disable the BusinessService : " + serviceURI;
                    logger.info(statusMsg);
                }
            } else {
                //wtf?
            }
            ProgressMonitor.increment("Activating session...");
            sm.activateSession(SESSION_NAME, statusMsg);
            return true;
        } catch (Exception ex) {
            if (null != sm) {
                try {
                    sm.discardSession(SESSION_NAME);
                } catch (Exception e) {
                    logger.debug("Not able to discard the session. "+e.getLocalizedMessage());
                }
            }
            final String msg = "Error in MBeanServerConnection. "+ex.getLocalizedMessage();
            cor.addMsg(msg);
            throw new SoapComponentException(msg, ex);
        } finally {
            if (null != conn) {
                try {
                    conn.close();
                } catch (IOException e) {
                    logger.debug("Not able to close the JMX connection. "+e.getLocalizedMessage());
                }
            }
        }
    }

    public static boolean changeSoaCompositeApplicationStatus(boolean status, String serviceUri, String hostName, int port, String username, String password) throws SoapComponentException {
        final OperationResult cor = OperationResult.getInstance();
        boolean result = false;
        try {
            ProgressMonitor.init(3, "Connecting to server...");
            CompositeManager.initConnection(hostName, Integer.toString(port), username, password);  
 //           oracle.soa.management.facade.Locator locator = LocatorFactory.createLocator();
            ProgressMonitor.increment("Assigning default composite...");
            CompositeManager.assignDefaultComposite(CompositeManager.getCompositeLifeCycleMBean(), serviceUri);  

            if (status) {
                ProgressMonitor.increment("Enabling service...");
                CompositeManager.startComposite(CompositeManager.getCompositeLifeCycleMBean(), serviceUri);           
            } else {
                ProgressMonitor.increment("Disabling service...");
                CompositeManager.stopComposite(CompositeManager.getCompositeLifeCycleMBean(), serviceUri);           
            }
            result = true;
        } catch (IOException | MalformedObjectNameException | InstanceNotFoundException | MBeanException | ReflectionException ex) {
            final String msg = "Error in CompositeManager. "+ex.getMessage();
            //logger.error(msg);
            cor.addMsg(msg);
            throw new SoapComponentException(msg, ex);
        } finally {
            try {
                CompositeManager.closeConnection();
            } catch (IOException e) {
                logger.debug("Not able to close the CompositeManager connection. "+e.getLocalizedMessage());
            }
        }

        return result;
    }
}

package com.ibm.soatf.component.jms;

import com.ibm.soatf.flow.OperationResult;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import javax.management.AttributeNotFoundException;
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
import javax.naming.InitialContext;

import weblogic.j2ee.descriptor.wl.JMSBean;
import weblogic.j2ee.descriptor.wl.JMSConnectionFactoryBean;
import weblogic.j2ee.descriptor.wl.QueueBean;
import weblogic.jms.extensions.JMSModuleHelper;
import weblogic.management.configuration.JMSSystemResourceMBean;

public class JMSResource {

    private final static String SERVICE_NAME = "com.bea:Name=RuntimeService,";
    private final static String SERVICE_MBEAN = "Type=weblogic.management.mbeanservers.runtime.RuntimeServiceMBean";
    private final static String JNDI_ROOT = "/jndi/";
    private final static String MBEAN_SERVER = "weblogic.management.mbeanservers.runtime";

    private final OperationResult cor = OperationResult.getInstance();

    private static MBeanServerConnection connection;
    private static JMXConnector connector;
    private static ObjectName service;

    static {

        try {

            System.out.println("…");
            service = new ObjectName(
                    SERVICE_NAME
                    + SERVICE_MBEAN);
        } catch (MalformedObjectNameException e) {
            throw new AssertionError(e.getMessage());
        }
    }

    public void initConnection(
            String hostname,
            String portString,
            String username,
            String password) throws JmsComponentException {
        try {
            String protocol = "t3";
            Integer portInteger = Integer.valueOf(portString);
            int port = portInteger.intValue();
            String jndiroot = JNDI_ROOT;
            String mserver = MBEAN_SERVER;
            JMXServiceURL serviceURL = new JMXServiceURL(protocol, hostname, port,
                    jndiroot + mserver);
            Hashtable h = new Hashtable();
            h.put(Context.SECURITY_PRINCIPAL, username);
            h.put(Context.SECURITY_CREDENTIALS, password);
            h.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES,
                    "weblogic.management.remote");
            connector = JMXConnectorFactory.connect(serviceURL, h);
            connection = connector.getMBeanServerConnection();
        } catch (IOException ex) {
            final String msg = "TODO";
            cor.addMsg(msg);
            throw new JmsComponentException(msg, ex);
        }
    }

    public ObjectName[] getJMSServers() throws JmsComponentException {
        try {
            ObjectName serverRuntime = (ObjectName) connection.getAttribute(
                    service, "ServerRuntime");
            ObjectName jmsRuntime = (ObjectName) connection.getAttribute(
                    serverRuntime, "JMSRuntime");
            ObjectName[] jmsServers = (ObjectName[]) connection.getAttribute(
                    jmsRuntime, "JMSServers");
            return jmsServers;
        } catch (MBeanException | AttributeNotFoundException | InstanceNotFoundException | ReflectionException ex) {
            final String msg = "TODO";
            cor.addMsg(msg);
            throw new JmsComponentException(msg, ex);
        } catch (IOException ex) {
            final String msg = "TODO";
            cor.addMsg(msg);
            throw new JmsComponentException(msg, ex);
        }
    }

    @Deprecated
    public void testMethod() {

        try {

            Hashtable env = new Hashtable();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
            env.put(Context.PROVIDER_URL, "t3://iwpdcdevsoaa.iwater.ie:7001");
            env.put(Context.SECURITY_PRINCIPAL, "weblogic");
            env.put(Context.SECURITY_CREDENTIALS, "passw0rd1");
            Context ctx = new InitialContext(env);

            ObjectName[] serverRT = getJMSServers();
            int length = (int) serverRT.length;
            System.out.println("length:::" + length);

            for (int i = 0; i < length; i++) {

                System.out.println("i:::" + i);

                ObjectName[] queues = (ObjectName[]) connection.getAttribute(
                        serverRT[i], "Destinations");

                String jmsServerName = (String) connection.getAttribute(
                        serverRT[i], "Name");
                System.out.println("JMS Server name: " + jmsServerName);

                int queueCount = (int) queues.length;
                if (!jmsServerName.equals("wlsbJMSServer")) {
                    for (int k = 0; k < queueCount; k++) {

                        String queueNameWithModule = (String) connection
                                .getAttribute(queues[k], "Name");

                        Long messagesCurrentCount = (Long) connection.getAttribute(
                                queues[k], "MessagesCurrentCount");
                        System.out.println("messagesCurrentCount: "
                                + messagesCurrentCount);

                        String moduleName = queueNameWithModule.substring(0,
                                queueNameWithModule.indexOf("!"));
                        System.out.println("JMS module Name: " + moduleName);
                        String resourceName = moduleName;
                        String queueName = queueNameWithModule
                                .substring(queueNameWithModule.indexOf("!") + 1);
                        System.out.println("queueName: " + queueName);

                        JMSSystemResourceMBean jmsSR = JMSModuleHelper
                                .findJMSSystemResource(ctx, resourceName);
                        JMSBean jmsBean = jmsSR.getJMSResource();
                        JMSConnectionFactoryBean[] connectionFactoryBean = jmsBean
                                .getConnectionFactories();

                        int length1 = (int) connectionFactoryBean.length;

                        for (int j = 0; j < length1; j++) {

                            JMSConnectionFactoryBean jmsFactory = connectionFactoryBean[j];
                            System.out.println("jmsFactory.getJNDIName()"
                                    + jmsFactory.getJNDIName());

                        }
                        QueueBean[] queueBean = jmsBean.getQueues();
                        length = (int) queueBean.length;

                        for (int m = 0; m < length; m++) {

                            QueueBean queue = queueBean[m];

                            if (queueName != null
                                    && queueName.equals(queue.getName())) {
                                System.out.println("queue.getJNDIName()"
                                        + queue.getJNDIName());
                            }
                        }

                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Deprecated
    public static void main(String[] args) throws Exception {
        String hostname = "iwpdcdevsoaa.iwater.ie";
        String portString = "8011";
        String username = "weblogic";
        String password = "passw0rd1";
        System.out.println("initializing conenction….");
        JMSResource jmsRes = new JMSResource();
        jmsRes.initConnection(hostname, portString, username, password);
        jmsRes.testMethod();
        connector.close();
    }
}

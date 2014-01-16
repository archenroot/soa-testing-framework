package com.ibm.soatf.component.jms;

import com.ibm.soatf.flow.OperationResult;
import java.io.IOException;
import java.util.HashMap;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;

import weblogic.management.remote.t3.ClientProvider;

/**
 *
 * @author zANGETSu
 */
public class JMXConnector {

    private final OperationResult cor = OperationResult.getInstance();
    
    private JMXServiceURL serviceURL;
    private javax.management.remote.JMXConnector connector;
    private MBeanServerConnection connection;

    private String hostName;
    private int port;
    private String userName;
    private String password;
    private final String jndiroot = "/jndi/";
    
    /*
    There exists 3 MBean servers accessible trough JMX, well even one can
    connect directly to the managed server, Oracle suppose to connect to the 
    admin server trough the "Domain Runtime MBean Server" server bean and
    manage other nodes from this point. Here is list of supported MBean servers:
    MBean Server                 JNDI Name
    Domain Runtime MBean Server  weblogic.management.mbeanservers.domainruntime
    Runtime MBean Server         weblogic.management.mbeanservers.runtime
    Edit MBean Server            weblogic.management.mbeanservers.edit 
    */
    private String messageBeanServer;
    
    // Basic constructor is supported by setter voids
    JMXConnector() {
    }
    
    // Extended constructor prefer to use
    JMXConnector(String hostname
            , int port
            , String userName
            , String password
            , String messageBeanServer){
        this.hostName = hostname;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.messageBeanServer = messageBeanServer;
        
    }

    private void initConnection() throws JmsComponentException {
        try {
            serviceURL = new JMXServiceURL("t3", hostName, Integer.valueOf(port), jndiroot + messageBeanServer);

            HashMap h;
            h = new HashMap<String, String>();
            h.put(Context.SECURITY_PRINCIPAL, userName);
            h.put(Context.SECURITY_CREDENTIALS, password);
            h.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, "weblogic.management.remote");
            connector = JMXConnectorFactory.connect(serviceURL, h);

            connection = connector.getMBeanServerConnection();
        } catch (IOException ioex) {
            final String msg = "TODO";
            cor.addMsg(msg);
            throw new JmsComponentException(msg, ioex);
        }
    }
    
    public void closeConnection() throws JmsComponentException {
        try {
            this.connector.close();
        } catch (IOException ex) {
            final String msg = "TODO";
            cor.addMsg(msg);
            throw new JmsComponentException(msg, ex);
        }
    }
    public MBeanServerConnection getConnection() throws JmsComponentException {
        this.initConnection();
        return connection;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setMessageBeanServer(String messageBeanServer) {
        this.messageBeanServer = messageBeanServer;
    }   
}

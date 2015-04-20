package com.ibm.soatf.component.jms;

import com.ibm.soatf.flow.FrameworkExecutionException;
import com.ibm.soatf.flow.OperationResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.MBeanServerConnection;
import javax.management.ReflectionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BeanProvider {

    private static final Logger logger = LogManager.getLogger(BeanProvider.class.getName());
    private MBeanServerConnection connection;
    private ObjectName service;
    private final OperationResult cor = OperationResult.getInstance();

    BeanProvider() {
    }

    BeanProvider(MBeanServerConnection connection, ObjectName service) {
        logger.trace("Constructing BeanProvider object.");
        this.connection = connection;
        this.service = service;
    }

    public Iterable<String> getDistributedMemberJndiNames(String distributedDestJndiName) throws JmsComponentException {
        Iterable<String> serverNames = getJmsServerNames();
        Set<String> distributedDestNames = new TreeSet<String>();

        for (String serverName : serverNames) {
            distributedDestNames.add(serverName + "@" + distributedDestJndiName);
        }

        return distributedDestNames;
    }

    public Iterable<String> getJmsServerNames() throws JmsComponentException {
        Set<String> jmsServerNames = new TreeSet<String>();
        Iterable<ObjectName> jmsServers = getJMSServers();

        for (ObjectName jmsServer : jmsServers) {
            try {
                jmsServerNames.add((String) connection.getAttribute(jmsServer, "Name"));
            } catch (IOException | AttributeNotFoundException | InstanceNotFoundException | MBeanException | ReflectionException e) {
                final String msg = "TODO";
                cor.addMsg(msg);
                throw new JmsComponentException(msg, e);
            }
        }

        return jmsServerNames;

    }

    public Iterable<String> getJMSConnectedHosts() throws JmsComponentException {
        Iterable<ObjectName> jmsConnections = getJMSConnections();
        List<String> jmsConnectedHosts = new ArrayList<String>();
        for (ObjectName jmsRuntimeConnection : jmsConnections) {
            try {
                jmsConnectedHosts.add((String) connection.getAttribute(jmsRuntimeConnection, "HostAddress"));
            } catch (MBeanException | AttributeNotFoundException | InstanceNotFoundException | ReflectionException | IOException ex) {

            }
        }
        return jmsConnectedHosts;
    }

    public Iterable<ObjectName> getJMSConnections() throws JmsComponentException {
        Iterable<ObjectName> jmsRuntimes = getJMSRuntimes();
        List<ObjectName> jmsConnections = new ArrayList<ObjectName>();
        for (ObjectName jmsRuntime : jmsRuntimes) {
            try {
                ObjectName jmsConnectionArr[] = (ObjectName[]) connection
                        .getAttribute(jmsRuntime, "Connections");
                for (int i = 0; i < jmsConnectionArr.length; i++) {
                    jmsConnections.add(jmsConnectionArr[i]);
                }
            } catch (MBeanException | AttributeNotFoundException | InstanceNotFoundException | ReflectionException | IOException ex) {
                final String msg = "TODO";
                cor.addMsg(msg);
                throw new JmsComponentException(msg, ex);
            }

        }
        return jmsConnections;
    }

    public Iterable<ObjectName> getJMSServers() throws JmsComponentException {
        Iterable<ObjectName> jmsRuntimes = getJMSRuntimes();
        List<ObjectName> jmsServers = new ArrayList<ObjectName>();

        for (ObjectName jmsRuntime : jmsRuntimes) {
            try {
                ObjectName jmsServerArr[] = (ObjectName[]) connection
                        .getAttribute(jmsRuntime, "JMSServers");
                for (int i = 0; i < jmsServerArr.length; i++) {
                    jmsServers.add(jmsServerArr[i]);
                }
            } catch (MBeanException | AttributeNotFoundException | InstanceNotFoundException | ReflectionException | IOException ex) {
                final String msg = "TODO";
                cor.addMsg(msg);
                throw new JmsComponentException(msg, ex);
            }
        }

        return jmsServers;
    }

    public Iterable<ObjectName> getJMSRuntimes() throws JmsComponentException {
        Iterable<ObjectName> serverRuntimes = getServerRuntimeMBeans();
        List<ObjectName> jmsRuntimes = new ArrayList<ObjectName>();

        for (ObjectName serverRuntime : serverRuntimes) {
            try {
                jmsRuntimes.add((ObjectName) connection.getAttribute(serverRuntime, "JMSRuntime"));
            } catch (MBeanException | AttributeNotFoundException | InstanceNotFoundException | ReflectionException | IOException ex) {
                final String msg = "TODO";
                cor.addMsg(msg);
                throw new JmsComponentException(msg, ex);
            }
        }

        return jmsRuntimes;

    }

    public List<ObjectName> getServerRuntimeMBeans() throws JmsComponentException {
        try {
            return Arrays.asList((ObjectName[]) connection.getAttribute(service, "ServerRuntimes"));

        } catch (MBeanException | AttributeNotFoundException | InstanceNotFoundException | ReflectionException | IOException ex) {
            final String msg = "TODO";
            cor.addMsg(msg);
            throw new JmsComponentException(msg, ex);
        }
    }

    public Hashtable<String, String> getProperties() {
        return service.getKeyPropertyList();

    }

}

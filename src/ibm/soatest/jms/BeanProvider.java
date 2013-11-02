


package ibm.soatest.jms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.MBeanServerConnection;
import javax.management.ReflectionException;
import org.apache.logging.log4j.LogManager;



public class BeanProvider {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(DistribuedQueueBrowser.class.getName());
    
    private MBeanServerConnection connection;
    private ObjectName service;

    BeanProvider() {
    }

    BeanProvider(MBeanServerConnection connection, ObjectName service) {
        logger.debug("");
        this.connection = connection;
        this.service = service;
    }

    public Iterable<String> getDistributedMemberJndiNames(String distributedDestJndiName) {
        Iterable<String> serverNames = getJmsServerNames();
        Set<String> distributedDestNames = new TreeSet<String>();

        for (String serverName : serverNames) {
            distributedDestNames.add(serverName + "@" + distributedDestJndiName);
        }

        return distributedDestNames;
    }

    public Iterable<String> getJmsServerNames() {
        Set<String> jmsServerNames = new TreeSet<String>();
        Iterable<ObjectName> jmsServers = getJMSServers();

        for (ObjectName jmsServer : jmsServers) {
            try {
                jmsServerNames.add((String) connection.getAttribute(jmsServer, "Name"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return jmsServerNames;

    }
    public Iterable<String> getJMSConnectedHosts(){
        Iterable<ObjectName> jmsConnections = getJMSConnections();
        List<String> jmsConnectedHosts = new ArrayList<String>(); 
        for (ObjectName jmsRuntimeConnection : jmsConnections) {
            try {
                jmsConnectedHosts.add( (String) connection.getAttribute(jmsRuntimeConnection, "HostAddress") );
            } catch (MBeanException ex) {
                Logger.getLogger(BeanProvider.class.getName()).log(Level.SEVERE, null, ex);
            } catch (AttributeNotFoundException ex) {
                Logger.getLogger(BeanProvider.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstanceNotFoundException ex) {
                Logger.getLogger(BeanProvider.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ReflectionException ex) {
                Logger.getLogger(BeanProvider.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(BeanProvider.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jmsConnectedHosts;
    }
    
    public Iterable<ObjectName> getJMSConnections(){
        Iterable<ObjectName> jmsRuntimes = getJMSRuntimes();
        List<ObjectName> jmsConnections = new ArrayList<ObjectName>();
        for (ObjectName jmsRuntime : jmsRuntimes) {
            try {
                ObjectName jmsConnectionArr[] = (ObjectName[]) connection
                        .getAttribute(jmsRuntime, "Connections");
                for (int i = 0; i < jmsConnectionArr.length; i++) {
                    jmsConnections.add(jmsConnectionArr[i]);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return jmsConnections;    
    }
    
    public Iterable<ObjectName> getJMSServers() {
        Iterable<ObjectName> jmsRuntimes = getJMSRuntimes();
        List<ObjectName> jmsServers = new ArrayList<ObjectName>();

        for (ObjectName jmsRuntime : jmsRuntimes) {
            try {
                ObjectName jmsServerArr[] = (ObjectName[]) connection
                        .getAttribute(jmsRuntime, "JMSServers");
                for (int i = 0; i < jmsServerArr.length; i++) {
                    jmsServers.add(jmsServerArr[i]);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return jmsServers;
    }
    
    public Iterable<ObjectName> getJMSRuntimes() {
        Iterable<ObjectName> serverRuntimes = getServerRuntimeMBeans();
        List<ObjectName> jmsRuntimes = new ArrayList<ObjectName>();

        for (ObjectName serverRuntime : serverRuntimes) {
            try {
                jmsRuntimes.add((ObjectName) connection.getAttribute(serverRuntime, "JMSRuntime"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return jmsRuntimes;

    }

    public List<ObjectName> getServerRuntimeMBeans() {

        try {
            return Arrays.asList((ObjectName[]) connection.getAttribute(service, "ServerRuntimes"));

        } catch (Exception e) {
            throw new RuntimeException("Error obtaining Server Runtime Information", e);

        }
    }

    public Hashtable<String, String> getProperties() {
        return service.getKeyPropertyList();

    }

}

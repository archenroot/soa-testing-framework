package com.ibm.fm.soatest.jms;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.ibm.fm.soatest.database.DatabaseComponent;



public class DistribuedQueueBrowser {
  private final Connection connection;
  private final Session session;
  private final InitialContext ctx;
  private final Iterable<String> queueNames;
  
  Logger logger = LogManager.getLogger(DistribuedQueueBrowser.class.getName());
  
    private final String hostName = "prometheus";
    private final int port = 7001;
    private final String userName = "weblogic";
    private final String password = "Weblogic123";

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
    private String messageBeanServer = "weblogic.management.mbeanservers.domainruntime";

    
    

  public DistribuedQueueBrowser(
          String adminUrl, 
          String providerUrl,
          String distributedDestinationName, 
          String userName, 
          String password) throws Exception {
    ctx = getInitialContext(providerUrl, userName, password);
    WeblogicMBeanHelper factory = null;

    try {
      factory = new WeblogicMBeanHelper(adminUrl, userName, password);
      queueNames = factory.getDistributedMemberJndiNames(distributedDestinationName);
    }
    finally {
      if (factory != null) {
        factory.close();
      }
    }

    ConnectionFactory connFactory = (ConnectionFactory) ctx
        .lookup("jms.OSBSamplesJMSConnectionFactory");
    connection = connFactory.createConnection();
    connection.start();
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
  }

  
  public void printQueueMessages() {
        try {
            // Note that the first argument is the admin url and the second is the
            // managed server url.
            
          
          
            Enumeration<ServerLocatedMessage> sli = this.getServerLocatedEnumeration();
            int i = 0;
            while (sli.hasMoreElements()) {
                i++;
                ServerLocatedMessage m = sli.nextElement();
                TextMessage mes = (TextMessage) m.getMessage();
                /*
                for (Enumeration<String> e = mes.getPropertyNames(); e.hasMoreElements();)
                        System.out.println("enum" + e.nextElement().toString());
                
              
                System.out.println("Correlation id:" + mes.getJMSCorrelationID());
                System.out.println("Message id:" + mes.getJMSCorrelationID());
                System.out.println("JMS Type:" + mes.getJMSType());
                System.out.println("Content: " + mes.getText());
                */
                
                this.writeStatementToFile(mes.getText(), "test/jms/message_" + i + ".xml");
                System.out.println(m);
            }
        } catch (JMSException ex) {
            
        } catch (NamingException ex) {
            
        } catch (Exception ex) {
            
        }
    }
  
  private InitialContext getInitialContext(String providerUrl, String userName, String password) throws Exception {
    Hashtable<String, String> ht = new Hashtable<String, String>();

    ht.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
    ht.put(Context.PROVIDER_URL, providerUrl);
    ht.put(Context.SECURITY_PRINCIPAL, userName);
    ht.put(Context.SECURITY_CREDENTIALS, password);

    return new InitialContext(ht);
  }

  public Enumeration<Message> getEnumeration() throws JMSException, NamingException {
    return new JmsMessageEnumeration(getMessageEnumeratorMap());
  }

  @SuppressWarnings("unchecked") private Map<String, Enumeration<Message>> getMessageEnumeratorMap() throws JMSException,
    NamingException {
    Map<String, Enumeration<Message>> serverMessageMap = new HashMap<String, Enumeration<Message>>();

    for (String queueName : queueNames) {
        if ("OSBSamplesJMSServer@OSBWriteQueue".equals(queueName)){
            String serverDq[] = StringUtils.split(queueName, "@");
            queueName = "jms.OSBWriteQueue";
            Queue queue = (Queue) ctx.lookup(queueName);
            javax.jms.QueueBrowser qb = session.createBrowser(queue);
            serverMessageMap.put(serverDq[0], qb.getEnumeration());
        }
    }

    return serverMessageMap;
  }

  public Enumeration<ServerLocatedMessage> getServerLocatedEnumeration() throws JMSException,
    NamingException {
    return new ServerLocatedMessageEnumeration(getMessageEnumeratorMap());
  }

  

  private static abstract class AbstractMessageEnumeration<T> implements Enumeration<T> {
    Map.Entry<String, Enumeration<Message>> current;

    private Enumeration<Message> currMessageEnumer;

    private final Iterator<Map.Entry<String, Enumeration<Message>>> iterator;

    public AbstractMessageEnumeration(Map<String, Enumeration<Message>> map) {
      iterator = map.entrySet().iterator();
      current = iterator.hasNext()
          ? iterator.next()
          : null;
      currMessageEnumer = current != null
          ? current.getValue()
          : new Enumeration<Message>() {

            public boolean hasMoreElements() {
              return false;
            }

            public Message nextElement() {
              throw new NoSuchElementException();
            }
          };
    }

    Enumeration<Message> getEnumeration() {
      if (current == null || currMessageEnumer.hasMoreElements()) {
        return currMessageEnumer;
      }

      while (iterator.hasNext()) {
        current = iterator.next();
        currMessageEnumer = current.getValue();
        if (currMessageEnumer.hasMoreElements()) {
          return currMessageEnumer;
        }
      }

      return currMessageEnumer;
    }

    public boolean hasMoreElements() {
      return getEnumeration().hasMoreElements();
    }
  }

  private static class ServerLocatedMessageEnumeration extends
      AbstractMessageEnumeration<ServerLocatedMessage> {

    public ServerLocatedMessageEnumeration(Map<String, Enumeration<Message>> map) {
      super(map);
    }

    public ServerLocatedMessage nextElement() {
      Message message = getEnumeration().nextElement();
      return new ServerLocatedMessage(current.getKey(), message);
    }
  }

  private static class JmsMessageEnumeration extends AbstractMessageEnumeration<Message> {
    public JmsMessageEnumeration(Map<String, Enumeration<Message>> map) {
      super(map);
    }

    public Message nextElement() {
      return getEnumeration().nextElement();
    }
  }

  public void close() {
    try {
      session.close();
    }
    catch (JMSException ignored) {
    }
    try {
      connection.close();
    } catch (JMSException ignored) {}

    try {
      ctx.close();
    }
    catch (NamingException ignored) {
    }
  }
  
   private void writeStatementToFile(String statement, String pathToFile) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(new File(pathToFile));
            fw.write(statement);
            fw.flush();
            fw.close();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(DatabaseComponent.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fw.close();
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(DatabaseComponent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
   }
}

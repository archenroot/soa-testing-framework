package com.ibm.soatf.component.jms;

import com.ibm.soatf.component.ComponentResult;
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
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.ibm.soatf.component.database.DatabaseComponent;
import com.ibm.soatf.tool.FileSystem;
import com.ibm.soatf.component.util.Utils;

public class DistribuedQueueBrowser {

    private static final String QUEUE_AT_SERVER_SIGN = "@";
    private static final String INITIAL_CONTEXT_FACTORY = "weblogic.jndi.WLInitialContextFactory";
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
    private static final String messageBeanServer = "weblogic.management.mbeanservers.domainruntime";

    private static final Logger logger = LogManager.getLogger(DistribuedQueueBrowser.class.getName());

    private final InitialContext ctx;
    private final Connection connection;
    private final Session session;
    private final Iterable<String> queueNames;

    private final String jmsServerName;
    private final String distributedDestinationName;
    private final String distributedDestinationJndi;

    private String workingDirectory;

    public DistribuedQueueBrowser(
            String workingDirectory,
            String adminUrl,
            String providerUrl,
            String jmsServerName,
            String connectionFactoryName,
            String distributedDestinationName,
            String distributedDestinationJndi,
            String userName,
            String password) throws Exception {
        this.jmsServerName = jmsServerName;
        this.distributedDestinationName = distributedDestinationName;
        this.distributedDestinationJndi = distributedDestinationJndi;
        this.workingDirectory = workingDirectory;

        WeblogicMBeanHelper factory = null;
        try {
            factory = new WeblogicMBeanHelper(adminUrl, userName, password);
            queueNames = factory.getDistributedMemberJndiNames(distributedDestinationName);
        } finally {
            if (factory != null) {
                factory.close();
            }
        }

        ctx = getInitialContext(providerUrl, userName, password);
        ConnectionFactory connFactory = (ConnectionFactory) ctx.lookup(connectionFactoryName);
        connection = connFactory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    public int printQueueMessagesByContent(String content) throws NoMessageFoundException, JMSException, NamingException {
        Enumeration<ServerLocatedMessage> sli = this.getServerLocatedEnumeration();
        int i = 0;
        if (!sli.hasMoreElements()) {
            throw new NoMessageFoundException();
        }

        while (sli.hasMoreElements()) {
            ServerLocatedMessage m = sli.nextElement();
            TextMessage mes = (TextMessage) m.getMessage();
            //

            String jmsMessageId = m.getMessage().getJMSMessageID().replaceAll("ID:<", "").replaceAll(">", "");

            if (content != null) {
                if (mes == null || mes.getText() == null || !mes.getText().contains(content)) {
                    logger.debug("skipping" + mes);
                    continue;
                }
            }
            /*
             for (Enumeration<String> e = mes.getPropertyNames(); e.hasMoreElements();)
             System.out.println("enum" + e.nextElement().toString());


             System.out.println("Correlation id:" + mes.getJMSCorrelationID());
             System.out.println("Message id:" + mes.getJMSCorrelationID());
             System.out.println("JMS Type:" + mes.getJMSType());
             System.out.println("Content: " + mes.getText());
             */

            String filename = new StringBuilder(distributedDestinationName)
                    .append(JMSComponent.NAME_DELIMITER)
                    .append(jmsMessageId)
                    .append(JMSComponent.NAME_DELIMITER)
                    .append(i)
                    .append(JMSComponent.MESSAGE_SUFFIX)
                    .toString();
            String path = Utils.getFullFilePathStr(workingDirectory, "\\", filename);
            this.writeStatementToFile(mes.getText(), path);
            System.out.println(m);
            ++i;
        }
        if (0 == i) {
            throw new NoMessageFoundException();
        }
        return i;
    }

    public int printQueueMessages() throws NoMessageFoundException, JMSException, NamingException {
        return printQueueMessagesByContent(null);
    }

    private InitialContext getInitialContext(String providerUrl, String userName, String password) throws NamingException {
        Hashtable<String, String> ht = new Hashtable<String, String>();

        ht.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
        ht.put(Context.PROVIDER_URL, providerUrl);
        ht.put(Context.SECURITY_PRINCIPAL, userName);
        ht.put(Context.SECURITY_CREDENTIALS, password);

        return new InitialContext(ht);
    }

    public Enumeration<Message> getEnumeration() throws JMSException, NamingException {
        return new JmsMessageEnumeration(getMessageEnumeratorMap());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Enumeration<Message>> getMessageEnumeratorMap() throws JMSException, NamingException {
        Map<String, Enumeration<Message>> serverMessageMap = new HashMap<String, Enumeration<Message>>();

        String queueAtServer = jmsServerName + QUEUE_AT_SERVER_SIGN + distributedDestinationName;
        for (String queueName : queueNames) {
            if (queueAtServer.equals(queueName)) {
                String serverDq[] = StringUtils.split(queueName, QUEUE_AT_SERVER_SIGN);
                queueName = distributedDestinationJndi;
                Queue queue = (Queue) ctx.lookup(queueName);
                logger.debug(queue);
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

                        @Override
                        public boolean hasMoreElements() {
                            return false;
                        }

                        @Override
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
        } catch (JMSException ignored) {
        }
        try {
            connection.close();
        } catch (JMSException ignored) {
        }

        try {
            ctx.close();
        } catch (NamingException ignored) {
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

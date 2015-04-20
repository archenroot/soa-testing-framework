package com.ibm.soatf.component.jms;

import com.ibm.soatf.flow.OperationResult;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    private InitialContext ctx = null;
    private Connection connection;
    private Session session;
    private Iterable<String> queueNames;
    private String jmsServerName;
    private String distributedDestinationName;
    private String distributedDestinationJndi;

    private File workingDirectory;

    private final OperationResult cor = OperationResult.getInstance();

    public DistribuedQueueBrowser(
            File workingDirectory,
            String adminUrl,
            String providerUrl,
            String jmsServerName,
            String connectionFactoryName,
            String distributedDestinationName,
            String distributedDestinationJndi,
            String userName,
            String password) throws JmsComponentException {
        try {
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
        } catch (JMSException | NamingException ex) {
            final String msg = "Problem catched while trying to get JMS messages from the queue.";
            cor.addMsg(msg);
            throw new JmsComponentException(msg, ex);
        }
    }

    public Map<File, TextMessage> getQueueMessagesByContent(String content) throws JmsComponentException {
        Map<File, TextMessage> messages = new HashMap<>();

        Enumeration<ServerLocatedMessage> sli = this.getServerLocatedEnumeration();
        int i = 0;
        if (!sli.hasMoreElements()) {
            throw new NoMessageFoundException("Queue: " + this.distributedDestinationJndi);
        }
        while (sli.hasMoreElements()) {
            try {
                ServerLocatedMessage m = sli.nextElement();
                TextMessage mes = (TextMessage) m.getMessage();
                String jmsMessageId = m.getMessage().getJMSMessageID().replaceAll("ID:<", "").replaceAll(">", "");
                if (content != null) {
                    if (mes == null || mes.getText() == null || !mes.getText().contains(content)) {
                        logger.debug("Skipping message" + mes);
                        continue;
                    } else {

                    }
                }
                String filename = new StringBuilder(distributedDestinationName)
                        .append(JmsComponent.NAME_DELIMITER)
                        .append(jmsMessageId)
                        .append(JmsComponent.NAME_DELIMITER)
                        .append(i)
                        .append(JmsComponent.MESSAGE_SUFFIX)
                        .toString();
                File file = new File(workingDirectory, filename);
                messages.put(file, mes);                
                FileUtils.writeStringToFile(file, mes.getText());
                System.out.println(m);
                ++i;
            } catch (JMSException ex) {
                final String msg = "Problem occured while trying to get JMS messages from the queue.";
                cor.addMsg(msg, ex.getMessage());
                throw new JmsComponentException(msg, ex);
            } catch (IOException ex) {
                final String msg = "Unable to save message to disk.";
                cor.addMsg(msg, ex.getMessage());
                throw new JmsComponentException(msg, ex);
            }

        }
        /*if (0 == i) {
                
         throw new NoMessageFoundException();
         }
         */
        return messages;
    }

    public Map<File,TextMessage> getQueueMessages() throws JmsComponentException {
        return getQueueMessagesByContent(null);
    }

    private InitialContext getInitialContext(String providerUrl, String userName, String password) throws JmsComponentException {
        try {

            Hashtable<String, String> ht = new Hashtable<String, String>();
            ht.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
            ht.put(Context.PROVIDER_URL, providerUrl);
            ht.put(Context.SECURITY_PRINCIPAL, userName);
            ht.put(Context.SECURITY_CREDENTIALS, password);

            // Using obsolete collection type, prepared for future changes in source code
            return new InitialContext(ht);
        } catch (NamingException ne) {
            final String msg = "Execption raised when trying to create initial context for specified weblogic server 'providerUrl'";
            logger.error(msg);
            throw new JmsComponentException(ne);

        }
    }

    public Enumeration<Message> getEnumeration() throws JmsComponentException {
        return new JmsMessageEnumeration(getMessageEnumeratorMap());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Enumeration<Message>> getMessageEnumeratorMap() throws JmsComponentException {
        Map<String, Enumeration<Message>> serverMessageMap = new HashMap<String, Enumeration<Message>>();

        String queueAtServer = jmsServerName + QUEUE_AT_SERVER_SIGN + distributedDestinationName;
        for (String queueName : queueNames) {
            if (queueAtServer.equals(queueName)) {
                try {
                    String serverDq[] = StringUtils.split(queueName, QUEUE_AT_SERVER_SIGN);
                    queueName = distributedDestinationJndi;
                    Queue queue = (Queue) ctx.lookup(queueName);
                    logger.debug(queue);
                    javax.jms.QueueBrowser qb = session.createBrowser(queue);
                    serverMessageMap.put(serverDq[0], qb.getEnumeration());
                } catch (NamingException | JMSException ex) {
                    final String msg = "Exception found when trying to get enumeration of messages within JMS queue.";
                    throw new JmsComponentException(msg, ex);
                }
            }
        }

        return serverMessageMap;
    }

    public Enumeration<ServerLocatedMessage> getServerLocatedEnumeration() throws JmsComponentException {
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

    public void close() throws JmsComponentException {
        try {
            session.close();
        } catch (JMSException ex) {
            final String msg = "TODO";
            cor.addMsg(msg);
            throw new JmsComponentException(msg, ex);
        }
        try {
            connection.close();
        } catch (JMSException ex) {
            final String msg = "TODO";
            cor.addMsg(msg);
            throw new JmsComponentException(msg, ex);
        }

        try {
            ctx.close();
        } catch (NamingException ex) {
            final String msg = "TODO";
            cor.addMsg(msg);
            throw new JmsComponentException(msg, ex);
        }
    }
}

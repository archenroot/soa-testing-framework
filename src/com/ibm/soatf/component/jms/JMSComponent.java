/*
 * Copyright (C) 2013 zANGETSu
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
package com.ibm.soatf.component.jms;

import com.ibm.soatf.flow.FrameworkExecutionException;
import com.ibm.soatf.component.CompOperType;
import com.ibm.soatf.component.SOATFCompType;
import com.ibm.soatf.component.AbstractSoaTFComponent;
import com.ibm.soatf.config.iface.jms.JMSConfig;
import com.ibm.soatf.config.master.Operation;
import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance;
import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance.AdminServer;
import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance.Cluster.ManagedServer;
import com.ibm.soatf.flow.FlowPatternCompositeKey;
import com.ibm.soatf.flow.OperationResult;
import com.ibm.soatf.xml.ValidatorResult;
import com.ibm.soatf.xml.XMLSubsystemException;
import com.ibm.soatf.xml.XMLValidator;
import com.ibm.soatf.xml.XmlFormatter;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * JMS Component is responsible to operate over any JMS related subsystem,
 * including JMS queues and topics.
 *
 * @author Ladislav Jech <archenroot@gmail.com>
 */
public class JmsComponent extends AbstractSoaTFComponent {

    /**
     * Default protocol to connect to fusion middleware product stack.
     */
    public static final String DEFAULT_PROTO = "t3";

    /**
     * Default suffix for JMS related artefacts.
     *
     *
     */
    public static final String MESSAGE_SUFFIX = ".xml";

    /**
     *
     */
    public static final String NAME_DELIMITER = "_";

    /**
     *
     */
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private static final Logger logger = LogManager.getLogger(JmsComponent.class.getName());

    /**
     *
     */
    public static Set<CompOperType> supportedOperations = CompOperType.JMS_OPERATIONS;

    private final OracleFusionMiddlewareInstance jmsMasterConfig;
    private final JMSConfig jmsInterfaceConfig;

    private AdminServer adminServer;
    private ManagedServer managedServer;
    private String queueName;

    private final OperationResult cor;

    /**
     *
     * @param jmsMasterConfig
     * @param jmsInterfaceConfig
     * @param ifaceFlowPatternCompositeKey
     * @param workingDir
     */
    public JmsComponent(
            OracleFusionMiddlewareInstance jmsMasterConfig,
            JMSConfig jmsInterfaceConfig,
            FlowPatternCompositeKey ifaceFlowPatternCompositeKey,
            File workingDir) {
        super(SOATFCompType.JMS);

        this.jmsMasterConfig = jmsMasterConfig;
        this.jmsInterfaceConfig = jmsInterfaceConfig;
        this.workingDir = workingDir;
        cor = OperationResult.getInstance();
        constructComponent();
    }

    /**
     *
     */
    @Override
    protected void constructComponent() {

        this.queueName = jmsInterfaceConfig.getQueue().getName();
        this.adminServer = jmsMasterConfig.getAdminServer();
        this.managedServer = jmsMasterConfig.getCluster().getManagedServer();
    }

    /**
     *
     * @param operation
     */
    @Override
    public void executeOperation(Operation operation) throws FrameworkExecutionException {
        /*if (!supportedOperations.contains(operation.getName())) {
         try {
         throw new com.ibm.soatf.UnsupportedComponentOperationException();
         } catch (    com.ibm.soatf.UnsupportedComponentOperationException ex) {
         logger.error("Component operation is not supported." + ex.getLocalizedMessage());
         return; // cor;
         }
         }
         */
        try {
            switch (operation.getName()) {
                case JMS_RECEIVE_MESSAGE_FROM_QUEUE:

                    readAllMessagesInQueue();

                    break;
                case JMS_CHECK_ERROR_QUEUE_FOR_MESSAGE:
                    checkErrorQueue();
                    break;
                case JMS_PURGE_QUEUE:
                    purgeQueue();
                    break;
                case JMS_VALIDATE_MESSAGE:
                    validateMessageFiles();
                    break;

                case JMS_DESTINATION_PAUSE_PRODUCTION:
                    pauseDestionationPoduction();
                    break;
                case JMS_DESTINATION_RESUME_PRODUCTION:
                    resumeDestinationConsumption();
                    break;
                default:
                    break;

            }
        } catch (JmsComponentException ex) {
            throw new FrameworkExecutionException(ex);
        }
    }

    private void readAllMessagesInQueue() throws JmsComponentException {
        try {
            DistribuedQueueBrowser dqb = new DistribuedQueueBrowser(
                    workingDir,
                    DEFAULT_PROTO + "://" + adminServer.getHost() + ":" + adminServer.getPort(),
                    DEFAULT_PROTO + "://" + managedServer.getHostName() + ":" + managedServer.getPort(),
                    jmsInterfaceConfig.getJmsServer().getName(),
                    jmsInterfaceConfig.getConnectionFactory().getName(),
                    jmsInterfaceConfig.getQueue().getName(),
                    jmsInterfaceConfig.getQueue().getJndiName(),
                    adminServer.getSecurityPrincipal(),
                    adminServer.getSecurityCredentials());
            List<TextMessage> messages = dqb.getQueueMessages();
            int count = messages.size();
            if (count < 0) {
                final String msg = "There has been found no message in JMS queue "
                        + jmsInterfaceConfig.getQueue().getName()
                        + " accessed trough admin server="
                        + adminServer.getJmxProtocol()
                        + "://"
                        + adminServer.getHost()
                        + ":"
                        + adminServer.getPort();
                logger.error(msg);
                throw new NoMessageFoundException(msg);
            }
            cor.markSuccessful();
            //cor.addMsg("Message read from " + jmsInterfaceConfig.getQueue().getName() + " queue available at " + managedServer.getHostName() + " managed server.");
            cor.addMsg(//count 
                    "1 message were read from queue "
                    + jmsInterfaceConfig.getQueue().getName()
                    + "."
                    + "\nContent of the message:\n"
                    + new XmlFormatter().format(messages.get(0).getText()));

            logger.info(count + " messages were read from queue " + jmsInterfaceConfig.getQueue().getName() + ".");
        } catch (JMSException | NoMessageFoundException | XMLSubsystemException e) {
            cor.addMsg(e.getMessage());
            throw new JmsComponentException(e);
        }
    }

    /**
     *
     * @param objectName
     * @return
     */
    public Iterator<File> getGeneratedFiles(String objectName) {
        String pattern = "*";
        if (objectName != null) {
            pattern = objectName;
        }
        String filemask = new StringBuilder(jmsInterfaceConfig.getQueue().getName()).append(NAME_DELIMITER).append(pattern).append(MESSAGE_SUFFIX).toString();
        Iterator it = FileUtils.iterateFiles(workingDir, new WildcardFileFilter(filemask), TrueFileFilter.INSTANCE);
        return it;
    }

    private void validateMessageFiles() {
        Iterator iterateFiles = getGeneratedFiles(null);
        while (iterateFiles.hasNext()) {
            String fileToValidate = iterateFiles.next().toString();

            ValidatorResult result = XMLValidator.validateXMLFile(fileToValidate, jmsInterfaceConfig.getMessageSchema().getPath());
            if (!result.isValid()) {
                cor.addMsg("Message schema validation resulted in FAIL:"
                        + "\n message file: " + fileToValidate
                        + "\n schema file: " + jmsInterfaceConfig.getMessageSchema().getPath()
                        + "\n validator result: \n" + result.getValidatorMessage());
                //cor.addMsg("Message in queue " + jmsInterfaceConfig.getQueue().getName() + " is not valid");
                logger.info("Message in queue " + jmsInterfaceConfig.getQueue().getName() + " is not valid.");
                return;
            }
            logger.info("Message in queue " + jmsInterfaceConfig.getQueue().getName() + " is valid.");
            cor.addMsg("Message schema validation resulted in SUCCESS:"
                    + "\nMessage file: " + fileToValidate
                    + "\nXML schema file: " + jmsInterfaceConfig.getMessageSchema().getPath()
                    + "\nValidator result: " + result.getValidatorMessage());
            cor.markSuccessful();
        }
    }

    private void purgeQueue() throws JmsComponentException {
        try {
            logger.debug("Trying to purge queue " + jmsInterfaceConfig.getQueue().getJndiName());
            PurgeQueue pq = new PurgeQueue();
            List<Message> messages = pq.deleteAllMessagesFromQueue(jmsInterfaceConfig, jmsMasterConfig);
            StringBuilder sb = new StringBuilder();
            sb.append("Queue " + jmsInterfaceConfig.getQueue().getName() + " configured at "
                    + jmsMasterConfig.getCluster().getManagedServer().getHostName() + " managed server has been purged.");
            sb.append("\nMessage count deleted: " + messages.size() + ".");
            //sb.append("\nMessage count deleted: " + messages.size() + ".");

            for (Message mes : messages) {
                sb.append("\nMessage " + ((TextMessage) mes).getJMSMessageID() + " content:");
                sb.append("\n" + new XmlFormatter().format(((TextMessage) mes).getText()));

            }
            cor.markSuccessful();
            cor.addMsg(sb.toString());
            logger.info("Queue " + jmsInterfaceConfig.getQueue().getName() + " was purged.");
        } catch (JMSException | XMLSubsystemException ex) {
            final String msg = "Queue " + jmsInterfaceConfig.getQueue().getName() + " configured at "
                    + jmsMasterConfig.getCluster().getManagedServer().getHostName() + " managed server cannot be purged:\n"
                    + getStackTrace(ex);

            cor.addMsg(msg);
            throw new JmsComponentException(ex);
        }
    }

    private String getJMSMessageIdFromFileName(String string) {
        String messageId;
        int start = string.indexOf("_") + 1;
        int end = string.lastIndexOf("_");
        messageId = "ID:<" + string.substring(start, end) + ">";
        return messageId;
    }

    private void checkErrorQueue() throws JmsComponentException {
        try {
            DistribuedQueueBrowser dqb = new DistribuedQueueBrowser(workingDir, DEFAULT_PROTO + "://" + adminServer.getHost() + ":" + adminServer.getPort(),
                    DEFAULT_PROTO + "://" + managedServer.getHostName() + ":" + managedServer.getPort(),
                    jmsInterfaceConfig.getJmsServer().getName(),
                    jmsInterfaceConfig.getConnectionFactory().getName(),
                    "ErrorQueue",
                    "ErrorQueue",
                    adminServer.getSecurityPrincipal(),
                    adminServer.getSecurityCredentials());

            Iterator<File> messageFilesIterator = getGeneratedFiles(null);
            while (messageFilesIterator.hasNext()) {
                String fileName = messageFilesIterator.next().getName();
                String messageId = getJMSMessageIdFromFileName(fileName);
                //String path = messageFilesIterator.next().getAbsolutePath();
                //String content = FileSystem.readContentFromFile(path);
                Enumeration<ServerLocatedMessage> sli = dqb.getServerLocatedEnumeration();

                boolean messageFound;
                int i = 0;
                if (!sli.hasMoreElements()) {
                    throw new NoMessageFoundException();
                }

                while (sli.hasMoreElements()) {
                    ServerLocatedMessage m = sli.nextElement();
                    TextMessage mes = (TextMessage) m.getMessage();

                    //String jmsMessageId = m.getMessage().getJMSMessageID().replaceAll("ID:<", "").replaceAll(">", "");
                    String jmsMessageId = m.getMessage().getJMSMessageID();

                    if (jmsMessageId.equals(messageId)) {
                        messageFound = true;
                        cor.markSuccessful();
                        cor.addMsg("Message '" + messageId + "' is presented in queue " + jmsInterfaceConfig.getQueue().getName() + " which is defined as on error delivery queue.");
                        logger.info("Message in queue " + jmsInterfaceConfig.getQueue().getName() + " is present in Error Queue.");
                        return;
                    }

                }
                cor.addMsg("There cannot be found message with id = '"
                        + messageId + "' withing Error queue defined as " + jmsInterfaceConfig.getQueue().getName() + ".");
                logger.info("There are no messages in Error Queue related to queue " + jmsInterfaceConfig.getQueue().getName() + ".");

            }
        } catch (JMSException ex) {

            throw new JmsComponentException(ex);
        }
    }

    private void pauseDestionationPoduction() {
        try {
            queueName = jmsInterfaceConfig.getQueue().getJndiName();
            InitialContext ctx = getInitialContext(DEFAULT_PROTO + "://" + managedServer.getHostName() + ":" + managedServer.getPort(),
                    adminServer.getSecurityPrincipal(),
                    adminServer.getSecurityCredentials());
            //Queue queue = (Queue) ctx.lookup(queueName);
            Destination queue = (Destination) ctx.lookup(jmsInterfaceConfig.getQueue().getJndiName());

            weblogic.management.runtime.JMSDestinationRuntimeMBean destMBean = weblogic.jms.extensions.JMSRuntimeHelper.getJMSDestinationRuntimeMBean(ctx, queue);
            destMBean.pauseProduction();
            if (destMBean.isProductionPaused()) {
                cor.markSuccessful();
                cor.addMsg(queueName + " queue active at " + managedServer.getHostName() + ":" + managedServer.getPort() + " production has been paused.");
            } else {
                cor.addMsg(queueName + " queue active at " + managedServer.getHostName() + ":" + managedServer.getPort() + " production pausing process finished ok, but the queue is not paused."
                        + "\nTry to restart the server.");
            }
        } catch (NamingException | JMSException ex) {
            cor.addMsg(queueName + " queue active at " + managedServer.getHostName() + ":" + managedServer.getPort() + " production pausing finished with exception:"
                    + ex.getMessage());

            //throw new FrameworkExecutionException(ex);
        }
    }

    private void resumeDestinationConsumption() {
        try {
            queueName = "iw.MyIrishWaterQueue";
            InitialContext ctx = getInitialContext(DEFAULT_PROTO + "://" + managedServer.getHostName() + ":" + managedServer.getPort(),
                    adminServer.getSecurityPrincipal(),
                    adminServer.getSecurityCredentials());
            //Queue queue = (Queue) ctx.lookup(queueName);
            Destination queue = (Destination) ctx.lookup(jmsInterfaceConfig.getQueue().getJndiName());
            weblogic.management.runtime.JMSDestinationRuntimeMBean destMBean = weblogic.jms.extensions.JMSRuntimeHelper.getJMSDestinationRuntimeMBean(ctx, queue);
            destMBean.resumeProduction();

            cor.markSuccessful();
            cor.addMsg(queueName + " queue active at " + managedServer.getHostName() + ":" + managedServer.getPort() + " production has been resumed.");
            /*
             cor.setOverallResultSuccess(false);
             cor.addMsg(queueName + " queue active at " + managedServer.getHostName() + ":" + managedServer.getPort() + " production pausing process finished ok, but the queue is not paused." 
             + "\nTry to restart the server.");
             */
        } catch (NamingException | JMSException ex) {
            cor.addMsg(queueName + " queue active at " + managedServer.getHostName() + ":" + managedServer.getPort() + " production pausing finished with exception:"
                    + ex.getMessage());
            //throw new FrameworkExecutionException(ex);
        }
    }

    private InitialContext getInitialContext(String providerUrl, String userName, String password) throws NamingException {
        Hashtable<String, String> ht = new Hashtable<String, String>();

        ht.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
        ht.put(Context.PROVIDER_URL, providerUrl);
        ht.put(Context.SECURITY_PRINCIPAL, userName);
        ht.put(Context.SECURITY_CREDENTIALS, password);

        return new InitialContext(ht);
    }

    /**
     *
     */
    @Override
    protected void destructComponent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

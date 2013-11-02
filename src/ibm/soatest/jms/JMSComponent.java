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
package ibm.soatest.jms;

import ibm.soatest.CompOperResult;
import ibm.soatest.SOATFComponent;
import ibm.soatest.SOATFCompType;
import ibm.soatest.CompOperType;
import ibm.soatest.config.AdminServer;
import ibm.soatest.config.JMSConfiguration;
import ibm.soatest.config.ManagedServer;
import ibm.soatest.config.OSBConfiguration;
import ibm.soatest.mapping.IMappingEndpoint;
import ibm.soatest.tool.FileSystem;
import ibm.soatest.util.Utils;
import ibm.soatest.xml.ValidatorResult;
import ibm.soatest.xml.XMLValidator;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Set;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public class JMSComponent extends SOATFComponent implements IMappingEndpoint {
    public static final String DEFAULT_PROTO = "t3";
    public static final String MESSAGE_SUFFIX = ".xml";
    public static final String NAME_DELIMITER = "_";
    
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    
    private static final Logger logger = LogManager.getLogger(JMSComponent.class.getName());
    
    public static Set<CompOperType> supportedOperations = CompOperType.jmsOperations;   
    
    private final JMSConfiguration jmsConfiguration;
    private final OSBConfiguration osbConfiguration;
    
    private AdminServer adminServer;
    private ManagedServer managedServer; 
    private String queueName;
    
    public JMSComponent(JMSConfiguration jmsConfiguration, OSBConfiguration osbConfiguration, CompOperResult componentOperationResult, String identificator) {
        super(SOATFCompType.JMS, componentOperationResult, identificator);
        this.jmsConfiguration = jmsConfiguration;
        this.osbConfiguration = osbConfiguration;
        
        constructComponent();
    }
    @Override
    protected void constructComponent() {
        this.queueName = jmsConfiguration.getQueue().getName();        
        this.adminServer = this.osbConfiguration.getAdminServer();
        this.managedServer = this.osbConfiguration.getCluster().getManagedServer().get(0);
    }

    @Override
    public void executeOperation(CompOperType componentOperation) {
        this.getComponentOperationResult().setCompOperType(componentOperation);
        if (supportedOperations.contains(componentOperation)) {
            try {
                throw new ibm.soatest.UnsupportedComponentOperation();
            } catch (ibm.soatest.UnsupportedComponentOperation ex) {
                logger.error("Component operation is not supported." + ex.getLocalizedMessage());
                this.getComponentOperationResult().setOverallResultSuccess(false);
                return; // this.getComponentOperationResult();
            }
        }
        
        switch (componentOperation){
            case JMS_READ_ALL_MESSAGES_IN_QUEUE:
                try {
                    readAllMessagesInQueue();
                } catch (Exception ex) {
                    logger.fatal(ex);
                }
                break;
            case JMS_CHECK_ERROR_QUEUE_FOR_MESSAGES:
                try{
                    checkErrorQueue();
                } catch (Exception ex) {
                    logger.fatal(ex);
                }
                break;
            case JMS_PURGE_QUEUE:
                try {
                    purgeQueue();
                } catch (Exception ex) {
                    logger.fatal(ex);
                }
                break;
            case JMS_VALIDATE_MESSAGE_FILES:
                try {
                    validateMessageFiles();
                } catch (Exception ex) {
                    logger.fatal(ex);
                }
                break;
            default:
                break;
                
        }
    }

    private void readAllMessagesInQueue() throws Exception {
        try {
            DistribuedQueueBrowser dqb = new DistribuedQueueBrowser(DEFAULT_PROTO + "://" + adminServer.getHost() + ":" + adminServer.getPort(),
               DEFAULT_PROTO + "://" + managedServer.getHostName() + ":" + managedServer.getPort(),
               jmsConfiguration.getServer().getName(),
               jmsConfiguration.getConnectionFactory().getName(),
               jmsConfiguration.getQueue().getName(), 
               jmsConfiguration.getQueue().getJndiName(),
               adminServer.getSecurityPrincipal(), 
               adminServer.getSecurityCredentials());
            int count = dqb.printQueueMessages();
            componentOperationResult.setOverallResultSuccess(true);
            componentOperationResult.addMsg(count + " messages were read from queue " + jmsConfiguration.getQueue().getName() + ".");
            logger.info(count + " messages were read from queue " + jmsConfiguration.getQueue().getName() + ".");            
        } catch (NoMessageFoundException e) {
            logger.error("No messages found in queue");
            componentOperationResult.setOverallResultSuccess(false);
            componentOperationResult.addMsg("No message found in queue");
        }        
    }
    
    @Override
    public Iterator<File> getGeneratedFiles(String objectName) {
        String pattern = "*";
        if (objectName != null) pattern = objectName;
        String filemask = new StringBuilder(jmsConfiguration.getQueue().getName()).append(NAME_DELIMITER).append(pattern).append(MESSAGE_SUFFIX).toString();
        return FileUtils.iterateFiles(new File(Utils.getFullFilePathStr(FileSystem.CURRENT_PATH, FileSystem.JMS_MESSAGE_DIR)), new WildcardFileFilter(filemask), TrueFileFilter.INSTANCE);
    }

    private void validateMessageFiles() {
        Iterator iterateFiles = getGeneratedFiles(null);
        while(iterateFiles.hasNext()) {
            ValidatorResult result = XMLValidator.validateXMLFile(iterateFiles.next().toString(), jmsConfiguration.getMessageSchema().getRelativeURI());
            if (!result.isValid()) {
                componentOperationResult.setOverallResultSuccess(false);
                componentOperationResult.addMsg("Message in queue " + jmsConfiguration.getQueue().getName() + " is not valid");
                logger.info("Message in queue " + jmsConfiguration.getQueue().getName() + " is not valid.");
                return;
            }
            logger.info("Message in queue " + jmsConfiguration.getQueue().getName() + " is valid.");
            componentOperationResult.setOverallResultSuccess(true);
        }
    }
    
    private void purgeQueue() throws Exception {
        PurgeQueue pq = new PurgeQueue();
        pq.deleteAllMessagesFromQueue(jmsConfiguration, osbConfiguration);
        componentOperationResult.setOverallResultSuccess(true);
        componentOperationResult.addMsg("Queue " + jmsConfiguration.getQueue().getName() + " was purged.");
        logger.info("Queue " + jmsConfiguration.getQueue().getName() + " was purged.");
    }  

    private void checkErrorQueue() throws Exception {
        DistribuedQueueBrowser dqb = new DistribuedQueueBrowser(DEFAULT_PROTO + "://" + adminServer.getHost() + ":" + adminServer.getPort(),
           DEFAULT_PROTO + "://" + managedServer.getHostName() + ":" + managedServer.getPort(),
           jmsConfiguration.getServer().getName(),
           jmsConfiguration.getConnectionFactory().getName(),
           "ErrorQueue", 
           "ErrorQueue",
           adminServer.getSecurityPrincipal(), 
           adminServer.getSecurityCredentials());

        Iterator<File> messageFilesIterator = getGeneratedFiles(null);
        while(messageFilesIterator.hasNext()) {
            String path = messageFilesIterator.next().getAbsolutePath();
            String content = FileSystem.readContentFromFile(path);
            try {
                dqb.printQueueMessagesByContent(content);
            } catch (NoMessageFoundException e) {
                logger.info("Message in queue " + jmsConfiguration.getQueue().getName() + " is not present in Error Queue."); 
                continue;
            }
            componentOperationResult.setOverallResultSuccess(false);
            componentOperationResult.addMsg("Message in queue " + jmsConfiguration.getQueue().getName() + " is present in Error Queue.");
            logger.info("Message in queue " + jmsConfiguration.getQueue().getName() + " is present in Error Queue.");
            return;
        }
        componentOperationResult.setOverallResultSuccess(true);
        componentOperationResult.addMsg("There are no messages in Error Queue related to queue " + jmsConfiguration.getQueue().getName() + ".");
        logger.info("There are no messages in Error Queue related to queue " + jmsConfiguration.getQueue().getName() + ".");        
    }
}

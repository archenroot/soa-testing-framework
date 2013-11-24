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

import com.ibm.soatf.component.CompOperType;
import com.ibm.soatf.component.ComponentResult;
import com.ibm.soatf.component.SOATFComponent;
import com.ibm.soatf.component.SOATFCompType;
import com.ibm.soatf.flow.FlowPatternCompositeKey;
import com.ibm.soatf.FrameworkConfiguration;

import com.ibm.soatf.config.iface.jms.JMSConfig;
import com.ibm.soatf.config.master.Operation;
import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance;
import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance.AdminServer;
import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance.Cluster.ManagedServer;


import com.ibm.soatf.tool.FileSystem;
import com.ibm.soatf.component.util.Utils;
import com.ibm.soatf.xml.ValidatorResult;
import com.ibm.soatf.xml.XMLValidator;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public class JMSComponent extends SOATFComponent {
    public static final String DEFAULT_PROTO = "t3";
    public static final String MESSAGE_SUFFIX = ".xml";
    public static final String NAME_DELIMITER = "_";
    
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    
    private static final Logger logger = LogManager.getLogger(JMSComponent.class.getName());
    
    public static Set<CompOperType> supportedOperations = CompOperType.JMS_OPERATIONS;   
    
    private final OracleFusionMiddlewareInstance jmsMasterConfig;
    private final JMSConfig jmsInterfaceConfig;
    
    private AdminServer adminServer;
    private ManagedServer managedServer; 
    private String queueName;
    private String workingDirectoryPath;
    private FlowPatternCompositeKey fpck;
    private static final FrameworkConfiguration FCFG = FrameworkConfiguration.getInstance();
    
    public JMSComponent(
            OracleFusionMiddlewareInstance jmsMasterConfig,
            JMSConfig jmsInterfaceConfig,
            ComponentResult componentOperationResult,
            FlowPatternCompositeKey ifaceFlowPatternCompositeKey) {
        super(SOATFCompType.JMS, componentOperationResult);
        
        this.jmsMasterConfig = jmsMasterConfig;
        this.jmsInterfaceConfig = jmsInterfaceConfig;
        this.fpck = ifaceFlowPatternCompositeKey;
        
        constructComponent();
    }
    @Override
    protected void constructComponent() {
        this.queueName = jmsInterfaceConfig.getQueue().getName();        
        this.adminServer = jmsMasterConfig.getAdminServer();
        this.managedServer = jmsMasterConfig.getCluster().getManagedServer();
        workingDirectoryPath = FCFG.SOA_TEST_HOME + "\\" +
                    fpck.getIfaceName() + "_" + FCFG.getValidFileSystemObjectName(fpck.getIfaceDesc()) +"\\" +
                    FCFG.FLOW_PATTERN_DIR_NAME_PREFIX + FCFG.getValidFileSystemObjectName(fpck.getFlowPatternId()) + "\\" +
                    FCFG.getValidFileSystemObjectName(fpck.getTestName()) + "\\" +
                    FCFG.getValidFileSystemObjectName(fpck.getTestScenarioId()) + "\\jms\\";
    }

    @Override
    public void executeOperation(Operation operation) {
        this.getComponentOperationResult().setOperation(operation);
        /*if (!supportedOperations.contains(operation.getName())) {
            try {
                throw new com.ibm.soatf.UnsupportedComponentOperationException();
            } catch (    com.ibm.soatf.UnsupportedComponentOperationException ex) {
                logger.error("Component operation is not supported." + ex.getLocalizedMessage());
                this.getComponentOperationResult().setOverallResultSuccess(false);
                return; // this.getComponentOperationResult();
            }
        }
        */
        switch (operation.getName()){
            case JMS_RECEIVE_MESSAGE_FROM_QUEUE:
                try {
                    readAllMessagesInQueue();
                } catch (Exception ex) {
                    logger.fatal(ex);
                }
                break;
            case JMS_CHECK_ERROR_QUEUE_FOR_MESSAGE:
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
            case JMS_VALIDATE_MESSAGE:
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
            DistribuedQueueBrowser dqb = new DistribuedQueueBrowser(
                    workingDirectoryPath,
                    DEFAULT_PROTO + "://" + adminServer.getHost() + ":" + adminServer.getPort(),
               DEFAULT_PROTO + "://" + managedServer.getHostName() + ":" + managedServer.getPort(),
               jmsInterfaceConfig.getJmsServer().getName(),
               jmsInterfaceConfig.getConnectionFactory().getName(),
               jmsInterfaceConfig.getQueue().getName(), 
               jmsInterfaceConfig.getQueue().getJndiName(),
               adminServer.getSecurityPrincipal(), 
               adminServer.getSecurityCredentials());
            int count = dqb.printQueueMessages();
            
            compOperResult.setOverallResultSuccess(true);
            this.getComponentOperationResult().setResultMessage("Message read from " + jmsInterfaceConfig.getQueue().getName() + " queue available at " + managedServer.getHostName() + " managed server.");
            compOperResult.addMsg(count + " messages were read from queue " + jmsInterfaceConfig.getQueue().getName() + ".");
            logger.info(count + " messages were read from queue " + jmsInterfaceConfig.getQueue().getName() + ".");            
        } catch (NoMessageFoundException e) {
            logger.error("No messages found in queue");
            compOperResult.setOverallResultSuccess(false);
            compOperResult.addMsg("No message found in queue");
        }        
    }
    
    public Iterator<File> getGeneratedFiles(String objectName) {
        String pattern = "*";
        if (objectName != null) pattern = objectName;
        String filemask = new StringBuilder(jmsInterfaceConfig.getQueue().getName()).append(NAME_DELIMITER).append(pattern).append(MESSAGE_SUFFIX).toString();
        Iterator it = FileUtils.iterateFiles(new File(Utils.getFullFilePathStr(this.workingDirectoryPath)), new WildcardFileFilter(filemask), TrueFileFilter.INSTANCE);
        return it;
    }

    private void validateMessageFiles() {
        Iterator iterateFiles = getGeneratedFiles(null);
        while(iterateFiles.hasNext()) {
            String fileToValidate = iterateFiles.next().toString();
            
            ValidatorResult result = XMLValidator.validateXMLFile(fileToValidate, jmsInterfaceConfig.getMessageSchema().getPath());
            if (!result.isValid()) {
                compOperResult.setResultMessage("Message schema validation:" 
                        + "\n message file: " + fileToValidate 
                        + "\n schema file: " +  jmsInterfaceConfig.getMessageSchema().getPath()
                        + "\n validator result: \n" + result.getValidatorMessage());
                compOperResult.setOverallResultSuccess(false);
                compOperResult.addMsg("Message in queue " + jmsInterfaceConfig.getQueue().getName() + " is not valid");
                logger.info("Message in queue " + jmsInterfaceConfig.getQueue().getName() + " is not valid.");
                return;
            }
            logger.info("Message in queue " + jmsInterfaceConfig.getQueue().getName() + " is valid.");
            compOperResult.setResultMessage("Validation executed with following files:" 
                    + "\nMessage file: " + fileToValidate 
                    + "\nXML schema file: " + jmsInterfaceConfig.getMessageSchema().getPath() 
                    + "\nValidator result: " + result.getValidatorMessage());
            compOperResult.setOverallResultSuccess(true);
        }
    }
    
    private void purgeQueue() throws Exception {
        PurgeQueue pq = new PurgeQueue();
        pq.deleteAllMessagesFromQueue(jmsInterfaceConfig, jmsMasterConfig);
         compOperResult.setResultMessage("Queue " + jmsInterfaceConfig.getQueue().getName() + " configured at " 
                 + jmsMasterConfig.getCluster().getManagedServer().getHostName() + " managed server has been purged.");
        compOperResult.setOverallResultSuccess(true);
        compOperResult.addMsg("Queue " + jmsInterfaceConfig.getQueue().getName() + " was purged.");
        logger.info("Queue " + jmsInterfaceConfig.getQueue().getName() + " was purged.");
    }  

    private void checkErrorQueue() throws Exception {
        DistribuedQueueBrowser dqb = new DistribuedQueueBrowser(workingDirectory,DEFAULT_PROTO + "://" + adminServer.getHost() + ":" + adminServer.getPort(),
           DEFAULT_PROTO + "://" + managedServer.getHostName() + ":" + managedServer.getPort(),
           jmsInterfaceConfig.getJmsServer().getName(),
           jmsInterfaceConfig.getConnectionFactory().getName(),
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
                logger.info("Message in queue " + jmsInterfaceConfig.getQueue().getName() + " is not present in Error Queue."); 
                continue;
            }
            compOperResult.setOverallResultSuccess(false);
            compOperResult.addMsg("Message in queue " + jmsInterfaceConfig.getQueue().getName() + " is present in Error Queue.");
            logger.info("Message in queue " + jmsInterfaceConfig.getQueue().getName() + " is present in Error Queue.");
            return;
        }
        compOperResult.setOverallResultSuccess(true);
        compOperResult.addMsg("There are no messages in Error Queue related to queue " + jmsInterfaceConfig.getQueue().getName() + ".");
        logger.info("There are no messages in Error Queue related to queue " + jmsInterfaceConfig.getQueue().getName() + ".");        
    }
    
    private void pauseDestionationPoduction(){
        try {
            queueName = "destiantion JNDI name";
            InitialContext ctx = getInitialContext(DEFAULT_PROTO + "://" + managedServer.getHostName() + ":" + managedServer.getPort(),
                    adminServer.getSecurityPrincipal(), 
           adminServer.getSecurityCredentials());
            //Queue queue = (Queue) ctx.lookup(queueName);
            Destination queue = (Destination) ctx.lookup("destiantion JNDI name");
            
            weblogic.management.runtime.JMSDestinationRuntimeMBean destMBean = weblogic.jms.extensions.JMSRuntimeHelper.getJMSDestinationRuntimeMBean(ctx, queue);
            destMBean.pauseProduction();
        } catch (NamingException ex) {
            java.util.logging.Logger.getLogger(JMSComponent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JMSException ex) {
            java.util.logging.Logger.getLogger(JMSComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
     private void resumeDestinationConsumption(){
        try {
            queueName = "iw.MyIrishWaterQueue";
            InitialContext ctx = getInitialContext(DEFAULT_PROTO + "://" + managedServer.getHostName() + ":" + managedServer.getPort(),
                    adminServer.getSecurityPrincipal(), 
           adminServer.getSecurityCredentials());
            //Queue queue = (Queue) ctx.lookup(queueName);
            Destination queue = (Destination) ctx.lookup(queueName);
            weblogic.management.runtime.JMSDestinationRuntimeMBean destMBean = weblogic.jms.extensions.JMSRuntimeHelper.getJMSDestinationRuntimeMBean(ctx, queue);
            destMBean.resumeProduction();
        } catch (NamingException ex) {
            java.util.logging.Logger.getLogger(JMSComponent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JMSException ex) {
            java.util.logging.Logger.getLogger(JMSComponent.class.getName()).log(Level.SEVERE, null, ex);
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
    }


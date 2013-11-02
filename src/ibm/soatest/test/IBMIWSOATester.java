/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ibm.soatest.test;

import ibm.soatest.CompOperResult;
import ibm.soatest.CompOperType;
import static ibm.soatest.CompOperType.DB_GENERATE_INSERT_ONE_ROW_RANDOM;
import static ibm.soatest.CompOperType.JMS_CHECK_ERROR_QUEUE_FOR_MESSAGES;
import static ibm.soatest.CompOperType.JMS_PURGE_QUEUE;
import static ibm.soatest.CompOperType.OSB_DISABLE_SERVICE;
import static ibm.soatest.CompOperType.OSB_ENABLE_SERVICE;
import ibm.soatest.SOATFCompFactory;
import static ibm.soatest.SOATFCompFactory.buildSOATFComponent;
import static ibm.soatest.SOATFCompType.DATABASE;
import static ibm.soatest.SOATFCompType.JMS;
import static ibm.soatest.SOATFCompType.MAPPING;
import static ibm.soatest.SOATFCompType.OSB;
import ibm.soatest.SOATFComponent;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author zANGETSu
 */
public class IBMIWSOATester {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(IBMIWSOATester.class.getName());
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        CompOperResult cor = new CompOperResult();
        boolean or = false;
        SOATFComponent c = null;
        
        /*
            Initialize filesystem structure
        */
        //ibm.soatest.tool.FileSystem.initializeFileSystemStructure(".");
        /*
            Generate INSERT statement
        */
        //c = buildSOATFComponent(DATABASE, "VendorContacts_DB", cor);
        //c.execute(DB_GENERATE_INSERT_ONE_ROW_RANDOM);
        //logger.debug("Generate db insert resulted in: " + cor.toString());
        
        /*
            SCENARIO 0 - PHAZE DB->PREQUEUE
        */
        
        /*
            Disable prequeue listner proxy
        */
        cor = new CompOperResult();
        c = SOATFCompFactory.buildSOATFComponent(OSB, "VendorContactsMaintainFromEBS_047C_prequeue_listener", cor);
        c.execute(OSB_DISABLE_SERVICE);
        logger.debug("Disable listener on prequeu resulted in: " + cor.toString());
        /*
            Purge prequeue
        */
        cor = new CompOperResult();
        c = SOATFCompFactory.buildSOATFComponent(JMS, "IWCommonJMSServer_VendorContacts_prequeue_O47C", cor);
        c.execute(JMS_PURGE_QUEUE);
        logger.debug("Step 2 purge queue resulted in: " + cor.toString());
        /*
            Execute insert statement
        */        
        cor = new CompOperResult();
        c = buildSOATFComponent(DATABASE, "VendorContacts_DB", cor);
        c.execute(CompOperType.DB_EXECUTE_INSERT_FROM_FILE);
        logger.debug("Insert statement into database resulted in: " + cor.toString());
        /*
            Delay - let the pool adapter to consume db row and send it to queue
        */
        try { Thread.sleep(15); } catch (InterruptedException ex) { logger.fatal(ex); }
        /*
            Read message in JMS queue
        */
        cor = new CompOperResult();
        c = SOATFCompFactory.buildSOATFComponent(JMS, "IWCommonJMSServer_VendorContacts_prequeue_O47C", cor);
        c.execute(CompOperType.JMS_READ_ALL_MESSAGES_IN_QUEUE);
        logger.debug("JMS read messages resulted in: " + cor.toString());
        /*
            Validate message from queue
        */
        cor = new CompOperResult();
        c = SOATFCompFactory.buildSOATFComponent(JMS, "IWCommonJMSServer_VendorContacts_prequeue_O47C", cor);
        c.execute(CompOperType.JMS_VALIDATE_MESSAGE_FILES);
        logger.debug("Step validate JMS resulted in: " + cor.toString());
        /*
            Check values DB against JMS message
        */
        cor = new CompOperResult();
        c = SOATFCompFactory.buildSOATFComponent(MAPPING,"Validate_VendorContactDB_with_queue_047C_message" , cor);
        c.execute(CompOperType.MAPPING_VALIDATE_SCENARIO);
        
        //----------------------------------------------------------------------
        
        /*
            Continue with prequeue to master queue
        */
        /*
            Disable listener on master queue
        */
        c = SOATFCompFactory.buildSOATFComponent(OSB, "VendorsSitesAndContactsPS_masterqueue_listener", cor);
        c.execute(OSB_DISABLE_SERVICE);
        
        /*
            Purge queue
        */
        c = SOATFCompFactory.buildSOATFComponent(JMS, "IWCommonJMSServer_master_queue_047C", cor);
        c.execute(CompOperType.JMS_PURGE_QUEUE);
        logger.debug("Step 2 purge queue resulted in: " + or); 
        
        /*
            Enable listener on pre queue
        */
        c = SOATFCompFactory.buildSOATFComponent(OSB, "VendorContactsMaintainFromEBS_047C_prequeue_listener", cor);
        c.execute(OSB_ENABLE_SERVICE);
        /*
            Delay to let the message be populated.
        */
        
        /*
            Read message in JMS queue
        */
        cor = new CompOperResult();
        c = SOATFCompFactory.buildSOATFComponent(JMS, "IWCommonJMSServer_master_queue_047C", cor);
        c.execute(CompOperType.JMS_READ_ALL_MESSAGES_IN_QUEUE);
        logger.debug("JMS read messages resulted in: " + cor.toString());
        
        /*
            Validate message from queue
        */
        cor = new CompOperResult();
        c = SOATFCompFactory.buildSOATFComponent(JMS, "IWCommonJMSServer_master_queue_047C", cor);
        c.execute(CompOperType.JMS_VALIDATE_MESSAGE_FILES);
        logger.debug("Step validate JMS resulted in: " + cor.toString());
        /*
            Map values between messages
        */
        c = SOATFCompFactory.buildSOATFComponent(MAPPING, "Validate_047C_with_master_queue", cor);
        c.execute(CompOperType.MAPPING_VALIDATE_SCENARIO); 
        
        
        
        /*
            Scenario 3 - queue -> endpoint
        */
        
        // Step 1 - enable listener on maintain queue
        c = SOATFCompFactory.buildSOATFComponent(OSB, "VendorsSitesAndContactsPS_masterqueue_listener", cor);
        c.execute(OSB_ENABLE_SERVICE);
        
        // Step 2 - check error queue
        c = SOATFCompFactory.buildSOATFComponent(JMS, "IWCommonJMSServer_master_queue_047C", cor);
         c.execute(JMS_CHECK_ERROR_QUEUE_FOR_MESSAGES);    
    }
    
}

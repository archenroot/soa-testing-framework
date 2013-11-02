/*
 * Copyright (C) 2013 Ladislav Jech <archenroot@gmail.com>
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
package ibm.soatest;

import java.io.FileNotFoundException;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import static ibm.soatest.CompOperType.JMS_READ_ALL_MESSAGES_IN_QUEUE;
import static ibm.soatest.CompOperType.JMS_PURGE_QUEUE;
import static ibm.soatest.CompOperType.OSB_DISABLE_SERVICE;
import static ibm.soatest.CompOperType.OSB_ENABLE_SERVICE;
import static ibm.soatest.CompOperType.MAPPING_VALIDATE_SCENARIO;
import static ibm.soatest.CompOperType.JMS_VALIDATE_MESSAGE_FILES;
import static ibm.soatest.CompOperType.JMS_CHECK_ERROR_QUEUE_FOR_MESSAGES;
import static ibm.soatest.SOATFCompType.JMS;
import static ibm.soatest.SOATFCompType.DATABASE;
import static ibm.soatest.SOATFCompType.OSB;
import static ibm.soatest.SOATFCompType.MAPPING;
import ibm.soatest.database.DatabaseComponent;
import ibm.soatest.jms.JMSComponent;
import ibm.soatest.mapping.MappingComponent;
import ibm.soatest.osb.OSBComponent;
import ibm.soatest.xml.XMLComponent;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gibello.zql.ParseException;
import org.xml.sax.SAXException;


/**
 *
 * @author Ladislav Jech <archenroot@gmail.com>
 */
public class TestJMSComponent {

    public static void testJmsComponent() throws SAXException, ParserConfigurationException, IOException, FileNotFoundException, ParseException {
        CompOperResult cor = new CompOperResult();

        //STEP 1
        OSBComponent mainQueuePoller = (OSBComponent) SOATFCompFactory.buildSOATFComponent(OSB, "test_service", cor);
        mainQueuePoller.execute(OSB_DISABLE_SERVICE);
        
        //STEP 2
        OSBComponent preQueuePoller = (OSBComponent) SOATFCompFactory.buildSOATFComponent(OSB, "preQueuePoller", cor);
        preQueuePoller.executeOperation(OSB_DISABLE_SERVICE);

        //STEP 3
        JMSComponent preQueue = (JMSComponent) SOATFCompFactory.buildSOATFComponent(JMS, "demo_local_jms_server", cor);
        preQueue.execute(JMS_PURGE_QUEUE);
        
        //STEP 4
        DatabaseComponent databaseComponent = (DatabaseComponent)SOATFCompFactory.buildSOATFComponent(DATABASE, "Dummy Test", cor);
        databaseComponent.execute(CompOperType.DB_GENERATE_INSERT_ONE_ROW_RANDOM);
        //STEP 5
        databaseComponent.execute(CompOperType.DB_EXECUTE_INSERT_FROM_FILE);   
        
        //STEP 6
        try {
            Thread.sleep(15000);
        } catch (InterruptedException ex) {
            Logger.getLogger(TestOSBComponent.class.getName()).log(Level.SEVERE, null, ex);
        }        
        
        //STEP 7
        preQueue.execute(JMS_READ_ALL_MESSAGES_IN_QUEUE);
        //STEP 8
        preQueue.execute(JMS_VALIDATE_MESSAGE_FILES);
        
        //STEP 9
        MappingComponent mappingComponent = (MappingComponent)SOATFCompFactory.buildSOATFComponent(MAPPING, "scenario1", cor);
        mappingComponent.execute(MAPPING_VALIDATE_SCENARIO);
        
        //STEP 10
        //OSBComponent mainQueuePoller = (OSBComponent) SOATFCompFactory.buildSOATFComponent(OSB, "test_service", cor);
        JMSComponent mainQueue = (JMSComponent) SOATFCompFactory.buildSOATFComponent(JMS, "vendorsSitesAndContactsQueue", cor);
        mainQueue.execute(JMS_PURGE_QUEUE);
        
        //STEP 11
        preQueuePoller.execute(OSB_ENABLE_SERVICE);
        //STEP 12
        try {
            Thread.sleep(15000);
        } catch (InterruptedException ex) {
            Logger.getLogger(TestOSBComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //STEP 13
        preQueue.execute(JMS_CHECK_ERROR_QUEUE_FOR_MESSAGES);
        
        //STEP 14
        mainQueue.execute(JMS_READ_ALL_MESSAGES_IN_QUEUE);
        //STEP 15
        mainQueue.execute(JMS_VALIDATE_MESSAGE_FILES);    
        
        //STEP 16
        MappingComponent mapping2 = (MappingComponent)SOATFCompFactory.buildSOATFComponent(MAPPING, "preQueueToQueue", cor);
        mapping2.execute(MAPPING_VALIDATE_SCENARIO);  
        
        //STEP 17
        mainQueuePoller.execute(OSB_ENABLE_SERVICE);  
        //STEP 18
        try {
            Thread.sleep(20000);
        } catch (InterruptedException ex) {
            Logger.getLogger(TestOSBComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //STEP 19
        mainQueue.execute(JMS_CHECK_ERROR_QUEUE_FOR_MESSAGES);        
        
        
    }
}

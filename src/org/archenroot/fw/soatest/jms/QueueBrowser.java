/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archenroot.fw.soatest.jms;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import javax.help.Map;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.NamingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.archenroot.fw.soatest.database.DatabaseComponent;

/**
 *
 * @author zANGETSu
 */
public class QueueBrowser {

    Logger logger = LogManager.getLogger(QueueBrowser.class.getName());
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
    
    public void printQueueMessages() {
        try {
            // Note that the first argument is the admin url and the second is the
            // managed server url.
            DistribuedQueueBrowser qb = new DistribuedQueueBrowser("t3://prometheus:7001",
                    "t3://prometheus:11001",
                    "OSBWriteQueue", "weblogic", "Weblogic123");
            
          /*  Enumeration<Message> i = qb.getEnumeration();
            while (i.hasMoreElements()) {
                
                Message m = i.nextElement();
                System.out.println("Message:" + m);
                
            }
            */
            Enumeration<ServerLocatedMessage> sli = qb.getServerLocatedEnumeration();
            int i = 0;
            while (sli.hasMoreElements()) {
                i++;
                ServerLocatedMessage m = sli.nextElement();
                TextMessage mes = (TextMessage) m.getMessage();
                for (Enumeration<String> e = mes.getPropertyNames(); e.hasMoreElements();)
                        System.out.println("enum" + e.nextElement().toString());
                
                Map map = (Map) mes;
                
                System.out.println("Correlation id:" + mes.getJMSCorrelationID());
                System.out.println("Message id:" + mes.getJMSCorrelationID());
                System.out.println("JMS Type:" + mes.getJMSType());
                System.out.println("Content: " + mes.getText());
                
                this.writeStatementToFile(mes.getText(), "test/jms/message_" + i + ".xml");
                System.out.println(m);
            }
        } catch (JMSException ex) {
            java.util.logging.Logger.getLogger(QueueBrowser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NamingException ex) {
            java.util.logging.Logger.getLogger(QueueBrowser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(QueueBrowser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archenroot.fw.soatest.jmsserver;

import java.util.Enumeration;
import java.util.logging.Level;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.NamingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    public void printQueueMessages() {
        try {
            // Note that the first argument is the admin url and the second is the
            // managed server url.
            DistribuedQueueBrowser qb = new DistribuedQueueBrowser("t3://prometheus:7001",
                    "t3://prometheus:11001",
                    "OSBListenQueue", "weblogic", "Weblogic123");
            
          /*  Enumeration<Message> i = qb.getEnumeration();
            while (i.hasMoreElements()) {
                
                Message m = i.nextElement();
                System.out.println("Message:" + m);
                
            }
            */
            Enumeration<ServerLocatedMessage> sli = qb.getServerLocatedEnumeration();
            
            while (sli.hasMoreElements()) {
                ServerLocatedMessage m = sli.nextElement();
                
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

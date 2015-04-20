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


import com.ibm.soatf.config.iface.jms.JMSConfig;
import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance;
import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance.Clusters.Cluster.ManagedServer;
import com.ibm.soatf.gui.ProgressMonitor;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public class PurgeQueue {

    private static final Logger logger = LogManager.getLogger(PurgeQueue.class.getName());
    
    private Queue queue;
    private ConnectionFactory queueConnFactory;
    private Connection queueConn;
    private Session queueSession;
    private QueueBrowser queueBrowser;
    private String connectionFactory;

    public static final String WEBLOGIC_JMS_XA_CONNECTION_FACTORY = "weblogic/jms/XAConnectionFactory";

    PurgeQueue(){
    }
    
    public static Context createInitialContext(String hostname, int port, String login, String pass) throws NamingException {
        Hashtable<String, String> ht = new Hashtable<>();
        ht.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
        ht.put(Context.PROVIDER_URL,
                "t3://"
                + hostname
                + ":"
                + port
        );
        ht.put(Context.SECURITY_PRINCIPAL, login);
        ht.put(Context.SECURITY_CREDENTIALS, pass);
        Context ctx = new InitialContext(ht);
        return ctx;
    }

    public void init(JMSConfig jmsConfig, OracleFusionMiddlewareInstance osbConfig, ManagedServer managedServer) throws JmsComponentException {
        try {
            // get the initial context
            Context ctx = createInitialContext(managedServer.getHostName(), managedServer.getPort(), osbConfig.getAdminServer().getSecurityPrincipal(), osbConfig.getAdminServer().getSecurityCredentials());
            // lookup the queue object
            queue = (Queue) ctx.lookup(jmsConfig.getQueue().getJndiName());

            // lookup the queue connection factory
            queueConnFactory = (ConnectionFactory) ctx.lookup(jmsConfig.getConnectionFactory().getName());
            // create a queue connection
            queueConn = queueConnFactory.createConnection();
            // start the connection
            queueConn.start();
            // create a queue session
            queueSession = queueConn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            // create a queue browser
            queueBrowser = queueSession.createBrowser(queue);
        } catch (NamingException | JMSException ex) {
            throw new JmsComponentException(ex);
        } 
    }

    public List<Message> deleteAllMessagesFromQueue(JMSConfig jmsConfig, OracleFusionMiddlewareInstance osbConfig, ManagedServer managedServer) throws JmsComponentException {
        MessageConsumer consumer = null;
        List<Message> messages = new ArrayList<>();
        try {
            ProgressMonitor.init(2, "Connecting to queue...");
            init(jmsConfig, osbConfig, managedServer);
            consumer = queueSession.createConsumer(queue);
            Message message = null;

            ProgressMonitor.increment("Removing messages...");
            do {
                message = consumer.receiveNoWait();
                if (message != null) {
                    message.acknowledge();
                    messages.add(message);
                }
            } while (message != null);

            consumer.close();

        } catch (JMSException ex) {
            throw new JmsComponentException(ex);
        } finally{
            try {            
                if (consumer != null) {
                    consumer.close();
                }
                if (queueConn != null) {
                    queueConn.close();
                }
            } catch (JMSException ex) {
                ;
            }            
        }
        return messages;
    }
}

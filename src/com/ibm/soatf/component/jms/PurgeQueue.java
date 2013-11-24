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
import java.util.Hashtable;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;


/**
 *
 * @author zANGETSu
 */
public class PurgeQueue {
    
    private Queue queue;
    private ConnectionFactory queueConnFactory;
    private Connection queueConn;
    private Session queueSession;
    private QueueBrowser queueBrowser;
    private String connectionFactory;
    
    
    public static final String WEBLOGIC_JMS_XA_CONNECTION_FACTORY = "weblogic/jms/XAConnectionFactory";


    
 public static Context createInitialContext(OracleFusionMiddlewareInstance osbConfig) throws NamingException {
  Hashtable<String, String> ht = new Hashtable<String, String>();
  ht.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
  ht.put(Context.PROVIDER_URL, 
          "t3://" + 
          osbConfig.getCluster().getManagedServer().getHostName() +
          ":" +
          osbConfig.getCluster().getManagedServer().getPort()
  );
  ht.put(Context.SECURITY_PRINCIPAL, osbConfig.getAdminServer().getSecurityPrincipal());
  ht.put(Context.SECURITY_CREDENTIALS, osbConfig.getAdminServer().getSecurityCredentials());
  Context ctx = new InitialContext(ht);
  return ctx;
 }


 public void init(JMSConfig jmsConfig, OracleFusionMiddlewareInstance osbConfig) throws Exception {
  // get the initial context
  Context ctx = createInitialContext(osbConfig);
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
 }


 public void deleteAllMessagesFromQueue(JMSConfig jmsConfig, OracleFusionMiddlewareInstance osbConfig) throws Exception {
    init(jmsConfig, osbConfig);
    MessageConsumer consumer = queueSession.createConsumer(queue);
    Message message = null;
    do {
        message = consumer.receiveNoWait();
        if (message != null) message.acknowledge();
    } 
    while (message != null);
  
    consumer.close();
    queueConn.close();
 }
}

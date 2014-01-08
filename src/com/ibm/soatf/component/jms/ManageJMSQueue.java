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

import javax.management.*;
import java.io.*;
import java.util.*;
import java.rmi.*;
import javax.naming.*;

public class ManageJMSQueue {

    private MBeanServerConnection server = null;

    public ManageJMSQueue() {
        try {
            Hashtable<String, String> ht = new Hashtable<String, String>();
            ht.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.security.jndi.JndiLoginInitialContextFactory");
            ht.put(Context.PROVIDER_URL, "localhost:1099");
            ht.put(Context.SECURITY_PRINCIPAL, "admin");
            ht.put(Context.SECURITY_CREDENTIALS, "admin");
            System.out.println("nt 1- Gotting InitialContext...... ");
            Context ctx = new InitialContext(ht);
            System.out.println("nt 2- Got InitialContext: " + ctx);
            server = (MBeanServerConnection) ctx.lookup("jmx/invoker/RMIAdaptor");
        } catch (Exception e) {
            System.out.println("nnt Exception inside ManageJMSQueue..." + e);
        }
    }

    public void monitorJMS() throws Exception {
        ObjectName objectName = new ObjectName("jboss.messaging.destination:name=DLQ,service=Queue");
        System.out.println("nnServerPeer = " + (javax.management.ObjectName) server.getAttribute(objectName, new String("ServerPeer")));
        System.out.println("QueueName = " + (String) server.getAttribute(new ObjectName("jboss.messaging.destination:name=DLQ,service=Queue"), new String("Name")));
        System.out.println("JNDI Name = " + (String) server.getAttribute(new ObjectName("jboss.messaging.destination:name=DLQ,service=Queue"), new String("JNDIName")));
        System.out.println("FullSize = " + (Integer) server.getAttribute(new ObjectName("jboss.messaging.destination:name=DLQ,service=Queue"), new String("FullSize")));
    }

    public void listAllJMS_Messages() throws Exception {
        ObjectName objectName = new ObjectName("jboss.messaging.destination:name=DLQ,service=Queue");
        //List<org.jboss.jms.message.JBossTextMessage> messages=(List<org.jboss.jms.message.JBossTextMessage>)server.invoke(objectName, "listAllMessages" , null, null);
        int count = 0;
        //for(org.jboss.jms.message.JBossTextMessage msg : messages)
        //System.out.println((++count)+"t"+msg.getText());
    }

    public void removeAllJMS_Messages() throws Exception {
        String queueName = (String) server.getAttribute(new ObjectName("jboss.messaging.destination:name=DLQ,service=Queue"), new String("Name"));
        System.out.println("nt Removing all JMS Messages from Queue: " + queueName);
        server.invoke(new ObjectName("jboss.messaging.destination:name=DLQ,service=Queue"), "removeAllMessages", null, null);
        System.out.println("nt All the Messages are removed from JMS Queue: " + queueName);
    }

    public void removeAllJMSMessages(
            MBeanServerConnection mbeanServerConnection)
            throws MalformedObjectNameException, MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, IOException {
        ObjectName service = new ObjectName("com.bea:Name=RuntimeService,Type=weblogic.management.mbeanservers.runtime.RuntimeServiceMBean");
        ObjectName serverRuntime = (ObjectName) mbeanServerConnection.getAttribute(service, "ServerRuntime");
        ObjectName jmsRuntime = (ObjectName) mbeanServerConnection.getAttribute(serverRuntime, "JMSRuntime");
        ObjectName[] jmsServers = (ObjectName[]) mbeanServerConnection.getAttribute(jmsRuntime, "JMSServers");
        for (ObjectName jmsServer : jmsServers) {
            /*
             if (JMS_SERVER.equals(jmsServer.getKeyProperty("Name"))) {
             ObjectName[] destinations = (ObjectName[]) mbeanServerConnection.getAttribute(jmsServer, "Destinations");
             for (ObjectName destination: destinations) {
             if (destination.getKeyProperty("Name").endsWith("!"+JMS_DESTINATION)) {
             Object o = mbeanServerConnection.invoke(
             destination,
             "deleteMessages",
             new Object[] {""},        // selector expression
             new String[] {"java.lang.String"});
             System.out.println("Result: "+o);
             break;
             }
             */
        }
    }

    public static void main(String ar[]) throws Exception {
        ManageJMSQueue ref = new ManageJMSQueue();
        ref.monitorJMS();
        System.out.println("nt Following Messages Are present inside the JMS Queue:");
        ref.listAllJMS_Messages();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("nn Please Specify (yes/no) to delete all the messages from JMS Queue ? ");
        String answer = "";
        if ((answer = br.readLine()).equals("yes")) {
            ref.removeAllJMS_Messages();
        }
        br.close();
    }
}

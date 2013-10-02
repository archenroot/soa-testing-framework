package com.ibm.fm.soatest.jms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;

import org.apache.commons.lang3.StringUtils;

public class WeblogicMBeanHelper {
  private final MBeanServerConnection connection;
  private final JMXConnector connector;
  private final ObjectName service;

  public WeblogicMBeanHelper(String url, String userName, String password) {
    try {
      service = new ObjectName(
          "com.bea:Name=DomainRuntimeService,Type=weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean");
    }
    catch (MalformedObjectNameException e) {
      throw new AssertionError(e.getMessage());
    }

    String[] urls = url.split(",");
    if (urls.length == 0) {
      throw new RuntimeException("Invalid URL Provided");
    }

    String hostNamePort[] = StringUtils.split(urls[0], ":");

    String hostName = hostNamePort.length == 3
        ? hostNamePort[1]
        : hostNamePort[0];
    hostName = StringUtils.strip(hostName, "/");
    String port = hostNamePort.length == 3
        ? hostNamePort[2]
        : hostNamePort[1];

    String jndiroot = "/jndi/";
    String mserver = "weblogic.management.mbeanservers.domainruntime";
    JMXServiceURL serviceURL;

    try {
      serviceURL = new JMXServiceURL("t3", hostName, Integer.valueOf(port), jndiroot + mserver);

      Hashtable<String, String> h = new Hashtable<String, String>();
      h.put(Context.SECURITY_PRINCIPAL, userName);
      h.put(Context.SECURITY_CREDENTIALS, password);
      h.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, "weblogic.management.remote");
      connector = JMXConnectorFactory.connect(serviceURL, h);
      connection = connector.getMBeanServerConnection();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public Iterable<String> getDistributedMemberJndiNames(String distributedDestJndiName) {
    Iterable<String> serverNames = getJmsServerNames();
    Set<String> distributedDestNames = new TreeSet<String>();

    for (String serverName : serverNames) {
      distributedDestNames.add(serverName + "@" + distributedDestJndiName);
    }

    return distributedDestNames;
  }

  public Iterable<String> getJmsServerNames() {
    Set<String> jmsServerNames = new TreeSet<String>();
    Iterable<ObjectName> jmsServers = getJMSServers();

    for (ObjectName jmsServer : jmsServers) {
      try {
        jmsServerNames.add((String) connection.getAttribute(jmsServer, "Name"));
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    return jmsServerNames;
  }

  public Iterable<ObjectName> getJMSServers() {

    Iterable<ObjectName> jmsRuntimes = getJMSRuntimes();
    List<ObjectName> jmsServers = new ArrayList<ObjectName>();

    for (ObjectName jmsRuntime : jmsRuntimes) {
      try {
        ObjectName jmsServerArr[] = (ObjectName[]) connection
            .getAttribute(jmsRuntime, "JMSServers");
        for (int i = 0; i < jmsServerArr.length; i++) {
          jmsServers.add(jmsServerArr[i]);
        }
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    return jmsServers;
  }

  public Iterable<ObjectName> getJMSRuntimes() {
    Iterable<ObjectName> serverRuntimes = getServerRuntimeMBeans();
    List<ObjectName> jmsRuntimes = new ArrayList<ObjectName>();

    for (ObjectName serverRuntime : serverRuntimes) {
      try {
        jmsRuntimes.add((ObjectName) connection.getAttribute(serverRuntime, "JMSRuntime"));
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    return jmsRuntimes;
  }

  public List<ObjectName> getServerRuntimeMBeans() {
    try {
      return Arrays.asList((ObjectName[]) connection.getAttribute(service, "ServerRuntimes"));
    }
    catch (Exception e) {
      throw new RuntimeException("Error obtaining Server Runtime Information", e);
    }
  }

  public void close() {
    try {
      connector.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }
}

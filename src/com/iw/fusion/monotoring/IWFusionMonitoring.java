/**
 * 
 */
package com.iw.fusion.monotoring;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;

/**
 * Program to monitor SOA.
 * @author ibmlocal
 * 
 */
public class IWFusionMonitoring {

	private static MBeanServerConnection connection;
	private static MBeanServerConnection serverRuntimeconnection;
	private static JMXConnector connector;
//	private static JMXConnector serverRuntimeconnector;

	public static void Connection(String hostname, String port)
			throws IOException {
		 Integer portInteger = Integer.valueOf(port);
		 HashMap<String, String> h;
         h = new HashMap<String, String>();
         h.put(Context.SECURITY_PRINCIPAL, "weblogic");
         h.put(Context.SECURITY_CREDENTIALS, "passw0rd1");
         h.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, "weblogic.management.remote");
         
         JMXServiceURL address = new JMXServiceURL(
					"service:jmx:iiop://"+hostname+":"+portInteger+"/jndi/weblogic.management.mbeanservers.domainruntime");         
		connector = JMXConnectorFactory.connect(address, h);		
		connection = connector.getMBeanServerConnection();
		
		
		//JMXServiceURL serverRuntimeAddress = new JMXServiceURL(
		//		"service:jmx:iiop://10.19.12.82:7001/jndi/weblogic.management.mbeanservers.runtime");
		//serverRuntimeconnector = JMXConnectorFactory.connect(serverRuntimeAddress, h);
		//serverRuntimeconnection = serverRuntimeconnector.getMBeanServerConnection();
		System.out.println("GOT THE MBeanServerConnection--SUCCESSFULLY");
	}

	
	
	public static void getThreadPoolRuntimeData()
			throws Exception {
		
		ObjectName threadPoolRuntimeMBean = 
				
				new ObjectName("com.bea:Name=DomainRuntimeService,Type=weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean");
		
		ObjectName[] serverRunTimes =(ObjectName[]) connection.getAttribute(
				threadPoolRuntimeMBean, "ServerRuntimes");
		
		
		
		
		for (int i = 0; i < serverRunTimes.length; i++) {
			String serverName=(String)connection.getAttribute(serverRunTimes[i], "Name");
			//System.out.println(serverName);
			System.out.println("\t ************************"+serverName+"**************************************");
			ObjectName jRockitRuntime = new ObjectName("com.bea:ServerRuntime="+serverName+",Name="+serverName+",Location="+serverName+",Type=JRockitRuntime");
			Long usedPhysicalMemory = (Long) connection.getAttribute(
					jRockitRuntime, "UsedPhysicalMemory");
			Long freePhysicalMemory = (Long) connection.getAttribute(
					jRockitRuntime, "FreePhysicalMemory");
			Long totalPhysicalMemory = (Long) connection.getAttribute(
					jRockitRuntime, "TotalPhysicalMemory");
			Long totalHeap = (Long) connection.getAttribute(jRockitRuntime,
					"TotalHeap");
			Long freeHeap = (Long) connection.getAttribute(jRockitRuntime,
					"FreeHeap");
			Long usedHeap = (Long) connection.getAttribute(jRockitRuntime,
					"UsedHeap");
			
			int numberOfDaemonThreads =  (Integer)connection.getAttribute(jRockitRuntime,
					"NumberOfDaemonThreads");
			int heapFreePercent= (Integer)connection.getAttribute(jRockitRuntime,
						"HeapFreePercent");
			Long heapSizeCurrent = (Long) connection.getAttribute(jRockitRuntime,
					"HeapSizeCurrent");
			
			int totalNumberOfThreads= (Integer)connection.getAttribute(jRockitRuntime,
					"TotalNumberOfThreads");
			
			System.out.println("\t Server JRockitRuntime");
			System.out
					.println("\n\n\t UsedPhysicalMemory  : " + usedPhysicalMemory);
			System.out.println("\t FreePhysicalMemory  : " + freePhysicalMemory);
			System.out.println("\t TotalPhysicalMemory : " + totalPhysicalMemory);
			System.out.println("\t TotalHeap           : " + totalHeap);
			System.out.println("\t FreeHeap            : " + freeHeap);
			System.out.println("\t UsedHeap            : " + usedHeap);
			System.out.println("\t NumberOfDaemonThreads            : " + numberOfDaemonThreads);
			System.out.println("\t HeapFreePercent            : " + heapFreePercent);
			System.out.println("\t HeapSizeCurrent            : " + heapSizeCurrent);
			System.out.println("\t TotalNumberOfThreads            : " + totalNumberOfThreads);
			System.out.println("\t **************************************************************");
			
			
			//***ThreadPoolRuntime**//
			System.out.println("\t Server ThreadPoolRuntime");
			ObjectName threadPoolRuntime = (ObjectName)connection.getAttribute(serverRunTimes[i], "ThreadPoolRuntime");
			int hoggingThreadCount= (Integer)connection.getAttribute(threadPoolRuntime, "HoggingThreadCount");
			
			System.out.println("\t HoggingThreadCount : " + hoggingThreadCount);
			System.out.println("\t Suspended : " + connection.getAttribute(threadPoolRuntime, "Suspended"));
			System.out.println("\t QueueLength : " + connection.getAttribute(threadPoolRuntime, "QueueLength"));
			System.out.println("\t ThroughPut : " + connection.getAttribute(threadPoolRuntime, "Throughput"));
			System.out.println("\t ExecuteThreadIdleCount : " + connection.getAttribute(threadPoolRuntime, "ExecuteThreadIdleCount"));
			//***State**//
			System.out.println("\t **************************************************************");
			System.out.println("\t Server Health");
			ObjectName ser = new ObjectName("com.bea:Name=" + serverName
					+ ",Location=" + serverName + ",Type=ServerRuntime");
			String serverState = (String) connection.getAttribute(ser,
					"State");
			System.out.println("\n\t Server: " + serverName + "\t State: "
					+ serverState);
			weblogic.health.HealthState serverHealthState = (weblogic.health.HealthState) connection
					.getAttribute(ser, "HealthState");
			int hState = serverHealthState.getState();
			if (hState == weblogic.health.HealthState.HEALTH_OK)
				System.out.println("\t Server: " + serverName
						+ "\t State Health: HEALTH_OK");
			if (hState == weblogic.health.HealthState.HEALTH_WARN)
				System.out.println("\t Server: " + serverName
						+ "\t State Health: HEALTH_WARN");
			if (hState == weblogic.health.HealthState.HEALTH_CRITICAL)
				System.out.println("\t Server: " + serverName
						+ "\t State Health: HEALTH_CRITICAL");
			if (hState == weblogic.health.HealthState.HEALTH_FAILED)
				System.out.println("\t Server: " + serverName
						+ "\t State Health: HEALTH_FAILED");
			if (hState == weblogic.health.HealthState.HEALTH_OVERLOADED)
				System.out.println("\t Server: " + serverName
						+ "\t State Health: HEALTH_OVERLOADED");
		}
		
	
	}

	public static void takeThreadDump(String serverName) throws Exception {
		ObjectName jRockitRuntime = new ObjectName("com.bea:ServerRuntime="
				+ serverName + ",Name=" + serverName + ",Type=JRockitRuntime");
		String dumpStack = (String) serverRuntimeconnection.getAttribute(jRockitRuntime,
				"ThreadStackDump");
		System.out
				.println("\n\n\t --------------FULL THREAD_DUMP--------------\n: "
						+ dumpStack);
	}
	
	
	public static void main(String[] args) throws Exception {
		String hostname = "10.19.12.82";
		String port = "7001";
		Connection(hostname, port);
		
		getThreadPoolRuntimeData();
		//takeThreadDump("AdminServer");
		connector.close();
	}
}
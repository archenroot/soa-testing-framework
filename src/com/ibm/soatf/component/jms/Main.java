/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.soatf.component.jms;

import com.ibm.soatf.component.soap.builder.XmlUtils;
import com.ibm.soatf.xml.XMLValidator;
import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;

/**
 *
 * @author zANGETSu
 */
public class Main {

    @SuppressWarnings("empty-statement")
    public static void main(String[] args) throws Exception {
        final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        factory.setResourceResolver(new XMLValidator.MySchemaResolver());        
        File schemaFile = new File("../osb/CrewOperationsConnectorMaximo/Resources/Schemas/BGICREWService.xsd");
        final InputStream is = XmlUtils.fixSchemaFile(schemaFile);        
        final Source schemaSource = new StreamSource(is);
        schemaSource.setSystemId("../osb/CrewOperationsConnectorMaximo/Resources/Schemas/BGICREWService.xsd");
        Schema schema = factory.newSchema(schemaSource);
        /*
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setSchema(schema);
        dbf.setValidating(false);
        dbf.setNamespaceAware(true);
        DocumentBuilder parser = dbf.newDocumentBuilder();

        Document document = parser.parse(new File("C:\\Irish Water\\repos_int1\\m2\\trunk\\soa_test\\IW.720.MAX_CLK_Crews\\FlowPattern_-_SOAPToQueueToSOAP\\TestCaseScenarioForCrewOperationsOfTypeMAXCLK001\\PositiveScenario1SourceToDestination\\jms\\bge.CrewOperations.MaintainQueue_441383.1391776859937.0_0.xml"));
        */
        Validator validator = schema.newValidator();
        
        if (true) return;
        
        /*XmlObject xmlObject = XmlObject.Factory.parse(schemaFile);
        XmlObject[] objs = xmlObject.selectPath("$this//*[local-name()=\"restriction\"]");
        System.out.println(objs.length);*/
        
        System.out.println("JMSQueueBrowser executed...");
        String hostName = "prometheus";
        int port = 7001;
        String userName = "weblogic";
        String password = "Weblogic123";
        String messageBeanServer = "weblogic.management.mbeanservers.domainruntime";
        ObjectName service = new ObjectName(
                "com.bea:Name=DomainRuntimeService,Type=weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean");
        JMXConnector jmxConnector = new JMXConnector(hostName, port, userName, password, messageBeanServer);
        MBeanServerConnection mbServerConn = jmxConnector.getConnection();
        
        System.out.println("Default domain: " + mbServerConn.getDefaultDomain());
        
        // Working with beans
        BeanProvider bp = new BeanProvider(mbServerConn, service);
        Hashtable<String,String> serviceProperties = bp.getProperties();
        Enumeration<String> elements = serviceProperties.elements();
       
        System.out.println("Going trought properties elements of the service.");
        while (elements.hasMoreElements()){
            System.out.println(elements.nextElement());
        }
        
        Enumeration<String> keys = serviceProperties.keys();
        System.out.println("Going trought properties keys of the service.");
        while (keys.hasMoreElements()){
            System.out.println(keys.nextElement());
        }
        
        
       System.out.println("KeyPropertyListString: " + service.getKeyPropertyListString());
       
       //Server runtime mbeans
       List<ObjectName> on = bp.getServerRuntimeMBeans();
       ListIterator<ObjectName> lion = on.listIterator();
       System.out.println("Iterate trough server runtime mbeans:");
       while (lion.hasNext()){
           System.out.println( "runtime: " + lion.next() );
       }
       // JMS Server Names
       Iterator jmsServerNames = bp.getJmsServerNames().iterator();
       while (jmsServerNames.hasNext()){System.out.println("JMSServerName: " + jmsServerNames.next());}
       
       // JMS runtimes
       Iterator jmsRuntimes = bp.getJMSRuntimes().iterator();
       while (jmsRuntimes.hasNext()){System.out.println("JMSRuntimes: " + jmsRuntimes.next());}
       
       // distributed member jndi names
       Iterator distributedMemberJndiNames = bp.getDistributedMemberJndiNames("a").iterator();
       while(distributedMemberJndiNames.hasNext()){System.out.println("DistributedMemberJNDINames: " + distributedMemberJndiNames.next());}
        
       Iterator jmsConnectedHosts = bp.getJMSConnectedHosts().iterator();
       while(jmsConnectedHosts.hasNext()){System.out.println("JMSConnectedHosts: " + jmsConnectedHosts.next());}
     
      
    }
}

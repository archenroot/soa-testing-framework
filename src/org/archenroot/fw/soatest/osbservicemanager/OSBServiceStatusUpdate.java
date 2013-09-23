/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archenroot.fw.soatest.osbservicemanager;


import com.bea.wli.config.Ref;
import com.bea.wli.sb.management.configuration.ALSBConfigurationMBean;
import com.bea.wli.sb.management.configuration.BusinessServiceConfigurationMBean;
import com.bea.wli.sb.management.configuration.ProxyServiceConfigurationMBean;
import com.bea.wli.sb.management.configuration.SessionManagementMBean;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import weblogic.management.jmx.MBeanServerInvocationHandler;
import weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean;

public class OSBServiceStatusUpdate {
    /*
    private static JMXConnector initConnection(String hostname, int port,String username, String password) throws IOException,MalformedURLException
    {
        JMXServiceURL serviceURL = new JMXServiceURL("t3", hostname, port,"/jndi/" + DomainRuntimeServiceMBean.MBEANSERVER_JNDI_NAME);
        HashMap<String, String> h = new HashMap<String, String>();
        h.put(Context.SECURITY_PRINCIPAL, username);
        h.put(Context.SECURITY_CREDENTIALS, password);
        h.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, "weblogic.management.remote");
        return JMXConnectorFactory.connect(serviceURL, h);
    }

    private static Ref constructRef(String refType,String serviceuri){
        Ref ref = null;
        String[] uriData = serviceuri.split("/");
        ref = new Ref(refType,uriData);       
        return ref;
    }
//weblogic.management.mbeanservers.runtime.RuntimeServiceMBean
    public static String changeProxyServiceStatus(String servicetype,boolean status,String serviceURI,String host,int port,String username,String password){
        JMXConnector conn = null;
        SessionManagementMBean sm = null;
        String sessionName = "mysession";
        String statusmsg="";
        try{
            conn = initConnection(host, port, username,password);
            MBeanServerConnection mbconn = conn.getMBeanServerConnection();
            DomainRuntimeServiceMBean clusterService = (DomainRuntimeServiceMBean) MBeanServerInvocationHandler.
                newProxyInstance(mbconn, new ObjectName(DomainRuntimeServiceMBean.OBJECT_NAME));
            sm = (SessionManagementMBean) clusterService.findService(SessionManagementMBean.NAME,SessionManagementMBean.TYPE,null);
            sm.createSession(sessionName);            
            ALSBConfigurationMBean alsbSession = (ALSBConfigurationMBean) clusterService.
                 findService(ALSBConfigurationMBean.NAME + "." + "mysession",ALSBConfigurationMBean.TYPE,null);      
          
            if(servicetype.equals("ProxyService")) {
                Ref ref = constructRef("ProxyService",serviceURI);
                ProxyServiceConfigurationMBean proxyConfigMBean = (ProxyServiceConfigurationMBean) clusterService.
                 findService(ProxyServiceConfigurationMBean.NAME + "." + sessionName,ProxyServiceConfigurationMBean.TYPE,null);
                if(status){
                   proxyConfigMBean.enableService(ref);
                   statusmsg="Enabled the Service : " + serviceURI;
                }
                else {            
                    proxyConfigMBean.disableService(ref);   
                    statusmsg="Disabled the Service : " + serviceURI;
                }  
            }else if(servicetype.equals("BusinessService")) {
                Ref ref = constructRef("BusinessService",serviceURI);               
                BusinessServiceConfigurationMBean businessConfigMBean = (BusinessServiceConfigurationMBean) clusterService.
                 findService(BusinessServiceConfigurationMBean.NAME + "." + sessionName,BusinessServiceConfigurationMBean.TYPE,null);
                if(status){
                   businessConfigMBean.enableService(ref);
                   statusmsg="Enabled the Service : " + serviceURI;
                }
                else {            
                    businessConfigMBean.disableService(ref);   
                    statusmsg="Disabled the Service : " + serviceURI;
                }  
            }
           sm.activateSession(sessionName, statusmsg);           
           conn.close();
        }catch(Exception ex){
            if(null != sm) {
                try{
                    sm.discardSession(sessionName);
                }catch(Exception e) {
                    System.out.println("able to discard the session");
                  
                }
            }
            statusmsg="Not able to perform the operation";           
            ex.printStackTrace();          
        }finally{
            if(null != conn)
                try{
                    conn.close();
                }catch(Exception e) {
                    e.printStackTrace();
                }
        }
       
        return statusmsg;
    }
      
   // public static void main(String[] args) {       
        //changeProxyServiceStatus(servicetype, status, serviceURI, host, port, username, password)       
       // System.out.println(changeProxyServiceStatus("ProxyService", false, "HudsonDemo/proxy/JMSListener", "prometheus", 7001, "weblogic", "Weblogic123"));
        
       
    //}
    */
}

class MyWLBeanBrowser{
          
}
 
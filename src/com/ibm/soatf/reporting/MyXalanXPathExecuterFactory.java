/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ibm.soatf.reporting;

import javax.xml.transform.TransformerException;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.util.xml.JRXPathExecuter;
import net.sf.jasperreports.engine.util.xml.JRXPathExecuterFactory;

import org.apache.xml.utils.PrefixResolver;
import org.apache.xpath.CachedXPathAPI;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author user
 */
public class MyXalanXPathExecuterFactory implements JRXPathExecuterFactory {

    public MyXalanXPathExecuterFactory(){
        System.out.println("MyXalanXPathExecuterFactory.............");
    }

    @Override
    public JRXPathExecuter getXPathExecuter() {
        return new MyJRXPathExecuter();
    }

    /**
    * source copied from XalanXPathExecuter from jasperreports
    * and then tweeked for namespace support
    *
    */
    public class MyJRXPathExecuter implements JRXPathExecuter {
        // XPath API facade
        private CachedXPathAPI xpathAPI = new CachedXPathAPI();
        PrefixResolver resolver=null;

        public MyJRXPathExecuter() {
            System.out.println("MyJRXPathExecuter.............");
            //maybe link resolver through threadlocale from calling client through a map,
            //so namespaces can be set dynamically by client program
            resolver=new MyPrefixResolver();
        }

        public NodeList selectNodeList(Node contextNode, String expression) throws JRException {
            //System.out.println(",,,selectNodeList");
            xpathAPI.getXPathContext().setNamespaceContext(resolver);

            try {
                return xpathAPI.selectNodeList(contextNode, expression);
            } catch (TransformerException e) {
                throw new JRException("XPath selection failed. Expression: " + expression, e);
            }
        }

        public Object selectObject(Node contextNode, String expression) throws JRException {
            try {
                //System.out.println(",,,selectObject");
                //xpathAPI.getXPathContext().setNamespaceContext(resolver);
                Object value;
                XObject object = xpathAPI.eval(contextNode, expression,resolver);
                switch (object.getType()) {
                case XObject.CLASS_NODESET:
                    value = object.nodeset().nextNode();
                    break;
                case XObject.CLASS_BOOLEAN:
                    value = object.bool() ? Boolean.TRUE : Boolean.FALSE;
                    break;
                case XObject.CLASS_NUMBER:
                    value = new Double(object.num());
                    break;
                default:
                    value = object.str();
                    break;
                }
                return value;
            } catch (TransformerException e) {
                throw new JRException("XPath selection failed. Expression: " + expression, e);
            }
        }
    }

    //public class MyPrefixResolver extends JAXPPrefixResolver{
    public class MyPrefixResolver implements PrefixResolver {
        public MyPrefixResolver(){
            //super();
        }

        @Override
        public String getBaseIdentifier() {
            //System.out.println("///////////////getBaseIdentifier");
            return null;
        }

        @Override
        public String getNamespaceForPrefix(String arg0) {
            //System.out.println("///////////////getNamespaceForPrefix");
            //TODO: use threadlocale here...
            if(arg0.equalsIgnoreCase("stfconf")){
                return "http://www.ibm.com/SOATF/Config/Iface";
            }else if(arg0.equalsIgnoreCase("ftpconf")){
                return "http://www.ibm.com/SOATF/Config/Iface/FTP";
            }else if(arg0.equalsIgnoreCase("jmsconf")){
                return "http://www.ibm.com/SOATF/Config/Iface/JMS";
            }else if(arg0.equalsIgnoreCase("dbconf")){
                return "http://www.ibm.com/SOATF/Config/Iface/DB";
            }else if(arg0.equalsIgnoreCase("soapconf")){
                return "http://www.ibm.com/SOATF/Config/Iface/SOAP";
            }else if(arg0.equalsIgnoreCase("utilconf")){
                return "http://www.ibm.com/SOATF/Config/Iface/UTIL";                
            }
            return null;
        }

        @Override
        public String getNamespaceForPrefix(String arg0, Node arg1) {
            //System.out.println("///////////////getNamespaceForPrefix2");
            //TODO: use threadlocale here...
            return getNamespaceForPrefix(arg0);
        }

        public boolean handlesNullPrefixes() {
            return false;
        }

    }
}
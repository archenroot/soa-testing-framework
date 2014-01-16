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
package com.ibm.soatf.reporting;

import com.ibm.soatf.FrameworkException;
import com.ibm.soatf.component.AbstractSoaTFComponent;
import com.ibm.soatf.component.SOATFCompType;
import com.ibm.soatf.config.master.Operation;
import com.ibm.soatf.flow.OperationResult;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRXmlUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author zANGETSu
 */
public class ReportComponent  extends AbstractSoaTFComponent {
    private static final Logger logger = LogManager.getLogger(ReportComponent.class);
    private static final String REPORT_NAME = "UTP";
    
    private final File masterConfigFile;
    private final File ifaceConfigFile;
    private final File workingDir;
    private final String flowPattern;
    private final String testName;
    
    private final OperationResult cor;
    
    public ReportComponent(File masterConfigFile,
            File ifaceConfigFile,         
            String flowPattern,
            String testName,
            File workingDir) {
        super(SOATFCompType.REPORT);
        this.masterConfigFile = masterConfigFile;
        this.ifaceConfigFile = ifaceConfigFile;
        this.flowPattern = flowPattern;
        if (testName != null) {
            this.testName = testName.replaceAll("'", "\\'");
        } else {
            this.testName = "";
        }
        this.workingDir = workingDir;
        cor = OperationResult.getInstance();
        constructComponent();
    }    

    @Override
    protected void constructComponent() {
        
    }

    @Override
    protected void destructComponent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void executeOperation(Operation operation) {
        //cor.setOperation(operation); //nastavuje konstruktor abstractoperation
        switch (operation.getName()) {
        case REPORT_GENERATE:
            try {
                System.setProperty("net.sf.jasperreports.properties", "reporting/IBM - SOA Testing Framework/jasperreports.properties");
                //JRXmlDataSource JprXmlDsr = new JRXmlDataSource (ifaceConfigFile, "//*[@testName='"+testName+"']");
                //JRXmlDataSource JprXmlDsr = new JRXmlDataSource (ifaceConfigFile);

                /* Compile the template */
                JasperReport Rep = JasperCompileManager.compileReport ("reporting/IBM - SOA Testing Framework/IBM_IrishWater_Report.jrxml");

                /* Create the JasperPrint object with the template and the data */
                Map<String, Object> params = new HashMap<>();
                /*Map<String, String> namespaces = new HashMap<>();
                namespaces.put("stfconf","http://www.ibm.com/SOATF/Config/Iface");
                namespaces.put("ftpconf","http://www.ibm.com/SOATF/Config/Iface/FTP");
                namespaces.put("jmsconf","http://www.ibm.com/SOATF/Config/Iface/JMS");
                namespaces.put("dbconf","http://www.ibm.com/SOATF/Config/Iface/DB");
                namespaces.put("soapconf","http://www.ibm.com/SOATF/Config/Iface/SOAP");
                namespaces.put("utilconf","http://www.ibm.com/SOATF/Config/Iface/UTIL");
                params.put("XML_NAMESPACE_MAP", namespaces);
                params.put("net.sf.jasperreports.xml.detect.namespaces", true);
                params.put("net.sf.jasperreports.xml.namespace.stfconf", "http://www.ibm.com/SOATF/Config/Iface");
                params.put("net.sf.jasperreports.xml.namespace.ftpconf", "http://www.ibm.com/SOATF/Config/Iface/FTP");
                params.put("net.sf.jasperreports.xml.namespace.jmsconf", "http://www.ibm.com/SOATF/Config/Iface/JMS");
                params.put("net.sf.jasperreports.xml.namespace.dbconf", "http://www.ibm.com/SOATF/Config/Iface/DB");
                params.put("net.sf.jasperreports.xml.namespace.soapconf", "http://www.ibm.com/SOATF/Config/Iface/SOAP");
                params.put("net.sf.jasperreports.xml.namespace.utilconf", "http://www.ibm.com/SOATF/Config/Iface/UTIL");*/
                params.put("testName", testName);
                Document document = JRXmlUtils.parse(JRLoader.getInputStream(ifaceConfigFile), true);
                Document masterDocument = JRXmlUtils.parse(JRLoader.getInputStream(masterConfigFile));
                
                XPathFactory factory = XPathFactory.newInstance();
                XPath xpath = factory.newXPath();
                XPathExpression expr = xpath.compile("//*[local-name()= 'ifaceFlowPattern'][./*[local-name()='InstanceMetadata']/*[local-name()='testName'] = '"+testName+"'][1]");

                //Object result = expr.evaluate(document, XPathConstants.NODESET);
                Node result = (Node) expr.evaluate(document, XPathConstants.NODE);
                document = JRXmlUtils.createDocument(result, true);
                
                XPath xpath2 = factory.newXPath();
                XPathExpression expr2 = xpath2.compile("//flowPattern[@identificator='"+flowPattern+"'][1]");
                Node result2 = (Node) expr2.evaluate(masterDocument, XPathConstants.NODE);
                masterDocument = JRXmlUtils.createDocument(result2);                

                /*TransformerFactory tranFactory = TransformerFactory.newInstance();
                Transformer transformer = tranFactory.newTransformer();
                Source src = new DOMSource(masterDocument);
                Result dest = new StreamResult(System.out);
                transformer.transform(src, dest);               */
                final String suffix = new SimpleDateFormat("_yyyyMMdd_hhmmss").format(new Date());
                final String fileName = REPORT_NAME + suffix + ".pdf";
                
                params.put("fileName", fileName);
                params.put("MASTER_DATA_DOCUMENT", masterDocument);
                params.put("operationResult", cor);
                params.put(JRXPathQueryExecuterFactory.PARAMETER_XML_DATA_DOCUMENT, document);
                JasperPrint  Prn = JasperFillManager.fillReport (Rep, params);

                /* Export the report to pdf format if needed*/
                JasperExportManager.exportReportToPdfFile (Prn, workingDir + "/" + fileName);
                cor.markSuccessful();
            } catch (   Exception ex) {
                logger.fatal(ex);
            }
            break;                                
        default:
            logger.info("Operation execution not yet implemented: " + operation.getName().value());
        }
    }
    
}

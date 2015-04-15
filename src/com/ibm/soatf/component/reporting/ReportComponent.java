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
package com.ibm.soatf.component.reporting;

import com.ibm.soatf.FrameworkException;
import com.ibm.soatf.component.AbstractSoaTFComponent;
import com.ibm.soatf.component.SOATFCompType;
import com.ibm.soatf.config.ConfigurationManager;
import com.ibm.soatf.config.FrameworkConfigurationException;
import com.ibm.soatf.config.MasterConfiguration;
import com.ibm.soatf.config.master.Operation;
import com.ibm.soatf.flow.FlowExecutor;
import com.ibm.soatf.flow.FrameworkExecutionException;
import com.ibm.soatf.flow.OperationResult;
import com.ibm.soatf.gui.ProgressMonitor;
import com.ibm.soatf.tool.Utils;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRXmlUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 * @author zANGETSu
 */
public class ReportComponent  extends AbstractSoaTFComponent {
    private static final Logger logger = LogManager.getLogger(ReportComponent.class);
    private static final String REPORT_NAME = "UTP";
    private static final MasterConfiguration MCFG = ConfigurationManager.getInstance().getMasterConfig();
    
    private final String ifaceName;
    private final String flowPattern;
    private final String testName;
    private final String scenarioName;
    
    private final OperationResult cor;
    
    private static Document lastMasterDOMpart;
    private static Document lastMasterDOMSource;
    
    private static Document lastInterfaceDOMpart;
    private static Document lastInterfaceDOMSource;
    
    public ReportComponent(String ifaceName,         
            String flowPattern,
            String testName,
            String scenarioName,
            File workingDir) {
        super(SOATFCompType.REPORT);
        this.ifaceName = ifaceName;
        this.flowPattern = flowPattern;
        if (testName != null) {
            this.testName = testName.replaceAll("'", "\\'");
        } else {
            this.testName = "";
        }
        this.scenarioName = scenarioName;
        this.workingDir = new File(workingDir, MCFG.getReportDirName());
        cor = OperationResult.getInstance();
        constructComponent();
    }    

    @Override
    protected final void constructComponent() {
        
    }

    @Override
    protected void destructComponent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void executeOperation(Operation operation) throws FrameworkException {
        switch (operation.getName()) {
        case REPORT_GENERATE:
            generateReport();
            break;                                                                
        default:
            logger.info("Operation execution not yet implemented: " + operation.getName().value());
            cor.addMsg("Operation: " + operation.getName().value() + " is valid, but not yet implemented");            
        }
    }   
    
    /**
     * Generates the PDF document based containing the results of all previous operations 
     * @throws FrameworkExecutionException when error occurs while compiling and filling the report
     */
    private void generateReport() throws FrameworkExecutionException {
        try {
            ProgressMonitor.init(5, "Compiling report...");
            System.setProperty("net.sf.jasperreports.properties", "reporting/IBM - SOA Testing Framework/jasperreports.properties");
            //JRXmlDataSource JprXmlDsr = new JRXmlDataSource (ifaceConfigFile, "//*[@testName='"+testName+"']");
            //JRXmlDataSource JprXmlDsr = new JRXmlDataSource (ifaceConfigFile);
            JasperReport report = JasperCompileManager.compileReport ("reporting/IBM - SOA Testing Framework/IBM_IrishWater_Report.jrxml");

            Map<String, Object> params = new HashMap<>();
            params.put("testName", testName);
            
            Document document = ConfigurationManager.getInstance().getInterfaceConfig(ifaceName).getDOM();
            Document masterDocument = ConfigurationManager.getInstance().getMasterConfig().getNoNamespaceDOM();

            XPathFactory factory = XPathFactory.newInstance();            
            
            ProgressMonitor.increment("Extracting instance metadata from interface...");
            if (lastInterfaceDOMSource != document || lastInterfaceDOMpart == null) {
                XPath xpath = factory.newXPath();
                XPathExpression expr = xpath.compile("//*[local-name()= 'ifaceFlowPattern'][./*[local-name()='InstanceMetadata']/*[local-name()='testName'] = '"+testName+"'][1]");
                //Object result = expr.evaluate(document, XPathConstants.NODESET);
                Node result = (Node) expr.evaluate(document, XPathConstants.NODE);
                lastInterfaceDOMpart = JRXmlUtils.createDocument(result, true);
                lastInterfaceDOMSource = document;
            }
            
            ProgressMonitor.increment("Extracting flow pattern from master...");
            if (lastMasterDOMSource != masterDocument || lastMasterDOMpart == null) {    
                XPath xpath = factory.newXPath();
                XPathExpression expr = xpath.compile("//flowPattern[@identificator='"+flowPattern+"'][1]");
                //XPathExpression expr2 = xpath2.compile("//*[local-name()= 'flowPattern'][@identificator='"+flowPattern+"'][1]");
                Node result = (Node) expr.evaluate(masterDocument, XPathConstants.NODE);
                lastMasterDOMpart = JRXmlUtils.createDocument(result);
                lastMasterDOMSource = masterDocument;
            }
            
            final String fileName = Utils.insertTimestampToFilename(REPORT_NAME+".pdf", FlowExecutor.getActualRunDate());
            final String fullFileName = new File(workingDir, fileName).getAbsolutePath();

            params.put("fileName", fileName);
            params.put("MASTER_DATA_DOCUMENT", lastMasterDOMpart);
            params.put("scenarioName", scenarioName);
            params.put("operationResult", cor);
            params.put(JRXPathQueryExecuterFactory.PARAMETER_XML_DATA_DOCUMENT, lastInterfaceDOMpart);
            ProgressMonitor.increment("Filling report...");
            JasperPrint print = JasperFillManager.fillReport (report, params);

            /* Export the report to pdf format if needed*/
            ProgressMonitor.increment("Exporting report...");
            JasperExportManager.exportReportToPdfFile (print, fullFileName);
            final String msg = "Successfully generater report in <a href='file://" + fullFileName + "'>" + fullFileName + "</a>";
            logger.info(msg);
            cor.addMsg(msg);
            cor.markSuccessful();
        } catch (JRException ex) {
            throw new FrameworkExecutionException("Failed to generate report: " + ex.getMessage());
        } catch (XPathExpressionException ex) {
            throw new FrameworkExecutionException("Failed to parse config files for report data feed: " + ex.getMessage());
        } catch (FrameworkConfigurationException ex) {
            throw new FrameworkExecutionException("Failed to parse master config file for report data feed: " + ex.getMessage());
//        } catch (TransformerConfigurationException ex) {
//            java.util.logging.Logger.getLogger(ReportComponent.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (TransformerException ex) {
//            java.util.logging.Logger.getLogger(ReportComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

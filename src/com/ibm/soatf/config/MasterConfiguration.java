/*
 * Copyright (C) 2013 Ladislav Jech
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
package com.ibm.soatf.config;

import static com.ibm.soatf.config.MasterFrameworkConfig.IFACE_CONFIG_FILENAME;
import com.ibm.soatf.config.master.Databases.Database;
import com.ibm.soatf.config.master.Databases.Database.DatabaseInstance;
import com.ibm.soatf.config.master.EmailServers.EmailServer;
import com.ibm.soatf.config.master.EmailServers.EmailServer.EmailServerInstance;
import com.ibm.soatf.config.master.ExecBlockOperation;
import com.ibm.soatf.config.master.FTPServers.FtpServer;
import com.ibm.soatf.config.master.FTPServers.FtpServer.Directories;
import com.ibm.soatf.config.master.FTPServers.FtpServer.FtpServerInstance;
import com.ibm.soatf.config.master.FlowPattern;
import com.ibm.soatf.config.master.Interface;
import com.ibm.soatf.config.master.Interface.Patterns.ReferencedFlowPattern;
import com.ibm.soatf.config.master.OSBReporting;
import com.ibm.soatf.config.master.OSBReporting.OsbReportingInstance;
import com.ibm.soatf.config.master.Operation;
import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance;
import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance.AdminServer;
import com.ibm.soatf.config.master.Project;
import com.ibm.soatf.config.master.SOATestingFrameworkMasterConfiguration;
import com.ibm.soatf.config.master.TestScenario;
import com.ibm.soatf.config.master.TestScenario.ExecutionBlock;
import com.ibm.soatf.config.master.TestScenarioPreOrPostExecutionBlock;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.XMLConstants;
import javax.xml.bind.Binder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Class responsible for input/output operations between framework and master
 * configuration file and SOA Testing Framework.
 *
 * @author Ladislav Jech <archenroot@gmail.com>
 */
public final class MasterConfiguration {

    private static final Logger logger = LogManager.getLogger(MasterConfiguration.class.getName());

    /**
     *
     */
    public static final String OSB_CLUSTER_TYPE = "OSB";

    /**
     *
     */
    public static final String SOA_CLUSTER_TYPE = "SOA";

    /**
     *
     */
    public static final String WSM_CLUSTER_TYPE = "WSM";

    private final MasterFrameworkConfig MFC;

    private SOATestingFrameworkMasterConfiguration XML_CONFIG;

    private Set<String> environments;

    private final Map<String, InterfaceConfiguration> ICFG = new HashMap<>();
    private final Map<Interface, InterfaceConfiguration> ICFG2 = new HashMap<>();
    private Document DOM;
    private Document noNamespaceDOM;
    private Binder<Node> binder;

    MasterConfiguration(final MasterFrameworkConfig mfc) {
        MFC = mfc;
    }

    /**
     * If refers <code>./schema/SOATFMasterConfig/SOATFMasterConfig.xsd</code> for schema file and
     * creates a document builder by passing the schema file as argument.
     * this document builder refers the <code>MasterFrameworkConfig.masterConfigFile</code> for the 
     * physical location of <code>master-config</code> file and parse the file using DOM and creates a JAVA object name 
     * <code>SOATestingFrameworkMasterConfiguration</code>
     * @throws FrameworkConfigurationException
     */
    void init() throws MasterConfigurationException {
        logger.info("Unmarshalling master configuration from file: " + MFC.getMasterConfigFile());
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            SchemaFactory sFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            File schemaFile = new File(new File(new File(MasterFrameworkConfig.SOATF_HOME, "schema"), "SOATFMasterConfig"), "SOATFMasterConfig.xsd");
            dbf.setSchema(sFactory.newSchema(schemaFile));
            DocumentBuilder builder = dbf.newDocumentBuilder();
            builder.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) throws SAXException {
                    throw exception;
                }
                @Override
                public void error(SAXParseException exception) throws SAXException {
                    throw exception;
                }
                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    throw exception;
                }
            });
            DOM = builder.parse(MFC.getMasterConfigFile());
            DocumentBuilderFactory noNamespaceFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder noNamespaceBuilder = noNamespaceFactory.newDocumentBuilder();
            noNamespaceDOM = noNamespaceBuilder.parse(MFC.getMasterConfigFile());
            JAXBContext jaxbContext = JAXBContext.newInstance("com.ibm.soatf.config.master");
            binder = jaxbContext.createBinder();
            XML_CONFIG = ((JAXBElement<SOATestingFrameworkMasterConfiguration>) binder.unmarshal(DOM)).getValue();
        } catch (JAXBException jbex) {
            throw new MasterConfigurationException("Error while unmarshalling master configuration object from XML file " + MFC.getMasterConfigFile(), jbex);
        } catch (ParserConfigurationException ex) {
            String msg = "Error while attempting to create DocumentBuilder while processing " + MFC.getMasterConfigFile().getAbsolutePath();
            throw new MasterConfigurationException(msg, ex);
        } catch (SAXException ex) {
            String msg = "Error while parsing master configuration from " + MFC.getMasterConfigFile().getAbsolutePath() + ": " + ex.getMessage();
            throw new MasterConfigurationException(msg, ex);
        } catch (IOException ex) {
            String msg = "I/O Error occured when trying to parse master configuration file " + MFC.getMasterConfigFile().getAbsolutePath() + ": " + ex.getMessage();
            throw new MasterConfigurationException(msg, ex);
        }
    }

    /**
     * Gets all environments configured within master configuration file.
     *
     * @return all environments definitions within framework configuration file.
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public Set<String> getAllEnvironments() throws MasterConfigurationException {
        if (environments == null) {
            NodeList list = getNodeList("//*/@environment", DOM);
            //sorting case insensitive
            environments = new TreeSet<>(new Comparator<String>() {

                @Override
                public int compare(String s1, String s2) {
                    return s1.compareToIgnoreCase(s2);
                }
            });
            for (int i = 0; i < list.getLength(); i++) {
                environments.add(list.item(i).getNodeValue());
            }
        }
        return environments;
    }
    
    private NodeList getNodeList(String xpathExpr, Document dom) throws MasterConfigurationException {
        try {
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            NodeList list = (NodeList) xpath.evaluate(xpathExpr, dom, XPathConstants.NODESET);
            return list;
        } catch (XPathExpressionException ex) {
            String msg = "Error while evaluating XPath expression.";
            logger.error(msg, ex);
            throw new MasterConfigurationException(ex);
        }
    }
    
    private Node getSingleNode(String xpathExpr, Document dom) throws MasterConfigurationException {
        try {
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            Node node = (Node) xpath.evaluate(xpathExpr, dom, XPathConstants.NODE);
            return node;
        } catch (XPathExpressionException ex) {
            String msg = "Error while evaluating XPath expression.";
            logger.error(msg, ex);
            throw new MasterConfigurationException(ex);
        }
    }

    /**
     * Gets list of all interfaces configured within framework master
     * configuration file.
     *
     * @return list of configured interfaces
     * @see com.ibm.soatf.config.master.Interface
     */
    public List<SOATestingFrameworkMasterConfiguration.Interfaces.Interface> getInterfaces() {
        final List<SOATestingFrameworkMasterConfiguration.Interfaces.Interface> iface = XML_CONFIG.getInterfaces().getInterface();
        if (iface.isEmpty()) {
            logger.warn("There are no interfaces defined in master configuration XML file.");
        }
        return iface;
    }

    /**
     * Gets list of all interface names configured within framework master
     * configuration file.
     *
     * @return list of configured interface names
     */
    public List<String> getInterfaceNames() {
        List<String> interfaceNames = new ArrayList<>();
        for (Interface iface : getInterfaces()) {
            interfaceNames.add(iface.getName());
        }
        return interfaceNames;
    }

    /**
     * Gets concrete interface.
     *
     * @param interfaceName String representation of interface name.
     * @return concrete interface instance
     * @throws com.ibm.soatf.config.MasterConfigurationException
     * @see com.ibm.soatf.config.master.Interface
     */
    public Interface getInterface(String interfaceName) throws MasterConfigurationException {

        for (Interface iface : getInterfaces()) {
            if (iface.getName().equals(interfaceName)) {
                return iface;
            }
        }
        throw new MasterConfigurationException("Interface with following identificator cannot be found in master configuration: " + interfaceName);
    }

    /**
     * Gets list of all relative project definition for selected interface.
     *
     * @param interfaceName String representation of interface name
     * @return list of configured projects.
     * @throws com.ibm.soatf.config.MasterConfigurationException
     * @see com.ibm.soatf.config.master.Project
     */
    public List<Project> getProjects(String interfaceName) throws MasterConfigurationException {
        final List<Project> projects = getInterface(interfaceName).getProjects().getProject();
        if (projects.isEmpty()) {
            throw new MasterConfigurationException("There are no configured projects for interface " + interfaceName + " in master configuration XML file.");
        }
        return projects;
    }

    /**
     * Gets concrete interface related project.
     *
     * @param interfaceName interface name
     * @param projectName project name
     * @return concrete project
     * @see com.ibm.soatf.config.master.Project
     */
    public Project getProject(String interfaceName, String projectName) {
        throw new UnsupportedOperationException("Not supported yet.");

    }

    /**
     * Gets list of interface referenced flow patterns.
     *
     * @param interfaceName String representation of interface name
     * @return list of flow patterns referenced by interface
     * @throws com.ibm.soatf.config.MasterConfigurationException
     * @see com.ibm.soatf.config.master.Interface.Patterns.ReferencedFlowPattern
     */
    public List<ReferencedFlowPattern> getReferencedFlowPatterns(String interfaceName) throws MasterConfigurationException {
        final List<ReferencedFlowPattern> referencedFlowPatterns = getInterface(interfaceName).getPatterns().getReferencedFlowPattern();
        if (referencedFlowPatterns.isEmpty()) {
            throw new MasterConfigurationException("There are no configured referenced flow patterns for interface " + interfaceName + ".");
        }
        return referencedFlowPatterns;
    }

    /**
     *
     * @param interfaceName
     * @param projectName
     * @return
     */
    public ReferencedFlowPattern getReferencedFlowPattern(String interfaceName, String projectName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     *
     * @return @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public List<OracleFusionMiddlewareInstance> getOracleFusionMiddlewareInstances() throws MasterConfigurationException {
        if (XML_CONFIG.getEnvironments().getOracleFusionMiddleware().getOracleFusionMiddlewareInstance().isEmpty()) {
            throw new MasterConfigurationException("There are no Oracle Fusion Middleware instances configured.");
        }
        return XML_CONFIG.getEnvironments().getOracleFusionMiddleware().getOracleFusionMiddlewareInstance();
    }

    /**
     *
     * @param environment
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public OracleFusionMiddlewareInstance getOracleFusionMiddlewareInstance(String environment) throws MasterConfigurationException {
        for (OracleFusionMiddlewareInstance inst : getOracleFusionMiddlewareInstances()) {
            if (inst.getEnvironment().equals(environment)) {
                return inst;
            }
        }
        throw new MasterConfigurationException("Oracle Fusion Middleware instance configuration not found for environment " + environment);
    }

    /**
     *
     * @param environment
     * @return
     * @throws MasterConfigurationException
     */
    public AdminServer getAdminServer(String environment) throws MasterConfigurationException {
        OracleFusionMiddlewareInstance ofmwi = getOracleFusionMiddlewareInstance(environment);
        AdminServer as = ofmwi.getAdminServer();
        if (as == null) {
            throw new MasterConfigurationException("Missing admin server configuration.");
        }
        return as;
    }

    /**
     *
     * @param environment
     * @return
     * @throws MasterConfigurationException
     */
    public List<OracleFusionMiddlewareInstance.Clusters.Cluster> getClusters(String environment) throws MasterConfigurationException {
        OracleFusionMiddlewareInstance.Clusters clusters = getOracleFusionMiddlewareInstance(environment).getClusters();
        // TODO
        if (false) {
            throw new MasterConfigurationException("TODO");
        }
        return clusters.getCluster();
    }

    /**
     *
     * @param ofmwi
     * @return
     * @throws MasterConfigurationException
     */
    public List<OracleFusionMiddlewareInstance.Clusters.Cluster> getClusters(OracleFusionMiddlewareInstance ofmwi) throws MasterConfigurationException {
        OracleFusionMiddlewareInstance.Clusters clusters = ofmwi.getClusters();
        // TODO
        if (false) {
            throw new MasterConfigurationException("TODO");
        }
        return clusters.getCluster();
    }

    /**
     *
     * @param environment
     * @return
     * @throws MasterConfigurationException
     */
    public OracleFusionMiddlewareInstance.Clusters.Cluster getOsbCluster(String environment) throws MasterConfigurationException {
        List<OracleFusionMiddlewareInstance.Clusters.Cluster> clusters = getOracleFusionMiddlewareInstance(environment).getClusters().getCluster();

        for (OracleFusionMiddlewareInstance.Clusters.Cluster cluster : clusters) {
            if (cluster.getType().equals(OSB_CLUSTER_TYPE)) {
                return cluster;

            }

        }
        throw new MasterConfigurationException("Cannot find any OSB cluster type configuration in master configuration file.");
    }

    /**
     *
     * @param ofmwi
     * @return
     * @throws MasterConfigurationException
     */
    public OracleFusionMiddlewareInstance.Clusters.Cluster getOsbCluster(OracleFusionMiddlewareInstance ofmwi) throws MasterConfigurationException {
        List<OracleFusionMiddlewareInstance.Clusters.Cluster> clusters = ofmwi.getClusters().getCluster();

        for (OracleFusionMiddlewareInstance.Clusters.Cluster cluster : clusters) {
            if (cluster.getType().equals(OSB_CLUSTER_TYPE)) {
                return cluster;

            }

        }
        throw new MasterConfigurationException("Cannot find any OSB cluster type configuration in master configuration file.");
    }

    /**
     *
     * @param environment
     * @return
     * @throws MasterConfigurationException
     */
    public OracleFusionMiddlewareInstance.Clusters.Cluster getSoaCluster(String environment) throws MasterConfigurationException {
        List<OracleFusionMiddlewareInstance.Clusters.Cluster> clusters = getOracleFusionMiddlewareInstance(environment).getClusters().getCluster();

        for (OracleFusionMiddlewareInstance.Clusters.Cluster cluster : clusters) {
            if (cluster.getType().equals(SOA_CLUSTER_TYPE)) {
                return cluster;

            }

        }
        throw new MasterConfigurationException("Cannot find any SOA cluster type configuration in master configuration file.");
    }

    /**
     *
     * @param ofmwi
     * @return
     * @throws MasterConfigurationException
     */
    public OracleFusionMiddlewareInstance.Clusters.Cluster getSoaCluster(OracleFusionMiddlewareInstance ofmwi) throws MasterConfigurationException {
        List<OracleFusionMiddlewareInstance.Clusters.Cluster> clusters = ofmwi.getClusters().getCluster();

        for (OracleFusionMiddlewareInstance.Clusters.Cluster cluster : clusters) {
            if (cluster.getType().equals(SOA_CLUSTER_TYPE)) {
                return cluster;

            }

        }
        throw new MasterConfigurationException("Cannot find any SOA cluster type configuration in master configuration file.");
    }

    /**
     *
     * @param environment
     * @return
     * @throws MasterConfigurationException
     */
    public OracleFusionMiddlewareInstance.Clusters.Cluster getWsmCluster(String environment) throws MasterConfigurationException {
        List<OracleFusionMiddlewareInstance.Clusters.Cluster> clusters = getOracleFusionMiddlewareInstance(environment).getClusters().getCluster();

        for (OracleFusionMiddlewareInstance.Clusters.Cluster cluster : clusters) {
            if (cluster.getType().equals(WSM_CLUSTER_TYPE)) {
                return cluster;

            }

        }
        throw new MasterConfigurationException("Cannot find any WSM cluster type configuration in master configuration file.");
    }

    /**
     *
     * @param ofmwi
     * @return
     * @throws MasterConfigurationException
     */
    public OracleFusionMiddlewareInstance.Clusters.Cluster getWsmCluster(OracleFusionMiddlewareInstance ofmwi) throws MasterConfigurationException {
        List<OracleFusionMiddlewareInstance.Clusters.Cluster> clusters = ofmwi.getClusters().getCluster();

        for (OracleFusionMiddlewareInstance.Clusters.Cluster cluster : clusters) {
            if (cluster.getType().equals(WSM_CLUSTER_TYPE)) {
                return cluster;

            }

        }
        throw new MasterConfigurationException("Cannot find any WSM cluster type configuration in master configuration file.");
    }

    /**
     *
     * @param environment
     * @return
     * @throws MasterConfigurationException
     */
    public OracleFusionMiddlewareInstance.Clusters.Cluster.ManagedServer getFirstManagedServerInCluster(String environment) throws MasterConfigurationException {
        for (OracleFusionMiddlewareInstance.Clusters.Cluster.ManagedServer ms : getWsmCluster(environment).getManagedServer()) {
            return ms;
        }

        throw new MasterConfigurationException("There are no managed servers configured within lookup cluster: " + getWsmCluster(environment).getName());
    }

    /**
     *
     * @param cluster
     * @return
     * @throws MasterConfigurationException
     */
    public OracleFusionMiddlewareInstance.Clusters.Cluster.ManagedServer getFirstManagedServerInCluster(
            OracleFusionMiddlewareInstance.Clusters.Cluster cluster) throws MasterConfigurationException {
        for (OracleFusionMiddlewareInstance.Clusters.Cluster.ManagedServer ms : cluster.getManagedServer()) {
            return ms;
        }
        throw new MasterConfigurationException("There are no managed servers configured within lookup cluster: " + cluster.getName());
    }

    /**
     *
     * @return @throws com.ibm.soatf.config.MasterConfigurationException @throws
     * FrameworkConfigurationException
     */
    public List<Database> getDatabases() throws MasterConfigurationException {
        if (XML_CONFIG.getEnvironments().getDatabases().getDatabase().isEmpty()) {
            throw new MasterConfigurationException("There are is no database configured.");
        }
        return XML_CONFIG.getEnvironments().getDatabases().getDatabase();
    }

    /**
     *
     * @param identificator
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public Database getDatabase(String identificator) throws MasterConfigurationException {
        for (Database database : getDatabases()) {
            if (database.getIdentificator().equals(identificator)) {
                return database;
            }
        }
        throw new MasterConfigurationException("Database configuration with identificator " + identificator + " cannot be found.");
    }

    /**
     *
     * @param identificator
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public List<DatabaseInstance> getDatabaseInstances(String identificator) throws MasterConfigurationException {
        if (getDatabase(identificator).getDatabaseInstance().isEmpty()) {
            throw new MasterConfigurationException("There are no database instances configured for database environment identificator " + identificator + ".");
        }
        return getDatabase(identificator).getDatabaseInstance();
    }

    /**
     *
     * @param environment
     * @param identificator
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public DatabaseInstance getDatabaseInstance(String environment, String identificator) throws MasterConfigurationException {
        for (DatabaseInstance inst : getDatabaseInstances(identificator)) {
            if (inst.getEnvironment().equals(environment)) {
                return inst;
            }
        }
        throw new MasterConfigurationException("Database configuration with identificator " + identificator + " for environment " + environment + " cannot be found.");
    }

    /**
     *
     * @return @throws com.ibm.soatf.config.MasterConfigurationException @throws
     * FrameworkConfigurationException
     */
    public List<FtpServer> getFTPServers() throws MasterConfigurationException {
        if (XML_CONFIG.getEnvironments().getFtpServers().getFtpServer().isEmpty()) {
            throw new MasterConfigurationException("There are zero FTP servers in configuration.");
        }
        return XML_CONFIG.getEnvironments().getFtpServers().getFtpServer();
    }
    
    /**
     *
     * @return @throws com.ibm.soatf.config.MasterConfigurationException @throws
     * FrameworkConfigurationException
     */
    public List<EmailServer> getEmailServers() throws MasterConfigurationException {
        if (XML_CONFIG.getEnvironments().getEmailServers().getEmailServer().isEmpty()) {
            throw new MasterConfigurationException("There are zero Email servers in configuration.");
        }
        return XML_CONFIG.getEnvironments().getEmailServers().getEmailServer();
    }    

    /**
     *
     * @param identificator
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public FtpServer getFTPServer(String identificator) throws MasterConfigurationException {
        for (FtpServer ftpServer : getFTPServers()) {
            if (ftpServer.getIdentificator().equals(identificator)) {
                return ftpServer;
            }
        }
        throw new MasterConfigurationException("FTP server configuration with identificator " + identificator + " cannot be found.");
    }
    
    /**
     *
     * @param identificator
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public EmailServer getEmailServer(String identificator) throws MasterConfigurationException {
        for (EmailServer emailServer : getEmailServers()) {
            if (emailServer.getIdentificator().equals(identificator)) {
                return emailServer;
            }
        }
        throw new MasterConfigurationException("Email server configuration with identificator " + identificator + " cannot be found.");
    }    

    /**
     *
     * @param identificator
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public Directories getFTPServerDirectories(String identificator) throws MasterConfigurationException {

        Directories directories = getFTPServer(identificator).getDirectories();
        if (directories == null) {
            throw new MasterConfigurationException();
        }
        return directories;
    }

    /**
     *
     * @param identificator
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public List<FtpServerInstance> getFTPServerInstances(String identificator) throws MasterConfigurationException {
        if (getFTPServer(identificator).getFtpServerInstance().isEmpty()) {
            throw new MasterConfigurationException("There are no ftp server instances configured for database environment identificator " + identificator + ".");
        }
        return getFTPServer(identificator).getFtpServerInstance();
    }
    
    /**
     *
     * @param identificator
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public List<EmailServerInstance> getEmailServerInstances(String identificator) throws MasterConfigurationException {
        if (getEmailServer(identificator).getEmailServerInstance().isEmpty()) {
            throw new MasterConfigurationException("There are no email server instances configured for environment identificator " + identificator + ".");
        }
        return getEmailServer(identificator).getEmailServerInstance();
    }    

    /**
     *
     * @param environment
     * @param identificator
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public FtpServerInstance getFtpServerInstance(String environment, String identificator) throws MasterConfigurationException {
        for (FtpServerInstance inst : getFTPServerInstances(identificator)) {
            if (inst.getEnvironment().equals(environment)) {
                return inst;
            }
        }
        throw new MasterConfigurationException("FTP instance configuration with identificator " + identificator + " for environment " + environment + " cannot be found.");
    }
    
    /**
     *
     * @param environment
     * @param identificator
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public EmailServerInstance getEmailServerInstance(String environment, String identificator) throws MasterConfigurationException {
        for (EmailServerInstance inst : getEmailServerInstances(identificator)) {
            if (inst.getEnvironment().equals(environment)) {
                return inst;
            }
        }
        throw new MasterConfigurationException("Email instance configuration with identificator " + identificator + " for environment " + environment + " cannot be found.");
    }    

    /**
     *
     * @return @throws com.ibm.soatf.config.MasterConfigurationException @throws
     * FrameworkConfigurationException
     */
    public List<FlowPattern> getFlowPatterns() throws MasterConfigurationException {
        if (XML_CONFIG.getFlowPatterns().getFlowPattern().isEmpty()) {
            throw new MasterConfigurationException("There are no flow pattern definitions available in the configuration file.");
        }
        return XML_CONFIG.getFlowPatterns().getFlowPattern();
    }

    /**
     *
     * @param flowPatternId
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public FlowPattern getFlowPattern(String flowPatternId) throws MasterConfigurationException {
        for (FlowPattern flowPattern : getFlowPatterns()) {
            if (flowPattern.getIdentificator().equals(flowPatternId)) {
                return flowPattern;
            }
        }
        throw new MasterConfigurationException("Master configuration file - no such flow pattern found: " + flowPatternId);
    }

    /**
     *
     * @param flowPatternId
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public List<TestScenario> getTestScenarios(String flowPatternId) throws MasterConfigurationException {
        final List<TestScenario> testScenarios = getFlowPattern(flowPatternId).getTestScenario();
        if (testScenarios.isEmpty()) {
            throw new MasterConfigurationException("No test scenario found in flow pattern: " + flowPatternId);
        }
        return testScenarios;
    }

    /**
     *
     * @param flowPatternId
     * @param testScenarioId
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public TestScenario getTestScenario(String flowPatternId, String testScenarioId) throws MasterConfigurationException {
        for (TestScenario testScenario : getTestScenarios(flowPatternId)) {
            if (testScenario.getIdentificator().equals(testScenarioId)) {
                return testScenario;
            }
        }
        throw new MasterConfigurationException("No such test scenario found within framework master configuration file: " + testScenarioId + " in flow pattern: " + flowPatternId);
    }

    /**
     *
     * @param flowPatternId
     * @param testScenarioId
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public List<ExecutionBlock> getExecutionBlocks(String flowPatternId, String testScenarioId) throws MasterConfigurationException {
        final List<ExecutionBlock> executionBlocks = getTestScenario(flowPatternId, testScenarioId).getExecutionBlock();
        if (executionBlocks.isEmpty()) {
            throw new MasterConfigurationException("No execution blocks found in test scenario: " + testScenarioId
                    + " in flow pattern: " + flowPatternId);
        }
        return executionBlocks;
    }

    /**
     *
     * @param flowPatternId
     * @param testScenarioId
     * @param execBlockId
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public ExecutionBlock getExecutionBlock(String flowPatternId, String testScenarioId, String execBlockId) throws MasterConfigurationException {
        if (execBlockId.equals("")) {
            throw new MasterConfigurationException("Execution block reference id if empty in config.xml for:"
                    + "\nFlowPatternId: " + flowPatternId
                    + "\nTestScenarioId: " + testScenarioId
                    + "\nCheck config.xml file for selected interface and fill reference id for this block.");
        }
        for (ExecutionBlock executionBlock : getExecutionBlocks(flowPatternId, testScenarioId)) {
            if (executionBlock.getIdentificator().equals(execBlockId)) {
                return executionBlock;
            }
        }
        throw new MasterConfigurationException("Execption foudn in master configuration file. '" + execBlockId + "' ExecutionBlockId cannot be found for: "
                + "\nFlowPatternId: " + flowPatternId
                + "\nTestScenarioId: " + testScenarioId
                + "\n Looks like there is mismatch between identificators in master and config files.");
    }

    /**
     *
     * @param interfaceFlowPatternId
     * @param interfaceTestScenarioId
     * @param interfaceExecutionBlockId
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public List<ExecBlockOperation> getOperations(String interfaceFlowPatternId, String interfaceTestScenarioId, String interfaceExecutionBlockId) throws MasterConfigurationException {
        ExecutionBlock executionBlock = this.getExecutionBlock(interfaceFlowPatternId, interfaceTestScenarioId, interfaceExecutionBlockId);
        return executionBlock.getOperation();
    }

    /**
     *
     * @param interfaceFlowPatternId
     * @param interfaceTestScenarioId
     * @param interfaceExecutionBlockId
     * @param operationName
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public Operation getOperation(String interfaceFlowPatternId, String interfaceTestScenarioId, String interfaceExecutionBlockId, String operationName) throws MasterConfigurationException {
        for (Operation operation : getOperations(interfaceFlowPatternId, interfaceTestScenarioId, interfaceExecutionBlockId)) {
            if (operation.getName().name().equals(operationName)) {
                return operation;
            }
        }
        throw new MasterConfigurationException("No such operation found: " + operationName + " in test scenario: " + interfaceTestScenarioId
                + " in flow pattern: " + interfaceFlowPatternId + " in execution block: " + interfaceExecutionBlockId);
    }

    File getInterfaceConfigFile(String interfaceName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @return
     */
    public SOATestingFrameworkMasterConfiguration getXmlConfig() {
        return XML_CONFIG;
    }

    /**
     *
     * @return @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public List<OsbReportingInstance> getOsbReportingInstanceInstances() throws MasterConfigurationException {
        if (XML_CONFIG.getEnvironments().getOsbDatabaseReporting().getOsbReportingInstance().isEmpty()) {
            throw new MasterConfigurationException("There are is no OSB Database Reporting instances configured.");
        }
        return XML_CONFIG.getEnvironments().getOsbDatabaseReporting().getOsbReportingInstance();
    }
    
    /**
     *
     * @return @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public OSBReporting getOsbReportingInstance() throws MasterConfigurationException {
        if (XML_CONFIG.getEnvironments().getOsbDatabaseReporting() == null) {
            throw new MasterConfigurationException("There are is no OSB Reporting instances configured.");
        }
        return XML_CONFIG.getEnvironments().getOsbDatabaseReporting();
    }    

    /**
     *
     * @param environment
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public OsbReportingInstance getOSBReportingInstance(String environment) throws MasterConfigurationException {
        for (OsbReportingInstance inst : getOsbReportingInstanceInstances()) {
            if (inst.getEnvironment().equals(environment)) {
                return inst;
            }
        }
        throw new MasterConfigurationException("OSB reporting instance configuration not found for environment " + environment);
    }

    /**
     *
     * @param ifaceName
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public String getInterfaceDirName(String ifaceName) throws MasterConfigurationException {
        try {
            return ifaceName + "_" + MFC.getValidFileSystemObjectName(getInterface(ifaceName).getDescription());
        } catch (FrameworkConfigurationException ex) {
            throw new MasterConfigurationException(ex);
        }
    }

    /**
     *
     * @param ifaceName
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public File getIfaceDir(String ifaceName) throws MasterConfigurationException {
        return new File(MFC.getSoaTestHome(), getInterfaceDirName(ifaceName));
    }

    /**
     *
     * @param ifaceName
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public File getIfaceConfigFile(String ifaceName) throws MasterConfigurationException {
        return new File(getIfaceDir(ifaceName), IFACE_CONFIG_FILENAME);
    }

    /**
     *This method is used to check whether <code>ICFG</code> map contains any value or not. If it is 
     * empty then it instantiate <code>InterfaceConfiguration</code> Class by passing Interface specific 
     * configuration file location, <code>MasterFrameowrk</code> object , <code>MasterConfiguration</code> 
     * object reference as an argument. And calls <code>InterfaceConfiguration.init()</code> method.
     * Then it populates the map with ifaceName and instantiated InterfaceConfiguration object.
     * @param ifaceName
     * @return <code>InterfaceConfiguration</code>
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public InterfaceConfiguration getInterfaceConfig(String ifaceName) throws MasterConfigurationException {
        if (!ICFG.containsKey(ifaceName)) {
            try {
                InterfaceConfiguration ifaceConfig = new InterfaceConfiguration(getIfaceConfigFile(ifaceName), ConfigurationManager.getInstance().getFrameworkConfig(), this);
                ifaceConfig.init();
                ICFG.put(ifaceName, ifaceConfig);
            } catch (FrameworkConfigurationException ex) {
                throw new MasterConfigurationException(ex);
            }
        }
        return ICFG.get(ifaceName);
    }

    /**
     *
     * @param interfaceObj
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public String getInterfaceDirName(Interface interfaceObj) throws MasterConfigurationException {
        try {
            return interfaceObj.getName() + "_" + MFC.getValidFileSystemObjectName(interfaceObj.getDescription());
        } catch (FrameworkConfigurationException ex) {
            throw new MasterConfigurationException(ex);
        }
    }

    /**
     *
     * @param interfaceObj
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public File getIfaceDir(Interface interfaceObj) throws MasterConfigurationException {
        return new File(MFC.getSoaTestHome(), getInterfaceDirName(interfaceObj));
    }

    /**
     *
     * @param interfaceObj
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public File getIfaceConfigFile(Interface interfaceObj) throws MasterConfigurationException {
        return new File(getIfaceDir(interfaceObj), IFACE_CONFIG_FILENAME);
    }

    /**
     *
     * @param interfaceObj
     * @return
     * @throws InterfaceConfigurationException
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public InterfaceConfiguration getInterfaceConfig(Interface interfaceObj) throws MasterConfigurationException, InterfaceConfigurationException {
        if (!ICFG2.containsKey(interfaceObj)) {
            InterfaceConfiguration ifaceConfig = new InterfaceConfiguration(getIfaceConfigFile(interfaceObj), ConfigurationManager.getInstance().getFrameworkConfig(), this);
            ifaceConfig.init();
            ICFG2.put(interfaceObj, ifaceConfig);
        }
        return ICFG2.get(interfaceObj);
    }

    /**
     * Returns the parent of the specified JAXB node.
     *
     * @param <T> expected return type, should be JAXB type
     * @param jaxbNode node we want to find the parent for
     * @param c class of the JAXB element we expect to be returned
     * @return returns the parent of the <code>jaxbNode</code> that is the type
     * of <code>T</code> or null
     */
    public <T> T getParent(Object jaxbNode, Class<T> c) {
        Node xmlNode = binder.getXMLNode(jaxbNode);
        if (xmlNode == null) {
            return null;
        }
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        Object retVal = null;
        try {
            Node parent = (Node) xpath.evaluate("..", xmlNode, XPathConstants.NODE);
            retVal = binder.getJAXBNode(parent);
            return c.cast(retVal);
        } catch (XPathExpressionException ex) {
            String msg = "Error during XPath query for the JAXB node's parent";
            logger.error(msg);
        } catch (ClassCastException ex) {
            String type = retVal == null ? "?" : retVal.getClass().toString();
            String msg = "Cannot cast parent of type " + type + " to the expected type of " + c.getClass();
            logger.error(msg);
        }
        return null;
    }

    /**
     *
     * @return
     */
    public Document getDOM() {
        return DOM;
    }

    /**
     *
     * @return
     */
    public Document getNoNamespaceDOM() {
        return noNamespaceDOM;
    }

    /**
     *
     * @param flowPatternId
     * @param testScenarioId
     * @return
     * @throws MasterConfigurationException
     */
    public TestScenarioPreOrPostExecutionBlock getPreExecutionBlock(String flowPatternId, String testScenarioId) throws MasterConfigurationException {
        return getTestScenario(flowPatternId, testScenarioId).getPreExecutionBlock();
    }

    /**
     *
     * @param flowPatternId
     * @param testScenarioId
     * @return
     * @throws MasterConfigurationException
     */
    public TestScenarioPreOrPostExecutionBlock getPostExecutionBlock(String flowPatternId, String testScenarioId) throws MasterConfigurationException {
        return getTestScenario(flowPatternId, testScenarioId).getPostExecutionBlock();
    }

    public List<String> getMasterDatabaseInstanceEnvironments(String dbId) throws MasterConfigurationException {
        String xPathExpr = "//*/environments/databases/database[@identificator='" + dbId + "']/databaseInstance/@environment";
        NodeList nodeList = getNodeList(xPathExpr, DOM);
        List<String> list = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            list.add(nodeList.item(i).getNodeValue());
        }
        return list;
    }

    public List<String> getMasterFusionMiddlewareInstanceEnvironments() throws MasterConfigurationException {
        String xPathExpr = "//*/environments/oracleFusionMiddleware/oracleFusionMiddlewareInstance/@environment";
        NodeList nodeList = getNodeList(xPathExpr, DOM);
        List<String> list = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            list.add(nodeList.item(i).getNodeValue());
        }
        return list;
    }
    
    public List<String> getMasterFTPServerInstanceEnvironments(String ftpId) throws MasterConfigurationException {
        String xPathExpr = "//*/environments/ftpServers/ftpServer[@identificator='" + ftpId + "']/ftpServerInstance/@environment";
        NodeList nodeList = getNodeList(xPathExpr, DOM);
        List<String> list = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            list.add(nodeList.item(i).getNodeValue());
        }
        return list;
    }
    
    public List<String> getMasterOSBReportingInstanceEnvironments() throws MasterConfigurationException {
        String xPathExpr = "//*/environments/osbDatabaseReporting/osbReportingInstance/@environment";
        NodeList nodeList = getNodeList(xPathExpr, DOM);
        List<String> list = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            list.add(nodeList.item(i).getNodeValue());
        }
        return list;
    }

    public String getReportDirName() {
        return XML_CONFIG.getFileSystemStructure().getFlowPatternInstanceRoot().getReportDirectory();
    }
    
    public String getArchiveDirName() {
        return XML_CONFIG.getFileSystemStructure().getFlowPatternInstanceRoot().getArchiveDirectory();
    }
}

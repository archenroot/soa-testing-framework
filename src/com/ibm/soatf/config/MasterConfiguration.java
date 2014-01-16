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
import com.ibm.soatf.config.master.FTPServers.FtpServer;
import com.ibm.soatf.config.master.FTPServers.FtpServer.Directories;
import com.ibm.soatf.config.master.FTPServers.FtpServer.FtpServerInstance;
import com.ibm.soatf.config.master.FlowPattern;
import com.ibm.soatf.config.master.Interface;
import com.ibm.soatf.config.master.Interface.Patterns.ReferencedFlowPattern;
import com.ibm.soatf.config.master.OSBReporting.OsbReportingInstance;
import com.ibm.soatf.config.master.Operation;
import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance;
import com.ibm.soatf.config.master.Project;
import com.ibm.soatf.config.master.SOATestingFrameworkMasterConfiguration;
import com.ibm.soatf.config.master.TestScenario;
import com.ibm.soatf.config.master.TestScenario.ExecutionBlock;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class responsible for input/output operations between framework and master
 * configuration file and SOA Testing Framework.
 *
 * @author Ladislav Jech <archenroot@gmail.com>
 */
public final class MasterConfiguration {

    private static final Logger logger = LogManager.getLogger(MasterConfiguration.class.getName());

    private final MasterFrameworkConfig MFC;

    private SOATestingFrameworkMasterConfiguration XML_CONFIG;

    private Set<String> environments;

    private final Map<String, InterfaceConfiguration> ICFG = new HashMap<>();
    private final Map<Interface, InterfaceConfiguration> ICFG2 = new HashMap<>();

    MasterConfiguration(final MasterFrameworkConfig mfc) {
        MFC = mfc;
    }

    /**
     * Initialize master configuraiton XML file unmarshaller. Check if the file
     * exists and unmarshall master configuration into object.
     *
     * @throws FrameworkConfigurationException
     */
    void init() throws FrameworkConfigurationException {
        logger.info("Unmarshalling master configuration from file: " + MFC.getMasterConfigFile());
        JAXBContext jaxbContext;
        Unmarshaller jaxbUnmarshaller;
        try {
            jaxbContext = JAXBContext.newInstance("com.ibm.soatf.config.master");
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            XML_CONFIG = ((JAXBElement<SOATestingFrameworkMasterConfiguration>) jaxbUnmarshaller.unmarshal(MFC.getMasterConfigFile())).getValue();
        } catch (JAXBException jbex) {
            throw new FrameworkConfigurationException("Error while unmarshalling master configuration object from XML file " + MFC.getMasterConfigFile(), jbex);
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
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            try {
                builder = factory.newDocumentBuilder();
                Document doc = builder.parse(MFC.getMasterConfigFile());
                XPathFactory xPathfactory = XPathFactory.newInstance();
                XPath xpath = xPathfactory.newXPath();
                NodeList list = (NodeList) xpath.evaluate("//*/@environment", doc, XPathConstants.NODESET);
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
            } catch (ParserConfigurationException ex) {
                String msg = "Error while attempting to parse master configuration for XPath queries.";
                logger.error(msg);
                throw new MasterConfigurationException(ex);
            } catch (SAXException | IOException | XPathExpressionException ex) {
                String msg = "Error while attempting to parse master configuration for XPath queries.";
                logger.error(msg, ex);
                throw new MasterConfigurationException(ex);
            }
        }
        return environments;
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
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException
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
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException @throws FrameworkConfigurationException
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
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException @throws FrameworkConfigurationException
     */
    public List<FtpServer> getFTPServers() throws MasterConfigurationException {
        if (XML_CONFIG.getEnvironments().getFtpServers().getFtpServer().isEmpty()) {
            throw new MasterConfigurationException("There are is FTP servers configuration.");
        }
        return XML_CONFIG.getEnvironments().getFtpServers().getFtpServer();
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
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException @throws FrameworkConfigurationException
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
    public List<Operation> getOperations(String interfaceFlowPatternId, String interfaceTestScenarioId, String interfaceExecutionBlockId) throws MasterConfigurationException {
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
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public List<OsbReportingInstance> getOsbReportingInstanceInstances() throws MasterConfigurationException {
        if (XML_CONFIG.getEnvironments().getOsbDatabaseReporting().getOsbReportingInstance().isEmpty()) {
            throw new MasterConfigurationException("There are is no OSB Database Reporting instances configured.");
        }
        return XML_CONFIG.getEnvironments().getOsbDatabaseReporting().getOsbReportingInstance();
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
     *
     * @param ifaceName
     * @return
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
        return new File(MFC.getSoaTestHome(), getInterfaceDirName(interfaceObj.getName()));
    }

    /**
     *
     * @param interfaceObj
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public File getIfaceConfigFile(Interface interfaceObj) throws MasterConfigurationException {
        return new File(getIfaceDir(interfaceObj.getName()), IFACE_CONFIG_FILENAME);
    }

    /**
     *
     * @param interfaceObj
     * @return
     * @throws com.ibm.soatf.config.MasterConfigurationException
     */
    public InterfaceConfiguration getInterfaceConfig(Interface interfaceObj) throws MasterConfigurationException {
        if (!ICFG2.containsKey(interfaceObj)) {
            try {
                InterfaceConfiguration ifaceConfig = new InterfaceConfiguration(getIfaceConfigFile(interfaceObj), ConfigurationManager.getInstance().getFrameworkConfig(), this);
                ifaceConfig.init();
                ICFG2.put(interfaceObj, ifaceConfig);
            } catch (FrameworkConfigurationException ex) {
                throw new MasterConfigurationException(ex);
            }
        }
        return ICFG2.get(interfaceObj);
    }
}

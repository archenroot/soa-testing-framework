/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.soatf.config;

import static com.ibm.soatf.config.FrameworkConfiguration.IFACE_CONFIG_FILENAME;
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
 *
 * @author zANGETSu
 */
public final class MasterConfiguration {

    private static final Logger logger = LogManager.getLogger(MasterConfiguration.class.getName());

    private final FrameworkConfiguration FCFG;

    private SOATestingFrameworkMasterConfiguration XML_CONFIG;
    
    private Set<String> environments;
    
    private final Map<String, InterfaceConfiguration> ICFG = new HashMap<>();
    private final Map<Interface, InterfaceConfiguration> ICFG2 = new HashMap<>();

    MasterConfiguration(FrameworkConfiguration fcfg) {
        FCFG = fcfg;
    }

    /**
     *
     * @return @throws FrameworkConfigurationException
     */
    void init() throws FrameworkConfigurationException {
        logger.info("Unmarshalling master configuration from file: " + FCFG.getMasterConfigFile());
        JAXBContext jaxbContext;
        Unmarshaller jaxbUnmarshaller;
        try {
            jaxbContext = JAXBContext.newInstance("com.ibm.soatf.config.master");
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            XML_CONFIG = ((JAXBElement<SOATestingFrameworkMasterConfiguration>) jaxbUnmarshaller.unmarshal(FCFG.getMasterConfigFile())).getValue();
        } catch (JAXBException jbex) {
            throw new FrameworkConfigurationException("Error while unmarshalling master configuration object from XML file " + FCFG.getMasterConfigFile(), jbex);
        }
    }
    
    public Set<String> getAllEnvironments() throws FrameworkConfigurationException {
        if (environments == null) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            try {
                builder = factory.newDocumentBuilder();
                Document doc = builder.parse(FCFG.getMasterConfigFile());
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
                throw new FrameworkConfigurationException(ex);
            } catch (SAXException | IOException | XPathExpressionException ex) {
                String msg = "Error while attempting to parse master configuration for XPath queries.";
                logger.error(msg, ex);
                throw new FrameworkConfigurationException(ex);
            }
        }
        return environments;
    }
    
    public List<Interface> getInterfaces() {
        final List<Interface> iface = XML_CONFIG.getInterfaces().getInterface();
        if (iface.isEmpty()) {
            logger.warn("There are no interfaces defined in master configuration XML file.");
        }
        return iface;
    }

    public List<String> getInterfaceNames() {
        List<String> interfaceNames = new ArrayList<>();
        for (Interface iface : getInterfaces()) {
            interfaceNames.add(iface.getName());
        }
        return interfaceNames;
    }

    /**
     *
     * @param interfaceName
     * @return
     * @throws FrameworkConfigurationException
     */
    public Interface getInterface(String interfaceName) throws FrameworkConfigurationException {
        for (Interface iface : getInterfaces()) {
            if (iface.getName().equals(interfaceName)) {
                return iface;
            }
        }
        throw new FrameworkConfigurationException("Interface with following identificator cannot be found in master configuration: " + interfaceName);
    }

    /**
     *
     * @param interfaceName
     * @return
     * @throws FrameworkConfigurationException
     */
    public List<Project> getProjects(String interfaceName) throws FrameworkConfigurationException {
        final List<Project> projects = getInterface(interfaceName).getProjects().getProject();
        if (projects.isEmpty()) {
            throw new FrameworkConfigurationException("There are no configured projects for interface " + interfaceName + " in master configuration XML file.");
        }
        return projects;
    }

    /**
     *
     * @param interfaceName
     * @param projectName
     * @return
     */
    public Project getProject(String interfaceName, String projectName) {
        throw new UnsupportedOperationException("Not supported yet.");

    }

    /**
     *
     * @param interfaceName
     * @return
     * @throws FrameworkConfigurationException
     */
    public List<ReferencedFlowPattern> getReferencedFlowPatterns(String interfaceName) throws FrameworkConfigurationException {
        final List<ReferencedFlowPattern> referencedFlowPatterns = getInterface(interfaceName).getPatterns().getReferencedFlowPattern();
        if (referencedFlowPatterns.isEmpty()) {
            throw new FrameworkConfigurationException("There are no configured referenced flow patterns for interface " + interfaceName + ".");
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
     * @return @throws FrameworkConfigurationException
     */
    public List<OracleFusionMiddlewareInstance> getOracleFusionMiddlewareInstances() throws FrameworkConfigurationException {
        if (XML_CONFIG.getEnvironments().getOracleFusionMiddleware().getOracleFusionMiddlewareInstance().isEmpty()) {
            throw new FrameworkConfigurationException("There are no Oracle Fusion Middleware instances configured.");
        }
        return XML_CONFIG.getEnvironments().getOracleFusionMiddleware().getOracleFusionMiddlewareInstance();
    }

    /**
     *
     * @param environment
     * @return
     * @throws FrameworkConfigurationException
     */
    public OracleFusionMiddlewareInstance getOracleFusionMiddlewareInstance(String environment) throws FrameworkConfigurationException {
        for (OracleFusionMiddlewareInstance inst : getOracleFusionMiddlewareInstances()) {
            if (inst.getEnvironment().equals(environment)) {
                return inst;
            }
        }
        throw new FrameworkConfigurationException("Oracle Fusion Middleware instance configuration not found for environment " + environment);
    }

    /**
     *
     * @return @throws FrameworkConfigurationException
     */
    public List<Database> getDatabases() throws FrameworkConfigurationException {
        if (XML_CONFIG.getEnvironments().getDatabases().getDatabase().isEmpty()) {
            throw new FrameworkConfigurationException("There are is no database configured.");
        }
        return XML_CONFIG.getEnvironments().getDatabases().getDatabase();
    }

    /**
     *
     * @param identificator
     * @return
     * @throws FrameworkConfigurationException
     */
    public Database getDatabase(String identificator) throws FrameworkConfigurationException {
        for (Database database : getDatabases()) {
            if (database.getIdentificator().equals(identificator)) {
                return database;
            }
        }
        throw new FrameworkConfigurationException("Database configuration with identificator " + identificator + " cannot be found.");
    }

    /**
     *
     * @param identificator
     * @return
     * @throws FrameworkConfigurationException
     */
    public List<DatabaseInstance> getDatabaseInstances(String identificator) throws FrameworkConfigurationException {
        if (getDatabase(identificator).getDatabaseInstance().isEmpty()) {
            throw new FrameworkConfigurationException("There are no database instances configured for database environment identificator " + identificator + ".");
        }
        return getDatabase(identificator).getDatabaseInstance();
    }

    /**
     *
     * @param environment
     * @param identificator
     * @return
     * @throws FrameworkConfigurationException
     */
    public DatabaseInstance getDatabaseInstance(String environment, String identificator) throws FrameworkConfigurationException {
        for (DatabaseInstance inst : getDatabaseInstances(identificator)) {
            if (inst.getEnvironment().equals(environment)) {
                return inst;
            }
        }
        throw new FrameworkConfigurationException("Database configuration with identificator " + identificator + " for environment " + environment + " cannot be found.");
    }

    /**
     *
     * @return @throws FrameworkConfigurationException
     */
    public List<FtpServer> getFTPServers() throws FrameworkConfigurationException {
        if (XML_CONFIG.getEnvironments().getFtpServers().getFtpServer().isEmpty()) {
            throw new FrameworkConfigurationException("There are is FTP servers configuration.");
        }
        return XML_CONFIG.getEnvironments().getFtpServers().getFtpServer();
    }

    /**
     *
     * @param identificator
     * @return
     * @throws FrameworkConfigurationException
     */
    public FtpServer getFTPServer(String identificator) throws FrameworkConfigurationException {
        for (FtpServer ftpServer : getFTPServers()) {
            if (ftpServer.getIdentificator().equals(identificator)) {
                return ftpServer;
            }
        }
        throw new FrameworkConfigurationException("FTP server configuration with identificator " + identificator + " cannot be found.");
    }

    public Directories getFTPServerDirectories(String identificator) throws FrameworkConfigurationException {
        
        Directories directories = getFTPServer(identificator).getDirectories();
        if (directories == null ){
            throw new FrameworkConfigurationException();
        }
        return directories;
    }
    
    /**
     *
     * @param identificator
     * @return
     * @throws FrameworkConfigurationException
     */
    public List<FtpServerInstance> getFTPServerInstances(String identificator) throws FrameworkConfigurationException {
        if (getFTPServer(identificator).getFtpServerInstance().isEmpty()) {
            throw new FrameworkConfigurationException("There are no ftp server instances configured for database environment identificator " + identificator + ".");
        }
        return getFTPServer(identificator).getFtpServerInstance();
    }

    /**
     *
     * @param environment
     * @param identificator
     * @return
     * @throws FrameworkConfigurationException
     */
    public FtpServerInstance getFtpServerInstance(String environment, String identificator) throws FrameworkConfigurationException {
        for (FtpServerInstance inst : getFTPServerInstances(identificator)) {
            if (inst.getEnvironment().equals(environment)) {
                return inst;
            }
        }
        throw new FrameworkConfigurationException("FTP instance configuration with identificator " + identificator + " for environment " + environment + " cannot be found.");
    }

    /**
     *
     * @return @throws FrameworkConfigurationException
     */
    public List<FlowPattern> getFlowPatterns() throws FrameworkConfigurationException {
        if (XML_CONFIG.getFlowPatterns().getFlowPattern().isEmpty()) {
            throw new FrameworkConfigurationException("There are no flow pattern definitions available in the configuration file.");
        }
        return XML_CONFIG.getFlowPatterns().getFlowPattern();
    }

    public FlowPattern getFlowPattern(String flowPatternId) throws FrameworkConfigurationException {
        for (FlowPattern flowPattern : getFlowPatterns()) {
            if (flowPattern.getIdentificator().equals(flowPatternId)) {
                return flowPattern;
            }
        }
        throw new FrameworkConfigurationException("No such flow pattern found: " + flowPatternId);
    }

    public List<TestScenario> getTestScenarios(String flowPatternId) throws FrameworkConfigurationException {
        final List<TestScenario> testScenarios = getFlowPattern(flowPatternId).getTestScenario();
        if (testScenarios.isEmpty()) {
            throw new FrameworkConfigurationException("No test scenario found in flow pattern: " + flowPatternId);
        }
        return testScenarios;
    }

    public TestScenario getTestScenario(String flowPatternId, String testScenarioId) throws FrameworkConfigurationException {
        for (TestScenario testScenario : getTestScenarios(flowPatternId)) {
            if (testScenario.getIdentificator().equals(testScenarioId)) {
                return testScenario;
            }
        }
        throw new FrameworkConfigurationException("No such test scenario found: " + testScenarioId + " in flow pattern: " + flowPatternId);
    }

    public List<ExecutionBlock> getExecutionBlocks(String flowPatternId, String testScenarioId) throws FrameworkConfigurationException {
        final List<ExecutionBlock> executionBlocks = getTestScenario(flowPatternId, testScenarioId).getExecutionBlock();
        if (executionBlocks.isEmpty()) {
            throw new FrameworkConfigurationException("No execution blocks found in test scenario: " + testScenarioId
                    + " in flow pattern: " + flowPatternId);
        }
        return executionBlocks;
    }

    public ExecutionBlock getExecutionBlock(String flowPatternId, String testScenarioId, String execBlockId) throws FrameworkConfigurationException {
        if (execBlockId.equals("")) {
            throw new FrameworkConfigurationException("Execution block reference id if empty in config.xml for:"
                    + "\nFlowPatternId: " + flowPatternId
                    + "\nTestScenarioId: " + testScenarioId
                    + "\nCheck config.xml file for selected interface and fill reference id for this block.");
        }
        for (ExecutionBlock executionBlock : getExecutionBlocks(flowPatternId, testScenarioId)) {
            if (executionBlock.getIdentificator().equals(execBlockId)) {
                return executionBlock;
            }
        }
        throw new FrameworkConfigurationException("'" + execBlockId + "' ExecutionBlockId cannot be found for: "
                + "\nFlowPatternId: " + flowPatternId
                + "\nTestScenarioId: " + testScenarioId
                + "\n Looks like there is mismatch between identificators in master and config files.");
    }

    public List<Operation> getOperations(String interfaceFlowPatternId, String interfaceTestScenarioId, String interfaceExecutionBlockId) throws FrameworkConfigurationException {
        ExecutionBlock executionBlock = this.getExecutionBlock(interfaceFlowPatternId, interfaceTestScenarioId, interfaceExecutionBlockId);
        return executionBlock.getOperation();
    }

    public Operation getOperation(String interfaceFlowPatternId, String interfaceTestScenarioId, String interfaceExecutionBlockId, String operationName) throws FrameworkConfigurationException {
        for (Operation operation : getOperations(interfaceFlowPatternId, interfaceTestScenarioId, interfaceExecutionBlockId)) {
            if (operation.getName().name().equals(operationName)) {
                return operation;
            }
        }
        throw new FrameworkConfigurationException("No such operation found: " + operationName + " in test scenario: " + interfaceTestScenarioId
                + " in flow pattern: " + interfaceFlowPatternId + " in execution block: " + interfaceExecutionBlockId);
    }

    File getInterfaceConfigFile(String interfaceName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public SOATestingFrameworkMasterConfiguration getXmlConfig() {
        return XML_CONFIG;
    }

    public List<OsbReportingInstance> getOsbReportingInstanceInstances() throws FrameworkConfigurationException {
        if (XML_CONFIG.getEnvironments().getOsbDatabaseReporting().getOsbReportingInstance().isEmpty()) {
            throw new FrameworkConfigurationException("There are is no OSB Database Reporting instances configured.");
        }
        return XML_CONFIG.getEnvironments().getOsbDatabaseReporting().getOsbReportingInstance();
    }

    /**
     *
     * @param environment
     * @return
     * @throws FrameworkConfigurationException
     */
    public OsbReportingInstance getOSBReportingInstance(String environment) throws FrameworkConfigurationException {
        for (OsbReportingInstance inst : getOsbReportingInstanceInstances()) {
            if (inst.getEnvironment().equals(environment)) {
                return inst;
            }
        }
        throw new FrameworkConfigurationException("OSB reporting instance configuration not found for environment " + environment);
    }
    
    public String getInterfaceDirName(String ifaceName) throws FrameworkConfigurationException {
        return ifaceName + "_" + FCFG.getValidFileSystemObjectName(getInterface(ifaceName).getDescription());
    }

    public File getIfaceDir(String ifaceName) throws FrameworkConfigurationException {
        return new File(FCFG.getSoaTestHome(), getInterfaceDirName(ifaceName));
    }

    public File getIfaceConfigFile(String ifaceName) throws FrameworkConfigurationException {
        return new File(getIfaceDir(ifaceName), IFACE_CONFIG_FILENAME);
    }
    
    public InterfaceConfiguration getInterfaceConfig(String ifaceName) throws FrameworkConfigurationException {
        if (!ICFG.containsKey(ifaceName)) {
            InterfaceConfiguration ifaceConfig = new InterfaceConfiguration(getIfaceConfigFile(ifaceName), ConfigurationManager.getInstance().getFrameworkConfig(), this);
            ifaceConfig.init();
            ICFG.put(ifaceName, ifaceConfig);
        }
        return ICFG.get(ifaceName);
    }
    
    public String getInterfaceDirName(Interface interfaceObj) throws FrameworkConfigurationException {
        return interfaceObj.getName() + "_" + FCFG.getValidFileSystemObjectName(interfaceObj.getDescription());
    }

    public File getIfaceDir(Interface interfaceObj) throws FrameworkConfigurationException {
        return new File(FCFG.getSoaTestHome(), getInterfaceDirName(interfaceObj.getName()));
    }

    public File getIfaceConfigFile(Interface interfaceObj) throws FrameworkConfigurationException {
        return new File(getIfaceDir(interfaceObj.getName()), IFACE_CONFIG_FILENAME);
    }

    public InterfaceConfiguration getInterfaceConfig(Interface interfaceObj) throws FrameworkConfigurationException {
        if (!ICFG2.containsKey(interfaceObj)) {
            InterfaceConfiguration ifaceConfig = new InterfaceConfiguration(getIfaceConfigFile(interfaceObj), ConfigurationManager.getInstance().getFrameworkConfig(), this);
            ifaceConfig.init();
            ICFG2.put(interfaceObj, ifaceConfig);
        }
        return ICFG2.get(interfaceObj);
    }
}

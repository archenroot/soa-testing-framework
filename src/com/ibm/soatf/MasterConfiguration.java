/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.soatf;

import com.ibm.soatf.config.master.Databases.Database;
import com.ibm.soatf.config.master.Databases.Database.DatabaseInstance;
import com.ibm.soatf.config.master.EnvironmentType;
import com.ibm.soatf.config.master.ExecutionBlock;
import com.ibm.soatf.config.master.FTPServers.FtpServer;
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
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public final class MasterConfiguration {

    private static final Logger logger = LogManager.getLogger(MasterConfiguration.class.getName());

    private static MasterConfiguration instance;
    private static final FrameworkConfiguration FCFG = FrameworkConfiguration.getInstance();

    private static final SOATestingFrameworkMasterConfiguration XML_CONFIG;

    static {
        try {
            XML_CONFIG = unmarshall();
        } catch (FrameworkConfigurationException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private MasterConfiguration() {

    }

    public static synchronized MasterConfiguration getInstance() {
        if (instance == null) {
            instance = new MasterConfiguration();
        }
        return instance;
    }

    /**
     *
     * @return @throws FrameworkConfigurationException
     */
    private static SOATestingFrameworkMasterConfiguration unmarshall() {
        if (!FCFG.SOATF_CONFIG_FILE.exists()) {
            String msg = "Master configuration file expected in '" + FCFG.SOATF_CONFIG_FILE + "' could not be found. Please ensure that the SOA_TEST_HOME environment variable is pointing to the correct soa_test directory.";
            logger.fatal(msg);
            throw new FrameworkConfigurationException(msg);
        }
        logger.info("Unmarshalling master configuration from file: " + FCFG.SOATF_CONFIG_FILE);
        JAXBContext jaxbContext;
        Unmarshaller jaxbUnmarshaller;
        try {
            jaxbContext = JAXBContext.newInstance("com.ibm.soatf.config.master");
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return ((JAXBElement<SOATestingFrameworkMasterConfiguration>) jaxbUnmarshaller.unmarshal(FCFG.SOATF_CONFIG_FILE)).getValue();
        } catch (JAXBException jbex) {
            throw new FrameworkConfigurationException("Error while unmarshalling master configuration object from XML file " + FCFG.SOATF_CONFIG_FILE, jbex);
        } finally {
            jaxbContext = null;
            jaxbUnmarshaller = null;
        }
    }

    /**
     *
     * @return @throws FrameworkConfigurationException
     */
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
    public Interface getInterface(String interfaceName) {
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
    public List<Project> getProjects(String interfaceName) {
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
    public List<ReferencedFlowPattern> getReferencedFlowPatterns(String interfaceName) {
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
    public List<OracleFusionMiddlewareInstance> getOracleFusionMiddlewareInstances() {
        if (XML_CONFIG.getEnvironments().getOracleFusionMiddleware().getOracleFusionMiddlewareInstance().isEmpty()) {
            throw new FrameworkConfigurationException("There are is no Oracle Fusion Middleware instances configured.");
        }
        return XML_CONFIG.getEnvironments().getOracleFusionMiddleware().getOracleFusionMiddlewareInstance();
    }

    /**
     *
     * @param environmentIdentificator
     * @return
     * @throws FrameworkConfigurationException
     */
    public OracleFusionMiddlewareInstance getOracleFusionMiddlewareInstance(String environmentIdentificator) {
        ListIterator<OracleFusionMiddlewareInstance> oracleFusionMiddlewareInstanceIterator = getOracleFusionMiddlewareInstances().listIterator();
        OracleFusionMiddlewareInstance oracleFusionMiddlewareInstance = null;
        boolean found = false;
        while (oracleFusionMiddlewareInstanceIterator.hasNext()) {
            oracleFusionMiddlewareInstance = oracleFusionMiddlewareInstanceIterator.next();
            if (getOFMEnvironmentRealIdentificator(oracleFusionMiddlewareInstance).equals(environmentIdentificator)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new FrameworkConfigurationException("Oracle Fusion Middleware instance configuration not found for environemtn " + environmentIdentificator);
        }
        return oracleFusionMiddlewareInstance;
    }

    /**
     *
     * @param instance
     * @return
     * @throws FrameworkConfigurationException
     */
    public String getOFMEnvironmentRealIdentificator(OracleFusionMiddlewareInstance instance) {
        if (instance.getEnvironmentType().equals(EnvironmentType.SYSTEM)) {
            return instance.getEnvironment().value();
        } else if (instance.getEnvironmentType().equals(EnvironmentType.USER)) {
            return instance.getName();
        } else {
            throw new FrameworkConfigurationException("Oracle Fusion Middleware instance configuration is not of any supported type which are " + EnvironmentType.values().toString());
        }
    }

    /**
     *
     * @return @throws FrameworkConfigurationException
     */
    public List<Database> getDatabases() {
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
    public Database getDatabase(String identificator) {
        ListIterator<Database> databases = getDatabases().listIterator();
        Database database = null;
        boolean found = false;
        while (databases.hasNext()) {
            database = databases.next();
            if (database.getIdentificator().equals(identificator)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new FrameworkConfigurationException("Database configuration with identificator " + identificator + " cannot be found.");
        }
        return database;
    }

    /**
     *
     * @param identificator
     * @return
     * @throws FrameworkConfigurationException
     */
    public List<DatabaseInstance> getDatabaseInstances(String identificator) {
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
    public DatabaseInstance getDatabaseInstance(String environment, String identificator) {
        ListIterator<DatabaseInstance> databaseInstancesIterator = getDatabaseInstances(identificator).listIterator();
        DatabaseInstance databaseInstance = null;
        boolean found = false;
        while (databaseInstancesIterator.hasNext()) {
            databaseInstance = databaseInstancesIterator.next();
            if (getDatabaseEnvironmentRealIdentificator(databaseInstance).equals(environment)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new FrameworkConfigurationException("Database configuration with identificator " + identificator + " for environment " + environment + " cannot be found.");
        }
        return databaseInstance;
    }

    /**
     *
     * @param instance
     * @return
     * @throws FrameworkConfigurationException
     */
    public String getDatabaseEnvironmentRealIdentificator(DatabaseInstance instance) {
        if (instance.getEnvironmentType().equals(EnvironmentType.SYSTEM)) {
            return instance.getEnvironment().value();
        } else if (instance.getEnvironmentType().equals(EnvironmentType.USER)) {
            return instance.getName();
        } else {
            throw new FrameworkConfigurationException("Database instance configuration is not of any supported type which are " + EnvironmentType.values().toString() + ".");
        }
    }

    /**
     *
     * @return @throws FrameworkConfigurationException
     */
    public List<FtpServer> getFTPServers() {
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
    public FtpServer getFTPServer(String identificator) {
        ListIterator<FtpServer> ftpServers = getFTPServers().listIterator();
        FtpServer ftpServer = null;
        boolean found = false;
        while (ftpServers.hasNext()) {
            ftpServer = ftpServers.next();
            if (ftpServer.getIdentificator().equals(identificator)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new FrameworkConfigurationException("FTP server configuration with identificator " + identificator + " cannot be found.");
        }
        return ftpServer;
    }

    /**
     *
     * @param identificator
     * @return
     * @throws FrameworkConfigurationException
     */
    public List<FtpServerInstance> getFTPServerInstances(String identificator) {
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
    public FtpServerInstance getFtpServerInstance(String environment, String identificator) {
        ListIterator<FtpServerInstance> ftpServerInstancesIterator = getFTPServerInstances(identificator).listIterator();
        FtpServerInstance ftpServerInstance = null;
        boolean found = false;
        while (ftpServerInstancesIterator.hasNext()) {
            ftpServerInstance = ftpServerInstancesIterator.next();
            if (getFTPServerEnvironmentRealIdentificator(ftpServerInstance).equals(environment)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new FrameworkConfigurationException("FTP instance configuration with identificator " + identificator + " for environment " + environment + " cannot be found.");
        }
        return ftpServerInstance;
    }

    /**
     *
     * @param instance
     * @return
     * @throws FrameworkConfigurationException
     */
    public String getFTPServerEnvironmentRealIdentificator(FtpServerInstance instance) {
        if (instance.getEnvironmentType().equals(EnvironmentType.SYSTEM)) {
            return instance.getEnvironment().value();
        } else if (instance.getEnvironmentType().equals(EnvironmentType.USER)) {
            return instance.getName();
        } else {
            throw new FrameworkConfigurationException("FTP server instance configuration is not of any supported type which are " + EnvironmentType.values().toString() + ".");
        }
    }

    /**
     *
     * @return @throws FrameworkConfigurationException
     */
    public List<FlowPattern> getFlowPatterns() {
        if (XML_CONFIG.getFlowPatterns().getFlowPattern().isEmpty()) {
            throw new FrameworkConfigurationException("There are no flow pattern definitions available in the configuration file.");
        }
        return XML_CONFIG.getFlowPatterns().getFlowPattern();
    }

    public FlowPattern getFlowPattern(String flowPatternId) {
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
        TestScenario interfaceTestScenario = null;
        try {
            for (TestScenario tsLocal : getTestScenarios(flowPatternId)) {
                if (tsLocal.getIdentificator().equals(testScenarioId)) {
                    interfaceTestScenario = tsLocal;
                    
                }
            }
            if (testScenarioId.equals("")) {
                throw new FrameworkConfigurationException("");
            }
            if (testScenarioId == null) {
                throw new IllegalArgumentException("");
            }
        } catch (IllegalArgumentException ex) {
            throw new FrameworkConfigurationException("No such test scenario found: " + testScenarioId + " in flow pattern: " + flowPatternId);
        }
        return interfaceTestScenario;
    }

    public List<ExecutionBlock> getExecutionBlocks(String flowPatternId, String testScenarioId) throws FrameworkConfigurationException {
        final List<ExecutionBlock> executionBlocks = getTestScenario(flowPatternId, testScenarioId).getExecutionBlock();
        if (executionBlocks.isEmpty()) {
            throw new FrameworkConfigurationException("No execution blocks found in test scenario: " + testScenarioId
                    + " in flow pattern: " + flowPatternId);
        }
        return executionBlocks;
    }

    public ExecutionBlock getExecutionBlock(String flowPatternId, String testScenarioId, String execBlockId) {
        for (ExecutionBlock executionBlock : getExecutionBlocks(flowPatternId, testScenarioId)) {
            if (executionBlock.getIdentificator().equals(execBlockId)) {
                return executionBlock;
            }
        }
        if (execBlockId.equals("")) {
            throw new FrameworkConfigurationException("Execution block reference id if empty in config.xml for:"
                    + "\nFlowPatternId: " + flowPatternId
                    + "\nTestScenarioId: " + testScenarioId
                    + "\nCheck config.xml file for selected interface and fill reference id for this block.");
        }
        throw new FrameworkConfigurationException("'" + execBlockId + "' ExecutionBlockId cannot be found for: "
                + "\nFlowPatternId: " + flowPatternId
                + "\nTestScenarioId: " + testScenarioId
                + "\n Looks like there is mismatch between identificators in master and config files.");
    }

    public List<Operation> getOperations(String interfaceFlowPatternId, String interfaceTestScenarioId, String interfaceExecutionBlockId) {
        ExecutionBlock executionBlock = this.getExecutionBlock(interfaceFlowPatternId, interfaceTestScenarioId, interfaceExecutionBlockId);
        return executionBlock.getOperation();
    }

    public Operation getOperation(String interfaceFlowPatternId, String interfaceTestScenarioId, String interfaceExecutionBlockId, String operationName) {
        for (Operation operation : getOperations(interfaceFlowPatternId, interfaceTestScenarioId, interfaceExecutionBlockId)) {
            if (operation.getName().equals(operationName)) {
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

    public List<OsbReportingInstance> getOsbReportingInstanceInstances() {
        if (XML_CONFIG.getEnvironments().getOsbDatabaseReporting().getOsbReportingInstance().isEmpty()) {
            throw new FrameworkConfigurationException("There are is no OSB Database Reporting instances configured.");
        }
        return XML_CONFIG.getEnvironments().getOsbDatabaseReporting().getOsbReportingInstance();
    }

    /**
     *
     * @param environmentIdentificator
     * @return
     * @throws FrameworkConfigurationException
     */
    public OsbReportingInstance getOSBReportingInstance(String environmentIdentificator) {
        ListIterator<OsbReportingInstance> osbReportingInstanceIt = getOsbReportingInstanceInstances().listIterator();
        OsbReportingInstance osbReportingInstance = null;
        boolean found = false;
        while (osbReportingInstanceIt.hasNext()) {
            osbReportingInstance = osbReportingInstanceIt.next();
            if (getOSBDatabaserReportingEnvRealIdentificator(osbReportingInstance).equals(environmentIdentificator)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new FrameworkConfigurationException("Oracle Fusion Middleware instance configuration not found for environemtn " + environmentIdentificator);
        }
        return osbReportingInstance;
    }

    public String getOSBDatabaserReportingEnvRealIdentificator(OsbReportingInstance osbReportingInstance) {
        if (osbReportingInstance.getEnvironmentType().equals(EnvironmentType.SYSTEM)) {
            return osbReportingInstance.getEnvironment().value();
        } else if (osbReportingInstance.getEnvironmentType().equals(EnvironmentType.USER)) {
            return osbReportingInstance.getName();
        } else {
            throw new FrameworkConfigurationException("OSB Database Reporting instance configuration is not of any supported type which are " + EnvironmentType.values().toString());
        }
    }
}

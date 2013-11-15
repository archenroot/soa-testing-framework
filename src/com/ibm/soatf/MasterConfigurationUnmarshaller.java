/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.soatf;

import static com.ibm.soatf.FrameworkConfiguration.SOATF_CONFIGURATION_FILE;
import com.ibm.soatf.config.master.Databases.Database;
import com.ibm.soatf.config.master.Databases.Database.DatabaseInstance;
import com.ibm.soatf.config.master.EnvironmentType;
import com.ibm.soatf.config.master.ExecutionBlock;
import com.ibm.soatf.config.master.FTPServers.FtpServer;
import com.ibm.soatf.config.master.FTPServers.FtpServer.FtpServerInstance;
import com.ibm.soatf.config.master.FlowPattern;
import com.ibm.soatf.config.master.Interface;
import com.ibm.soatf.config.master.Interface.Patterns.ReferencedFlowPattern;
import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance;
import com.ibm.soatf.config.master.Project;
import com.ibm.soatf.config.master.SOATestingFrameworkMasterConfiguration;
import com.ibm.soatf.config.master.TestScenario;
import java.io.File;
import java.util.List;
import java.util.ListIterator;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author zANGETSu
 */
public class MasterConfigurationUnmarshaller {

    /**
     *
     * @return
     * @throws FrameworkConfigurationException
     */
    public static SOATestingFrameworkMasterConfiguration getSOATestingFrameworkMasterConfiguration() throws FrameworkConfigurationException {
        JAXBContext jaxbContext;
        Unmarshaller jaxbUnmarshaller;
        try {
            jaxbContext = JAXBContext.newInstance("com.ibm.soatf.config.master");
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return ((JAXBElement<SOATestingFrameworkMasterConfiguration>) jaxbUnmarshaller.unmarshal(new File(SOATF_CONFIGURATION_FILE))).getValue();
        } catch (JAXBException jbex) {
            throw new FrameworkConfigurationException("Error while unmarshalling master configuration object from XML file " + SOATF_CONFIGURATION_FILE, jbex);
        } finally {
            jaxbContext = null;
            jaxbUnmarshaller = null;
        }
    }

    /**
     *
     * @return
     * @throws FrameworkConfigurationException
     */
    public static List<Interface> getInterfaces() throws FrameworkConfigurationException {
        if (getSOATestingFrameworkMasterConfiguration().getInterfaces().getInterface().isEmpty()) {
            throw new FrameworkConfigurationException("There are no interfaces defined in master configuration XML file.");
        }
        return getSOATestingFrameworkMasterConfiguration().getInterfaces().getInterface();
    }

    /**
     *
     * @param interfaceName
     * @return
     * @throws FrameworkConfigurationException
     */
    public static Interface getInterface(String interfaceName) throws FrameworkConfigurationException {
        ListIterator<Interface> interfaces = getInterfaces().listIterator();
        Interface currentInterface = null;
        boolean found = false;
        while (interfaces.hasNext()) {
            currentInterface = interfaces.next();
            if (currentInterface.getName().equals(interfaceName)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new FrameworkConfigurationException("Interface with follwing identificator cannot be found in master configuration: " + interfaceName);
        }
        return currentInterface;
    }

    /**
     *
     * @param interfaceName
     * @return
     * @throws FrameworkConfigurationException
     */
    public static List<Project> getProjects(String interfaceName) throws FrameworkConfigurationException {
        if (getInterface(interfaceName).getProjects().getProject().isEmpty()) {
            throw new FrameworkConfigurationException("There are no configured projects for interface " + interfaceName + " in master configuration XML file.");
        }
        return getInterface(interfaceName).getProjects().getProject();
    }

    /**
     *
     * @param interfaceName
     * @param projectName
     * @return
     */
    public static Project getProject(String interfaceName, String projectName) {
        throw new UnsupportedOperationException("Not supported yet.");

    }

    /**
     *
     * @param interfaceName
     * @return
     * @throws FrameworkConfigurationException
     */
    public static List<ReferencedFlowPattern> getReferencedFlowPatterns(String interfaceName) throws FrameworkConfigurationException {
        if (getInterface(interfaceName).getPatterns().getReferencedFlowPattern().isEmpty()) {
            throw new FrameworkConfigurationException("There are no configured referenced flow patterns for interface " + interfaceName + ".");
        }
        return getInterface(interfaceName).getPatterns().getReferencedFlowPattern();
    }

    /**
     *
     * @param interfaceName
     * @param projectName
     * @return
     */
    public static ReferencedFlowPattern getReferencedFlowPattern(String interfaceName, String projectName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     *
     * @return
     * @throws FrameworkConfigurationException
     */
    public static List<OracleFusionMiddlewareInstance> getOracleFusionMiddlewareInstances() throws FrameworkConfigurationException {
        if (getSOATestingFrameworkMasterConfiguration().getEnvironments().getOracleFusionMiddleware().getOracleFusionMiddlewareInstance().isEmpty()) {
            throw new FrameworkConfigurationException("There are is no Oracle Fusion Middleware instances configured.");
        }
        return getSOATestingFrameworkMasterConfiguration().getEnvironments().getOracleFusionMiddleware().getOracleFusionMiddlewareInstance();
    }

    /**
     *
     * @param environmentIdentificator
     * @return
     * @throws FrameworkConfigurationException
     */
    public static OracleFusionMiddlewareInstance getOracleFusionMiddlewareInstance(String environmentIdentificator) throws FrameworkConfigurationException {
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
    public static String getOFMEnvironmentRealIdentificator(OracleFusionMiddlewareInstance instance) throws FrameworkConfigurationException {
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
     * @return
     * @throws FrameworkConfigurationException
     */
    public static List<Database> getDatabases() throws FrameworkConfigurationException {
        if (getSOATestingFrameworkMasterConfiguration().getEnvironments().getDatabases().getDatabase().isEmpty()) {
            throw new FrameworkConfigurationException("There are is no database configured.");
        }
        return getSOATestingFrameworkMasterConfiguration().getEnvironments().getDatabases().getDatabase();
    }

    /**
     *
     * @param identificator
     * @return
     * @throws FrameworkConfigurationException
     */
    public static Database getDatabase(String identificator) throws FrameworkConfigurationException {
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
    public static List<DatabaseInstance> getDatabaseInstances(String identificator) throws FrameworkConfigurationException {
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
    public static DatabaseInstance getDatabaseInstance(String environment, String identificator) throws FrameworkConfigurationException {
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
    public static String getDatabaseEnvironmentRealIdentificator(DatabaseInstance instance) throws FrameworkConfigurationException {
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
     * @return
     * @throws FrameworkConfigurationException
     */
    public static List<FtpServer> getFTPServers() throws FrameworkConfigurationException {
        if (getSOATestingFrameworkMasterConfiguration().getEnvironments().getFtpServers().getFtpServer().isEmpty()) {
            throw new FrameworkConfigurationException("There are is FTP servers configuration.");
        }
        return getSOATestingFrameworkMasterConfiguration().getEnvironments().getFtpServers().getFtpServer();
    }

    /**
     *
     * @param identificator
     * @return
     * @throws FrameworkConfigurationException
     */
    public static FtpServer getFTPServer(String identificator) throws FrameworkConfigurationException {
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
            throw new FrameworkConfigurationException("Database configuration with identificator " + identificator + " cannot be found.");
        }
        return ftpServer;
    }

    /**
     *
     * @param identificator
     * @return
     * @throws FrameworkConfigurationException
     */
    public static List<FtpServerInstance> getFTPServerInstances(String identificator) throws FrameworkConfigurationException {
        if (getFTPServer(identificator).getFtpServerInstance().isEmpty()) {
            throw new FrameworkConfigurationException("There are no database instances configured for database environment identificator " + identificator + ".");
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
    public static FtpServerInstance getFtpServerInstance(String environment, String identificator) throws FrameworkConfigurationException {
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
            throw new FrameworkConfigurationException("Database configuration with identificator " + identificator + " for environment " + environment + " cannot be found.");
        }
        return ftpServerInstance;
    }

    /**
     *
     * @param instance
     * @return
     * @throws FrameworkConfigurationException
     */
    public static String getFTPServerEnvironmentRealIdentificator(FtpServerInstance instance) throws FrameworkConfigurationException {
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
     * @return
     * @throws FrameworkConfigurationException
     */
    public static List<FlowPattern> getFlowPatterns() throws FrameworkConfigurationException {
        if (getSOATestingFrameworkMasterConfiguration().getFlowPatterns().getFlowPattern().isEmpty()) {
            throw new FrameworkConfigurationException("There are no flow pattern definitions available in the configuration file.");
        }
        return getSOATestingFrameworkMasterConfiguration().getFlowPatterns().getFlowPattern();
    }

    /**
     *
     * @param flowPatternIdentificator
     * @return
     * @throws FrameworkConfigurationException
     */
    public static FlowPattern getFlowPattern(String flowPatternIdentificator) throws FrameworkConfigurationException {
        ListIterator<FlowPattern> flowPatterns = getFlowPatterns().listIterator();
        FlowPattern flowPattern = null;
        boolean found = false;
        while (flowPatterns.hasNext()) {
            flowPattern = flowPatterns.next();
            if (flowPattern.getIdentificator().equals(flowPatternIdentificator)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new FrameworkConfigurationException("Flow pattern " + flowPatternIdentificator + " cannot be found.");
        }
        return flowPattern;
    }

    /**
     *
     * @param flowPatternIdentificator
     * @return
     * @throws FrameworkConfigurationException
     */
    public static List<TestScenario> getTestScenarios(String flowPatternIdentificator) throws FrameworkConfigurationException {
        if (getFlowPattern(flowPatternIdentificator).getTestScenario().isEmpty()) {
            throw new FrameworkConfigurationException();
        }
        return getFlowPattern(flowPatternIdentificator).getTestScenario();
    }

    /**
     *
     * @param flowPatternIdentificator
     * @param testScenarioIdentificator
     * @return
     * @throws FrameworkConfigurationException
     */
    public static TestScenario getTestScenario(String flowPatternIdentificator, String testScenarioIdentificator) throws FrameworkConfigurationException {
        ListIterator<TestScenario> testScenarios = getTestScenarios(flowPatternIdentificator).listIterator();
        TestScenario testScenario = null;
        boolean found = false;
        while (testScenarios.hasNext()) {
            testScenario = testScenarios.next();
            if (testScenario.getIdentificator().equals(testScenarioIdentificator)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new FrameworkConfigurationException("Test scenario for flow pattern identificator "
                    + flowPatternIdentificator + " and for test scenario identificator "
                    + testScenarioIdentificator + " are not configured.");
        }
        return testScenario;
    }

    /**
     *
     * @param flowPatternIdentificator
     * @param testScenarioIdentificator
     * @return
     * @throws FrameworkConfigurationException
     */
    public static List<ExecutionBlock> getExecutionBlocks(String flowPatternIdentificator, String testScenarioIdentificator) throws FrameworkConfigurationException {
        if (getTestScenario(flowPatternIdentificator, testScenarioIdentificator).getExecutionBlock().isEmpty()) {
            throw new FrameworkConfigurationException("There are no execution blocks defined for the flow pattern "
                    + flowPatternIdentificator + " and test scenario " + testScenarioIdentificator);
        }
        return getTestScenario(flowPatternIdentificator, testScenarioIdentificator).getExecutionBlock();

    }

    /**
     *
     * @param flowPatternIdentificator
     * @param testScenarioIdentificator
     * @param executionBlockIdentificator
     * @return
     * @throws FrameworkConfigurationException
     */
    public static ExecutionBlock getExecutionBlock(String flowPatternIdentificator, String testScenarioIdentificator, String executionBlockIdentificator) throws FrameworkConfigurationException {
        ListIterator<ExecutionBlock> executionBlocks = getExecutionBlocks(flowPatternIdentificator, testScenarioIdentificator).listIterator();
        ExecutionBlock executionBlock = null;
        boolean found = false;
        while (executionBlocks.hasNext()) {
            executionBlock = executionBlocks.next();
            if (executionBlock.getIdentificator().equals(executionBlockIdentificator)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new FrameworkConfigurationException("Execution block identified by " + executionBlockIdentificator
                    + " within test scenario identified by " + testScenarioIdentificator + " within flow pattern identified by "
                    + flowPatternIdentificator + " cannot be found.");
        }
        return executionBlock;
    }
}

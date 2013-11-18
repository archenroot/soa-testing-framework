/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.soatf.gui;

import static com.ibm.soatf.gui.FrameworkConfiguration.SOATF_CONFIGURATION_FILE;
import com.ibm.soatf.config.master.Databases.Database;
import com.ibm.soatf.config.master.Databases.Database.DatabaseInstance;
import com.ibm.soatf.config.master.EnvironmentType;
import com.ibm.soatf.config.master.ExecutionBlock;
import com.ibm.soatf.config.master.FTPServers.FtpServer;
import com.ibm.soatf.config.master.FTPServers.FtpServer.FtpServerInstance;
import com.ibm.soatf.config.master.FlowPattern;
import com.ibm.soatf.config.master.Interface;
import com.ibm.soatf.config.master.Interface.Patterns.ReferencedFlowPattern;
import com.ibm.soatf.config.master.Operation;
import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance;
import com.ibm.soatf.config.master.Project;
import com.ibm.soatf.config.master.SOATestingFrameworkMasterConfiguration;
import com.ibm.soatf.config.master.TestScenario;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
     * @return @throws FrameworkConfigurationException
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
     * @return @throws FrameworkConfigurationException
     */
    public static List<Interface> getInterfaces() throws FrameworkConfigurationException {
        if (getSOATestingFrameworkMasterConfiguration().getInterfaces().getInterface().isEmpty()) {
            throw new FrameworkConfigurationException("There are no interfaces defined in master configuration XML file.");
        }
        return getSOATestingFrameworkMasterConfiguration().getInterfaces().getInterface();
    }

    public static List<String> getInterfaceNames() throws FrameworkConfigurationException {
        if (getSOATestingFrameworkMasterConfiguration().getInterfaces().getInterface().isEmpty()) {
            throw new FrameworkConfigurationException("There are no interfaces defined in master configuration XML file.");
        }
        List<String> interfaceNames = new ArrayList<>();
        ListIterator<Interface> _interface = getInterfaces().listIterator();
        while (_interface.hasNext()) {
            interfaceNames.add(_interface.next().getName());
        }
        return interfaceNames;
    }

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

    public static List<Project> getProjects(String interfaceName) throws FrameworkConfigurationException {
        if (getInterface(interfaceName).getProjects().getProject().isEmpty()) {
            throw new FrameworkConfigurationException("There are no configured projects for interface " + interfaceName + " in master configuration XML file.");
        }
        return getInterface(interfaceName).getProjects().getProject();
    }

    public static List<String> getProjectNames(String interfaceName) throws FrameworkConfigurationException {
        ListIterator<Project> projectIterator = getProjects(interfaceName).listIterator();
        Project project;
        List<String> projectNames = new ArrayList();
        while (projectIterator.hasNext()) {
            project = projectIterator.next();
            if (project.getName().isEmpty()) {
                //throw new FrameworkConfigurationException("Identificator empty.");
            }
            projectNames.add(project.getName());
        }

        return projectNames;
    }

    public static Project getProject(String interfaceName, String projectName) {
        throw new UnsupportedOperationException("Not supported yet.");

    }

    public static List<ReferencedFlowPattern> getReferencedFlowPatterns(String interfaceName) throws FrameworkConfigurationException {
        if (getInterface(interfaceName).getPatterns().getReferencedFlowPattern().isEmpty()) {
            throw new FrameworkConfigurationException("There are no configured referenced flow patterns for interface " + interfaceName + ".");
        }
        return getInterface(interfaceName).getPatterns().getReferencedFlowPattern();
    }

    public static List<String> getReferencedFlowPatternNames(String interfaceName) throws FrameworkConfigurationException {
        ListIterator<ReferencedFlowPattern> referencedFlowPatternIterator = getReferencedFlowPatterns(interfaceName).listIterator();
        ReferencedFlowPattern referencedFlowPattern;
        List<String> referencedFlowPatternNames = new ArrayList();
        while (referencedFlowPatternIterator.hasNext()) {
            referencedFlowPattern = referencedFlowPatternIterator.next();
            if (referencedFlowPattern.getIdentificator().isEmpty()) {
                throw new FrameworkConfigurationException("Identificator empty.");
            }
            referencedFlowPatternNames.add(referencedFlowPattern.getIdentificator());
        }

        return referencedFlowPatternNames;
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
     * @return @throws FrameworkConfigurationException
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
     * @return @throws FrameworkConfigurationException
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
     * @return @throws FrameworkConfigurationException
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
     * @return @throws FrameworkConfigurationException
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

    public static List<String> getTestScenariosAndExecutionBlocks(String flowPatternIdentificator) throws FrameworkConfigurationException {
        List<String> result = new ArrayList<>();

        ListIterator<TestScenario> testScenarioIterator = getTestScenarios(flowPatternIdentificator).listIterator();
        TestScenario testScenario;

        while (testScenarioIterator.hasNext()) {
            StringBuilder sbPar = new StringBuilder();

            testScenario = testScenarioIterator.next();
            sbPar.append(testScenario.getIdentificator());
            sbPar.append("; type:");
            sbPar.append(testScenario.getType());
            sbPar.append("; seqId:");
            sbPar.append(testScenario.getSequenceId());
            ListIterator<ExecutionBlock> execBlcIter = testScenario.getExecutionBlock().listIterator();
            ExecutionBlock eb;
            while (execBlcIter.hasNext()) {
                StringBuilder sbChil = new StringBuilder(sbPar);
                eb = execBlcIter.next();
                sbChil.append("; execBlc:");
                sbChil.append(eb.getIdentificator());
                sbChil.append("; seqId:");
                sbChil.append(eb.getSequenceId());
                sbChil.append("; dir:");
                sbChil.append(eb.getDirection());
                result.add(sbChil.toString());
            }
        }

        return result;

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

    public static List<String> getExecutionBlockNames(SimpleCompositeKey sck) throws FrameworkConfigurationException {

        ListIterator<ExecutionBlock> exBlkIter = getExecutionBlocks(sck.getFlowPatternName(), sck.getTestScenarioName()).listIterator();
        ExecutionBlock eb;
        List<String> operations = new ArrayList();
        while (exBlkIter.hasNext()) {
            eb = exBlkIter.next();
            ListIterator<Operation> operIter = eb.getOperation().listIterator();
            Operation oper;
            while (operIter.hasNext()) {
                oper = operIter.next();
                StringBuilder sb = new StringBuilder();
                sb.append(oper.getName());
                sb.append("; execOn:");
                sb.append(oper.getExecuteOn().value());
                operations.add(sb.toString());
            }
        }
        return operations;
    }

    // This needs to be revised because the combobox should alway provide only really supported values for particular interface.
    public static List<String> getAllEnvironments() throws FrameworkConfigurationException {
        List<String> allEn = new ArrayList();
        ListIterator<OracleFusionMiddlewareInstance> ofmIter = getOracleFusionMiddlewareInstances().listIterator();
        ListIterator<Database> dbIter = getDatabases().listIterator();
        ListIterator<FtpServer> ftpIter = getFTPServers().listIterator();
        String envName = null;
        while (ofmIter.hasNext()) {

            OracleFusionMiddlewareInstance ofmi = ofmIter.next();
            if (ofmi.getEnvironmentType().SYSTEM.value().equals("SYSTEM")) {
                envName = ofmi.getEnvironment().value();
            } else {
                envName = ofmi.getName();
            }
            if (!allEn.contains(envName)) {
                allEn.add(envName);
            }
        }
        while (dbIter.hasNext()) {
            Database db = dbIter.next();
            ListIterator<DatabaseInstance> dbIt = db.getDatabaseInstance().listIterator();
            DatabaseInstance dbInst;
            while (dbIt.hasNext()) {
                dbInst = dbIt.next();
                if (dbInst.getEnvironmentType().SYSTEM.value().equals("SYSTEM")) {
                    envName = dbInst.getEnvironment().value();
                } else {
                    envName = dbInst.getName();
                }
                if (!allEn.contains(envName)) {
                    allEn.add(envName);
                }
            }

        }
        while (ftpIter.hasNext()) {
            FtpServer ftp = ftpIter.next();
            ListIterator<FtpServerInstance> ftpIt = ftp.getFtpServerInstance().listIterator();
            FtpServerInstance ftpInst;
            while (ftpIt.hasNext()) {
                ftpInst = ftpIt.next();
                if (ftpInst.getEnvironmentType().SYSTEM.value().equals("SYSTEM")) {
                    envName = ftpInst.getEnvironment().value();
                } else {
                    envName = ftpInst.getName();
                }
                if (!allEn.contains(envName)) {
                    allEn.add(envName);
                }
            }

        }

        return allEn;

    }
}

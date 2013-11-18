/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.soatf;

import static com.ibm.soatf.FrameworkConfiguration.SOA_TEST_HOME;
import static com.ibm.soatf.FrameworkConfiguration.validateInterfaceStructure;
import static com.ibm.soatf.MasterConfiguration.getExecutionBlock;
import static com.ibm.soatf.MasterConfiguration.getFlowPattern;
import static com.ibm.soatf.MasterConfiguration.getInterface;
import static com.ibm.soatf.MasterConfiguration.getInterfaces;
import com.ibm.soatf.config._interface.InterfaceExecutionBlock;
import com.ibm.soatf.config._interface.InterfaceFlowPattern;
import com.ibm.soatf.config._interface.InterfaceTestScenario;
import com.ibm.soatf.config._interface.db.DatabaseConfiguration;
import com.ibm.soatf.config._interface.ftp.FTPConfiguration;
import com.ibm.soatf.config._interface.jms.JMSConfiguration;
import com.ibm.soatf.config._interface.osb.OSBConfiguration;
import com.ibm.soatf.config._interface.soap.SOAPConfiguration;
import com.ibm.soatf.config._interface.util.UtilConfiguration;
import com.ibm.soatf.config.master.Databases;
import com.ibm.soatf.config.master.Databases.Database.DatabaseInstance;
import com.ibm.soatf.config.master.Environment;
import com.ibm.soatf.config.master.EnvironmentType;
import static com.ibm.soatf.config.master.ExecuteOn.SOURCE;
import static com.ibm.soatf.config.master.ExecuteOn.TARGET;
import com.ibm.soatf.config.master.ExecutionBlock;
import com.ibm.soatf.config.master.FTPServers;
import com.ibm.soatf.config.master.FTPServers.FtpServer.FtpServerInstance;
import com.ibm.soatf.config.master.FileSystemProjectStructure;
import com.ibm.soatf.config.master.FlowPattern;
import com.ibm.soatf.config.master.Interface;
import com.ibm.soatf.config.master.Interface.Patterns.ReferencedFlowPattern;
import com.ibm.soatf.config.master.Operation;
import com.ibm.soatf.config.master.OracleFusionMiddleware;
import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance;
import com.ibm.soatf.config.master.TestScenario;
import com.ibm.soatf.database.DatabaseComponent;
import com.ibm.soatf.ftp.FTPComponent;
import com.ibm.soatf.util.UtilityComponent;
import com.ibm.soatf.jms.JMSComponent;
import com.ibm.soatf.osb.OSBComponent;
import com.ibm.soatf.soap.SOAPComponent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public class FlowManager {

    private final Logger logger = LogManager.getLogger(FlowManager.class.getName());

    public String interfaceName;
    private String environmentName;
    private String inputEnvironment;
    private String inputInterfaceName;
    private List<String> flowPatternsToProcess = new ArrayList();
    private String flowPatternToProcess;

    private Interface _interface;

    private Environment env;
    private EnvironmentType envType;

    // Master configuration lists
    private List<FlowPatternCompositeKey> masterCompositeKeys = new ArrayList();

    private List<Interface> _interfaces = new ArrayList<>();
    private List<ReferencedFlowPattern> referencedFlowPatterns = new ArrayList<>();
    private List<FlowPattern> flowPatterns = new ArrayList<>();
    private FlowPattern flowPattern;
    private List<TestScenario> testScenarios = new ArrayList<>();
    private TestScenario testScenario;
    private List<ExecutionBlock> executionBlocks = new ArrayList<>();
    private ExecutionBlock executionBlock;
    private List<Operation> operations = new ArrayList<>();
    private Operation operation;

    private List<FlowPatternCompositeKey> flowPatternsCompositeKeys = new ArrayList<>();

    // Master Environments
    private OracleFusionMiddleware oracleFusionMiddleware;
    private Databases databases;
    private FTPServers ftpServers;

    // Filesystem structure
    FileSystemProjectStructure fsProjStructure = null;

    // Interface configuration objects
    InterfaceConfiguration ifaceConfig;
    private List<FlowPatternCompositeKey> ifaceCompositeKeys = new ArrayList();

    private List<InterfaceFlowPattern> ifaceFlowPatterns = new ArrayList();
    private List<InterfaceTestScenario> ifaceTestCases = new ArrayList();
    private List<InterfaceExecutionBlock> ifaceExecutionBlocks = new ArrayList();
    private InterfaceFlowPattern ifaceFlowPattern;
    private InterfaceTestScenario ifaceTestCase;
    private InterfaceExecutionBlock ifaceExecutionBlock;

    private ComponentResult componentResult = new ComponentResult();

    FlowManager(String interfaceName, String environmentName) throws FrameworkConfigurationException {
        this.interfaceName = interfaceName;
        this.environmentName = environmentName;
        this.ifaceConfig = new InterfaceConfiguration(new File(SOA_TEST_HOME + "\\" + interfaceName + "\\" + interfaceName + ".xml"));

        try {
            constructFlowManager();
        } catch (FrameworkConfigurationException fcex) {
            logger.fatal("Main soa testing framework object " + FlowManager.class.getName() + " cannot be created.", fcex);
        }
    }

    private void constructFlowManager() throws FrameworkConfigurationException {
        // overall ceck
        generalFrameworkConfiguration();
        logger.debug("Going to load master object instances.");
        loadMasterInstances();
        FrameworkConfiguration.init();
        //FrameworkConfiguration.checkConfiguration();
        validateInterfaceStructure(this._interface);
        logger.debug("Master object instances loaded, going to load interface object instances.");
        loadInterfaceInstances();
        logger.debug("Interface object instances loaded, going to validate master and interface configuration.");
        validateFlowPatternConfiguration();
        logger.info("Configuration looks consistent, instance construction of " + this.getClass().getCanonicalName() + " class finished with success.");
    }

    public static void executeInterfaceTest(String interfaceName, String environmentName) throws FrameworkConfigurationException {
        FlowManager flowManager = new FlowManager(interfaceName, environmentName);
        flowManager.execute();
    }

    private void generalFrameworkConfiguration() throws FrameworkConfigurationException {
        
    }
    private void loadMasterInstances() throws FrameworkConfigurationException {
        try {
            FlowPatternCompositeKey fpck;

            this._interfaces = getInterfaces();
            this._interface = getInterface(this.interfaceName);
            this.referencedFlowPatterns = this._interface.getPatterns().getReferencedFlowPattern();
            

            ListIterator<ReferencedFlowPattern> referencedFlowPatternIterator = this.referencedFlowPatterns.listIterator();
            ReferencedFlowPattern referencedFlowPattern;
            while (referencedFlowPatternIterator.hasNext()) {
                fpck = new FlowPatternCompositeKey();
                fpck.setDescriptor("MASTER");
                fpck.setInterfaceName(this._interface.getName());
                referencedFlowPattern = referencedFlowPatternIterator.next();

                // Add flow patterns to process
                for (int i = 0; i < referencedFlowPattern.getOccurrence(); i++) {
                    this.flowPatternsToProcess.add(referencedFlowPattern.getIdentificator());
                }

                if (referencedFlowPattern.getIdentificator().isEmpty()) {
                    throw new FrameworkConfigurationException("TestScenario identificator is empty for composite key: " + fpck.toString());
                }
                fpck.setFlowPatternIdentificator(referencedFlowPattern.getIdentificator());
                this.flowPattern = getFlowPattern(fpck);
                this.flowPatterns.add(this.flowPattern);
                // Set first part of composite key
                fpck.setFlowPatternIdentificator(this.flowPattern.getIdentificator());

                ListIterator<TestScenario> testScenarioIterator = flowPattern.getTestScenario().listIterator();

                while (testScenarioIterator.hasNext()) {
                    TestScenario testScenario = testScenarioIterator.next();

                    if (testScenario.getIdentificator().isEmpty()) {
                        throw new FrameworkConfigurationException("TestScenario identificator is empty for composite key: " + fpck.toString());
                    }
                    //Set second part of composite key
                    fpck.setTestScenarioIdentificator(testScenario.getIdentificator());
                    ListIterator<ExecutionBlock> executionBlockIterator = testScenario.getExecutionBlock().listIterator();
                    ExecutionBlock executionBlock;
                    while (executionBlockIterator.hasNext()) {
                        executionBlock = executionBlockIterator.next();
                        if (executionBlock.getIdentificator().isEmpty()) {
                            throw new FrameworkConfigurationException("ExecutionBlock identificator is empty for composite key: " + fpck.toString());
                        }
                        fpck.setExecutionBlockIdentificator(executionBlock.getIdentificator());
                        ListIterator<Operation> operationIterator = executionBlock.getOperation().listIterator();
                        Operation operation;
                        while (operationIterator.hasNext()) {
                            operation = operationIterator.next();
                            switch (operation.getExecuteOn()) {
                                case SOURCE:
                                    fpck.setHasSource(true);
                                    break;
                                case TARGET:
                                    fpck.setHasTarget(true);
                                    break;
                                case NA:
                                    fpck.setHasCommon(true);
                                    break;
                            }
                        }
                        logger.debug("Master composite key basic structure completed: " + fpck.toString());
                        // Composite key from master configuration has been set
                        this.masterCompositeKeys.add(fpck);
                        fpck = new FlowPatternCompositeKey(fpck);
                    }
                }
            }
        } catch (FrameworkConfigurationException ex) {
            throw new FrameworkConfigurationException("Exception occured while trying to prepare master configuration objects.", ex);
        }
    }

    private void loadInterfaceInstances() throws FrameworkConfigurationException {
        try {

            FlowPatternCompositeKey fpck;
            ifaceFlowPatterns = ifaceConfig.getInterfaceFlowPatterns();
            ListIterator<InterfaceFlowPattern> interfaceFlowPatternIterator = ifaceFlowPatterns.listIterator();
            InterfaceFlowPattern interfaceFlowPattern;
            while (interfaceFlowPatternIterator.hasNext()) {
                fpck = new FlowPatternCompositeKey();
                fpck.setDescriptor("INTERFACE");
                fpck.setInterfaceName(this._interface.getName());
                interfaceFlowPattern = interfaceFlowPatternIterator.next();
                fpck.setTestName(interfaceFlowPattern.getTestName());
                if (interfaceFlowPattern.getIdentificator().isEmpty()) {
                    throw new FrameworkConfigurationException("InterfaceFlowPattern identificator is empty for composite key: " + fpck.toString());
                }
                fpck.setFlowPatternIdentificator(interfaceFlowPattern.getIdentificator());
                ListIterator<InterfaceTestScenario> interfaceTestScenarioIterator = interfaceFlowPattern.getInterfaceTestScenario().listIterator();
                InterfaceTestScenario interfaceTestScenario;
                while (interfaceTestScenarioIterator.hasNext()) {
                    interfaceTestScenario = interfaceTestScenarioIterator.next();
                    if (interfaceTestScenario.getIdentificator().isEmpty()) {
                        throw new FrameworkConfigurationException("InterfaceTestScenario identificator is empty for composite key: " + fpck.toString());
                    }
                    fpck.setTestScenarioIdentificator(interfaceTestScenario.getIdentificator());
                    ListIterator<InterfaceExecutionBlock> interfaceExecutionBlockIterator = interfaceTestScenario.getInterfaceExecutionBlock().listIterator();
                    InterfaceExecutionBlock interfaceExecutionBlock;
                    while (interfaceExecutionBlockIterator.hasNext()) {
                        interfaceExecutionBlock = interfaceExecutionBlockIterator.next();
                        if (interfaceExecutionBlock.getIdentificator().isEmpty()) {
                            throw new FrameworkConfigurationException("InterfaceExecutionBlock identificator is empty for composite key: " + fpck.toString());
                        }
                        fpck.setExecutionBlockIdentificator(interfaceExecutionBlock.getIdentificator());
                        fpck.setSource(interfaceExecutionBlock.getSource());
                        fpck.setTarget(interfaceExecutionBlock.getTarget());
                        fpck.setUtilConfiguration(interfaceExecutionBlock.getUtil());

                        logger.debug("Interface composite key basic structure completed: " + fpck.toString());
                        this.ifaceCompositeKeys.add(fpck);
                        fpck = new FlowPatternCompositeKey(fpck);
                    }
                }

            }
            logger.debug("We have this number of composite keys to process: " + this.ifaceCompositeKeys.size() + ".");
        } catch (FrameworkConfigurationException ex) {
            throw new FrameworkConfigurationException("Exception occured while trying to prepare interface configuration objects.", ex);
        }
    }

    private void validateFlowPatternConfiguration() {

    }

    private void execute() throws FrameworkConfigurationException {
        //ListIterator<String> flowPatternsToProcessIterator = flowPatternsToProcess.listIterator();

        ListIterator<FlowPatternCompositeKey> interfaceFlowPatternCompositeKeyIterator = this.ifaceCompositeKeys.listIterator();
        FlowPatternCompositeKey ifaceFlowPatternCompositeKey;
        FlowPatternCompositeKey masterFlowPatternCompositeKey;

        while (interfaceFlowPatternCompositeKeyIterator.hasNext()) {
            ifaceFlowPatternCompositeKey = interfaceFlowPatternCompositeKeyIterator.next();
            logger.trace("Processing flow : " + ifaceFlowPatternCompositeKey.getFlowPatternIdentificator());
            logger.debug("Processing test scenario: " + ifaceFlowPatternCompositeKey.getTestScenarioIdentificator());
            logger.trace("Processing execution block: " + ifaceFlowPatternCompositeKey.getExecutionBlockIdentificator() + ".");
            //logger.debug("Interface composite key digest to search for within master configuration is: " + ifaceFlowPatternCompositeKey.getCompositeKeyDigest());
            masterFlowPatternCompositeKey = ifaceFlowPatternCompositeKey.getMatchingKey(this.masterCompositeKeys);
            //ListIterator<TestScenario> tsIterator = MasterConfiguration.getTestScenarios(ifaceFlowPatternCompositeKey.getFlowPatternIdentificator()).listIterator();
            //testScenario = tsIterator.next();
            
            executionBlock = getExecutionBlock(masterFlowPatternCompositeKey);
            ifaceExecutionBlock = ifaceConfig.getInterfaceExecutionBlock(ifaceFlowPatternCompositeKey);
            //operations = executionBlock.getOperation();
            ListIterator<Operation> operationIterator = MasterConfiguration.getExecutionBlock(ifaceFlowPatternCompositeKey).getOperation().listIterator();

            String component;
            // Master configuration objects
            DatabaseInstance dbInstance;
            OracleFusionMiddlewareInstance  ofmInstance;
            FtpServerInstance ftpServerInstance;
            
            // Interface configuration objects
            DatabaseConfiguration dbConfig = null;
            FTPConfiguration ftpConfig = null;
            JMSConfiguration jmsConfig = null;
            SOAPConfiguration soapConfig = null;
            UtilConfiguration utilConfig;
            OSBConfiguration osbConfig = null;
            
            // Components
            DatabaseComponent dbComp;
            OSBComponent osbComp;
            JMSComponent jmsComp;
            FTPComponent ftpComp;
            SOAPComponent soapComp;
            UtilityComponent utilComp;
                
            while (operationIterator.hasNext()) {
                operation = operationIterator.next();
                component = getComponentTypeName(operation.getName());
                switch (component) {
                    case "DB":
                        logger.trace("Operation " + operation.getName() + " has been identified as a type of " + component + " component operation type.");
                        // Database configuration
                        if (operation.getExecuteOn().equals(SOURCE)) { dbConfig = ifaceConfig.getSource(ifaceFlowPatternCompositeKey).getDatabase(); }
                        if (operation.getExecuteOn().equals(TARGET)) { dbConfig = ifaceConfig.getTarget(ifaceFlowPatternCompositeKey).getDatabase(); }
                        dbInstance = MasterConfiguration.getDatabaseInstance(environmentName, dbConfig.getIdentificator());
                        dbComp = new DatabaseComponent(dbInstance, dbConfig, componentResult, ifaceFlowPatternCompositeKey);
                        dbComp.execute(CompOperType.DB_GENERATE_INSERT_ONE_ROW_RANDOM);
                        dbComp.execute(CompOperType.valueOf(operation.getName()));
                        break;
                    case "FILE":
                        throw new FrameworkConfigurationException("File Component is not implemented yet!");
                    case "FTP":
                        logger.trace("Operation " + operation.getName() + " has been identified as a type of " + component + " component operation type.");
                        if (operation.getExecuteOn().equals(SOURCE)) {ftpConfig = ifaceConfig.getSource(ifaceFlowPatternCompositeKey).getFtpServer(); } 
                        if (operation.getExecuteOn().equals(TARGET)) {ftpConfig = ifaceConfig.getTarget(ifaceFlowPatternCompositeKey).getFtpServer(); }
                        ftpServerInstance = MasterConfiguration.getFtpServerInstance(environmentName, ftpConfig.getIdentificator());
                        ftpComp = new FTPComponent(ftpServerInstance, ftpConfig, componentResult, ifaceFlowPatternCompositeKey);
                        
                        break;
                    case "OSB":
                        logger.trace("Operation " + operation.getName() + " has been identified as a type of " + component + " component operation type.");
                        if (operation.getExecuteOn().equals(SOURCE)) { osbConfig = ifaceConfig.getSource(ifaceFlowPatternCompositeKey).getOsb(); }
                        if (operation.getExecuteOn().equals(TARGET)) { osbConfig = ifaceConfig.getTarget(ifaceFlowPatternCompositeKey).getOsb(); }
                        ofmInstance = MasterConfiguration.getOracleFusionMiddlewareInstance(environmentName);
                        osbComp = new OSBComponent(ofmInstance, osbConfig, componentResult, ifaceFlowPatternCompositeKey);
                        osbComp.executeOperation(CompOperType.valueOf(operation.getName()));
                        break;
                    case "JMS":
                        logger.trace("Operation " + operation.getName() + " has been identified as a type of " + component + " component operation type.");
                        if (operation.getExecuteOn().equals(SOURCE)) { jmsConfig = ifaceConfig.getSource(ifaceFlowPatternCompositeKey).getJmsSubsystem(); }
                        if (operation.getExecuteOn().equals(TARGET)) { jmsConfig = ifaceConfig.getTarget(ifaceFlowPatternCompositeKey).getJmsSubsystem(); }
                        ofmInstance = MasterConfiguration.getOracleFusionMiddlewareInstance(environmentName);
                        jmsComp = new JMSComponent(ofmInstance, jmsConfig, componentResult, ifaceFlowPatternCompositeKey);
                        jmsComp.executeOperation(CompOperType.valueOf(operation.getName()));
                        break;
                    case "SOAP":
                        logger.trace("Operation " + operation.getName() + " has been identified as a type of " + component + " component operation type.");
                        if (operation.getExecuteOn().equals(SOURCE)) { soapConfig = ifaceConfig.getSource(ifaceFlowPatternCompositeKey).getSoap(); } 
                        if (operation.getExecuteOn().equals(TARGET)) { soapConfig = ifaceConfig.getTarget(ifaceFlowPatternCompositeKey).getSoap(); }
                        ofmInstance = MasterConfiguration.getOracleFusionMiddlewareInstance(environmentName);
                        soapComp = new SOAPComponent(ofmInstance, soapConfig, componentResult, ifaceFlowPatternCompositeKey);
                        break;
                    case "UTIL":
                        logger.trace("Operation " + operation.getName() + " has been identified as a type of " + component + " component operation type.");
                        utilConfig = ifaceExecutionBlock.getUtil();
                        utilComp = new UtilityComponent(utilConfig, componentResult, ifaceFlowPatternCompositeKey);
                        break;
                    default:
                        throw new FrameworkConfigurationException("Unsupported type of component to be created: " + component);
                }
            }
        }
    }

    private String getComponentTypeName(String operationName) {
        return operationName.substring(0, operationName.indexOf("_"));
    }
    
    private boolean isNull(Object object){
        if (object == null){
            return true;
        }
        return false;
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.soatf;

import com.ibm.soatf.config._interface.SOATestingFrameworkInterfaceConfiguration;
import com.ibm.soatf.config.master.AbstractMasterConfigEnvironmentInstance;
import com.ibm.soatf.config.master.Databases;
import com.ibm.soatf.config.master.Environment;
import com.ibm.soatf.config.master.EnvironmentType;
import com.ibm.soatf.config.master.FTPServers;
import com.ibm.soatf.config.master.FileSystemProjectStructure;
import com.ibm.soatf.config.master.FlowPattern;
import com.ibm.soatf.config.master.Interface;
import com.ibm.soatf.config.master.Interface.Patterns.ReferencedFlowPattern;
import com.ibm.soatf.config.master.Operation;
import com.ibm.soatf.config.master.OracleFusionMiddleware;
import com.ibm.soatf.config.master.SOATestingFrameworkMasterConfiguration;
import com.ibm.soatf.config.master.TestScenario;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public class FlowManager {

    private static final Logger LOGGER = LogManager.getLogger(FlowManager.class.getName());

    private String interfaceName;
    private String environmentName;
    private String inputEnvironment;
    private String inputInterfaceName;

    private Environment env;
    private EnvironmentType envType;

    // Master configuration lists
    private List<Interface> masterInterfaces = new ArrayList<>();
    private List<ReferencedFlowPattern> referencedFlowPatterns = new ArrayList<>();
    private List<FlowPattern> flowPatterns = new ArrayList<>();
    private List<TestScenario> testCaseScenarios = new ArrayList<>();
    private Operation operation;
    
    private List<FlowPatternCompositeKey> flowPatternsCompositeKeys = new ArrayList<>();
    
   

    // Master Environments
    private OracleFusionMiddleware oracleFusionMiddleware;
    private Databases databases;
    private FTPServers ftpServers;

    // Instance object comming from abstract
    AbstractMasterConfigEnvironmentInstance abstractMasterConfEnvInstance = null;

    // Filesystem structure
    FileSystemProjectStructure fsProjStructure = null;

    private SOATestingFrameworkMasterConfiguration soaTFMasterConfig;
    private SOATestingFrameworkInterfaceConfiguration soaTFInterfaceConfig;

    FlowManager() {

    }

    FlowManager(String interfaceName, String environmentName) {
        this.interfaceName = interfaceName;
        this.environmentName = environmentName;
    }

    public static void startInterfaceTest(String interfaceName, String environmentName) {
        FlowManager flowManager = new FlowManager(interfaceName, environmentName);
        flowManager.execute();
    }

    public void execute() {
          
    }

    
    private class FlowPatternCompositeKey {

        private String flowPatternIdentificator;
        private String testScenarioIdentificator;
        private String executionBlockIdentificator;

        FlowPatternCompositeKey() {

        }

        public FlowPatternCompositeKey(String flowPatternIdentificator) {
            this.flowPatternIdentificator = flowPatternIdentificator;
        }

        public FlowPatternCompositeKey(String flowPatternIdentificator, String testScenarioIdentificator) {
            this.flowPatternIdentificator = flowPatternIdentificator;
            this.testScenarioIdentificator = testScenarioIdentificator;
        }

        public FlowPatternCompositeKey(String flowPatternIdentificator, String testScenarioIdentificator, String executionBlockIdentificator) {
            this.flowPatternIdentificator = flowPatternIdentificator;
            this.testScenarioIdentificator = testScenarioIdentificator;
            this.executionBlockIdentificator = executionBlockIdentificator;
        }

        public String getFlowPatternIdentificator() {
            return flowPatternIdentificator;
        }

        public String getTestScenarioIdentificator() {
            return testScenarioIdentificator;
        }

        public String getExecutionBlockIdentificator() {
            return executionBlockIdentificator;
        }

        public void setFlowPatternIdentificator(String flowPatternIdentificator) {
            this.flowPatternIdentificator = flowPatternIdentificator;
        }

        public void setTestScenarioIdentificator(String testScenarioIdentificator) {
            this.testScenarioIdentificator = testScenarioIdentificator;
        }

        public void setExecutionBlockIdentificator(String executionBlockIdentificator) {
            this.executionBlockIdentificator = executionBlockIdentificator;
        }

    }
}

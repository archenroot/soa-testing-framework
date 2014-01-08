/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.soatf.config;

import com.ibm.soatf.FrameworkException;
import com.ibm.soatf.config.iface.IfaceExecBlock;
import com.ibm.soatf.config.iface.IfaceExecBlock.Source;
import com.ibm.soatf.config.iface.IfaceExecBlock.Target;
import com.ibm.soatf.config.iface.IfaceFlowPattern;
import com.ibm.soatf.config.iface.IfaceTestScenario;
import com.ibm.soatf.config.iface.SOATFIfaceConfig;
import com.ibm.soatf.config.iface.SOATFIfaceConfig.IfaceEndPoints.IfaceEndPoint;
import com.ibm.soatf.config.iface.db.DBConfig;
import com.ibm.soatf.config.iface.db.DBConfig.DbObjects;
import com.ibm.soatf.config.iface.db.DBConfig.DefaultDbObjects;
import com.ibm.soatf.config.iface.db.DbObject;
import com.ibm.soatf.config.iface.util.UTILConfig;
import com.ibm.soatf.config.master.ExecuteOn;
import com.ibm.soatf.config.master.ExecutionBlock;
import com.ibm.soatf.config.master.Operation;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author zANGETSu
 */
public class InterfaceConfiguration {
    private final File INTERFACE_CONFIG_FILE;
    private final MasterConfiguration MCFG;
    private final FrameworkConfiguration FCFG;
    private SOATFIfaceConfig XML_CONFIG;

    InterfaceConfiguration(File ifaceConfigFile, FrameworkConfiguration fcfg, MasterConfiguration mcfg) {
        INTERFACE_CONFIG_FILE = ifaceConfigFile;
        FCFG = fcfg;
        MCFG = mcfg;
    }

    void init() throws FrameworkConfigurationException {
        JAXBContext jaxbContext;
        Unmarshaller jaxbUnmarshaller;
        try {
            jaxbContext = JAXBContext.newInstance("com.ibm.soatf.config.iface");
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            XML_CONFIG = ((JAXBElement<SOATFIfaceConfig>) jaxbUnmarshaller.unmarshal(INTERFACE_CONFIG_FILE)).getValue();
        } catch (JAXBException jbex) {
            throw new FrameworkConfigurationException("Error while unmarshalling interface configuration object from XML file " + INTERFACE_CONFIG_FILE, jbex);
        }
    }

    public List<IfaceFlowPattern> getIfaceFlowPatterns() throws FrameworkConfigurationException {
        if (XML_CONFIG.getIfaceFlowPatternConfig().getIfaceFlowPattern().isEmpty()) {
            throw new FrameworkConfigurationException("No flow patterns configuration found in interface config file '" + INTERFACE_CONFIG_FILE + "'.");
        }
        return XML_CONFIG.getIfaceFlowPatternConfig().getIfaceFlowPattern();
    }

    public IfaceFlowPattern getIfaceFlowPattern(String flowPatternId) throws FrameworkConfigurationException {
        for (IfaceFlowPattern refFlowPattern : getIfaceFlowPatterns()) {
            if (refFlowPattern.getRefId().equals(flowPatternId)) {
                return refFlowPattern;
            }
        }
        throw new FrameworkConfigurationException("No flow pattern found using reference id '" + flowPatternId + "'.");
    }

    public List<IfaceTestScenario> getIfaceTestScenarios(String ifaceFlowPatternId) throws FrameworkConfigurationException {
        final List<IfaceTestScenario> ifaceTestScenarios = getIfaceFlowPattern(ifaceFlowPatternId).getIfaceTestScenario();
        if (ifaceTestScenarios.isEmpty()) {
            throw new FrameworkConfigurationException("No test scenarios found for flow pattern: " + ifaceFlowPatternId + "'.");
        }
        return ifaceTestScenarios;
    }

    public IfaceTestScenario getIfaceTestScenario(String ifaceFlowPatternId, String ifaceTestScenarioId) throws FrameworkConfigurationException {
        for (IfaceTestScenario ifaceTestScenario : getIfaceTestScenarios(ifaceFlowPatternId)) {
            if (ifaceTestScenario.getRefId().equals(ifaceTestScenarioId)) {
                return ifaceTestScenario;
            }
        }
        throw new FrameworkConfigurationException("No such test scenario found: " + ifaceTestScenarioId + " in flow pattern: " + ifaceFlowPatternId);
    }

    public List<IfaceExecBlock> getIfaceExecBlocks(String ifaceFlowPatternId, String ifaceTestScenarioId) throws FrameworkConfigurationException {
        final List<IfaceExecBlock> ifaceExecBlocks = getIfaceTestScenario(ifaceFlowPatternId, ifaceTestScenarioId).getIfaceExecBlock();
        if (ifaceExecBlocks.isEmpty()) {
            throw new FrameworkConfigurationException("No execution blocks found in test scenario: " + ifaceTestScenarioId
                    + " in flow pattern: " + ifaceFlowPatternId);
        }
        return ifaceExecBlocks;
    }

    public IfaceExecBlock getIfaceExecBlock(String ifaceFlowPatternId, String ifaceTestScenarioId, String ifaceExecBlockId) throws FrameworkConfigurationException {
        for (IfaceExecBlock refExecBlock : getIfaceExecBlocks(ifaceFlowPatternId, ifaceTestScenarioId)) {
            if (refExecBlock.getRefId().equals(ifaceExecBlockId)) {
                return refExecBlock;
            }
        }
        throw new FrameworkConfigurationException("No such execution block found: " + ifaceExecBlockId + " in test scenario: " + ifaceTestScenarioId
                + " in flow pattern: " + ifaceFlowPatternId);
    }

    public List<Source> getSource(String ifaceFlowPatternId, String ifaceTestScenarioId, String ifaceExecBlockId) throws FrameworkConfigurationException {
        return getIfaceExecBlock(ifaceFlowPatternId, ifaceTestScenarioId, ifaceExecBlockId).getSource();
    }

    public List<Target> getTarget(String ifaceFlowPatternId, String ifaceTestScenarioId, String ifaceExecBlockId) throws FrameworkConfigurationException {
        return getIfaceExecBlock(ifaceFlowPatternId, ifaceTestScenarioId, ifaceExecBlockId).getTarget();
    }

    public List<Operation> getOperations(String ifaceFlowPatternId, String ifaceTestScenarioId, String ifaceExecBlockId) throws FrameworkConfigurationException {
        ExecutionBlock executionBlock = MCFG.getExecutionBlock(ifaceFlowPatternId, ifaceTestScenarioId, ifaceExecBlockId);
        return executionBlock.getOperation();
    }

    public Operation getOperation(String ifaceFlowPatternId, String ifaceTestScenarioId, String ifaceExecBlockId, String operationName) throws FrameworkConfigurationException {
        for (Operation operation : getOperations(ifaceFlowPatternId, ifaceTestScenarioId, ifaceExecBlockId)) {
            if (operation.getName().value().equals(operationName)) {
                return operation;
            }
        }
        throw new FrameworkConfigurationException("No such operation found within interface configuration file: " + operationName + " in test scenario: " + ifaceTestScenarioId
                + " in flow pattern: " + ifaceFlowPatternId + " in execution block: " + ifaceExecBlockId);
    }

    public List<IfaceEndPoint> getIfaceEndPoints() {
        return XML_CONFIG.getIfaceEndPoints().getIfaceEndPoint();

    }

    public IfaceEndPoint getIfaceEndPoint(String endPointLocalIdRef) throws FrameworkConfigurationException {
        for (IfaceEndPoint ifaceEndPoint : getIfaceEndPoints()) {
            if (ifaceEndPoint.getEndPointLocalId().equals(endPointLocalIdRef)) {
                return ifaceEndPoint;
            }
        }
        throw new InterfaceConfigurationException("Endpoint identified by '" 
                + endPointLocalIdRef 
                + "' was not found in configuration file '" 
                + this.INTERFACE_CONFIG_FILE 
                + "'.");
    }

    /**
     *
     * @param ifaceExecBlock
     * @param execOn
     * @return
     * @throws FrameworkConfigurationException
     */
    public List<IfaceEndPoint> getIfaceEndPoint(IfaceExecBlock ifaceExecBlock, ExecuteOn execOn) throws FrameworkConfigurationException {       
        
        final List<IfaceEndPoint> ifaceEndPoints = new ArrayList<>();
        switch (execOn) {
            case SOURCE:
                for (Source source : ifaceExecBlock.getSource()) {
                   ifaceEndPoints.add(getIfaceEndPoint(source.getEndPointLocalIdRef()));
                }
                break;
            case TARGET:
                for (Target target : ifaceExecBlock.getTarget()) {
                    ifaceEndPoints.add(getIfaceEndPoint(target.getEndPointLocalIdRef()));
                }
                break;
            case NA:
                break;
            default:
                break;
        }
        
        if (execOn != ExecuteOn.NA && ifaceEndPoints.isEmpty()){
            throw new FrameworkConfigurationException("The lookup for relevant endpoints for execution block='"
            + ifaceExecBlock.getRefId() + "' resulted in empty list. Please review the configuration file: " 
                    + this.INTERFACE_CONFIG_FILE + ".");
        }
        return ifaceEndPoints;

    }

    public List<DbObject> getIfaceDbObjectList(String environmentName, IfaceExecBlock ifaceExecBlock, ExecuteOn execOn) throws FrameworkException{
        // Variables init
        List<IfaceEndPoint> ifaceEndPoints = getIfaceEndPoint(ifaceExecBlock,execOn);
        DBConfig dbConfig = null;
        DbObjects dbObjects = null;
        String[] envRefNames = environmentName.split("|");
        
        // Return object
        List<DbObject> dbObjectList = new ArrayList<>();
        
        // Get the database endpoint object
        for (IfaceEndPoint ifep : ifaceEndPoints){
            if (ifep.getDatabase() != null){
                dbConfig = ifep.getDatabase();
            }
        }
        
        // Error raised if endpoint object null
        if (dbConfig == null){
            String msg = "TODO";
            throw new FrameworkConfigurationException(msg);
        }
        
        // Get default db objects
        DefaultDbObjects defaultDbObjects = dbConfig.getDefaultDbObjects();
        // Get environment db objects list
        List<DBConfig.DbObjects> dbObjectsList = dbConfig.getDbObjects();
        
        // Seek db objects for selected environment
        boolean dbObjectsExists = false;
        for (DbObjects dbos : dbObjectsList){
            for (String envRefName : envRefNames){
                if (dbos.getEnvRefName().equals(envRefName)){
                    // ATTENTION: This will seek only first occurency, if multiple environment data source exists due to wrong 
                    // iface configuration, those will not be picked up. Should be added to configuration sanity check process.
                    dbObjectsExists = true;
                    for (DbObject dbo : dbos.getDbObject()){
                        // Return object
                        return dbos.getDbObject();
                    }
                    break;
                }
            }
        }
        
        // Execption if no dbObjects found for selected environment and no defaultDbObject exists
        if (!dbObjectsExists && defaultDbObjects == null){
            String msg = "TODO";
            throw new FrameworkConfigurationException(msg);
        } else {
            // Return default object
            return defaultDbObjects.getDbObject();
        }
    }
    
    public UTILConfig getUtilConfig() {
        return XML_CONFIG.getUtilConfig();
    }
    
    public File getComponentWorkingDir(String interfaceId, IfaceFlowPattern interfaceFlowPattern, String ifaceTestScenarioId, String componentDirName) throws FrameworkConfigurationException {
        File dir = getTestScenarioWorkingDir(interfaceId, interfaceFlowPattern, ifaceTestScenarioId);
        if (componentDirName != null) {
            dir = new File(dir, componentDirName);
        }
        return dir;
    }

    public File getTestScenarioWorkingDir(String interfaceId, IfaceFlowPattern interfaceFlowPattern, String ifaceTestScenarioId) throws FrameworkConfigurationException {
        File dir = new File(FCFG.getSoaTestHome(), interfaceId + "_" + FCFG.getValidFileSystemObjectName(MCFG.getInterface(interfaceId).getDescription()));
        dir = new File(dir, FrameworkConfiguration.FLOW_PATTERN_DIR_NAME_PREFIX + FCFG.getValidFileSystemObjectName(interfaceFlowPattern.getRefId()));
        dir = new File(dir, FCFG.getValidFileSystemObjectName(interfaceFlowPattern.getTestName()));
        dir = new File(dir, FCFG.getValidFileSystemObjectName(ifaceTestScenarioId));
        return dir;
    }
    
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.soatf.flow;

import com.ibm.soatf.component.ComponentResult;
import com.ibm.soatf.FrameworkConfiguration;
import com.ibm.soatf.FrameworkConfigurationException;
import com.ibm.soatf.InterfaceConfiguration;
import com.ibm.soatf.MasterConfiguration;
import com.ibm.soatf.config.iface.IfaceExecBlock;
import com.ibm.soatf.config.iface.IfaceFlowPattern;
import com.ibm.soatf.config.iface.IfaceTestScenario;
import com.ibm.soatf.config.iface.SOATFIfaceConfig;
import com.ibm.soatf.config.iface.SOATFIfaceConfig.IfaceEndPoints.IfaceEndPoint;
import com.ibm.soatf.config.iface.db.DBConfig;
import com.ibm.soatf.config.iface.ftp.FTPConfig;
import com.ibm.soatf.config.iface.jms.JMSConfig;
import com.ibm.soatf.config.iface.soap.SOAPConfig;
import com.ibm.soatf.config.iface.util.UTILConfig;
import com.ibm.soatf.config.master.Databases.Database.DatabaseInstance;
import com.ibm.soatf.config.master.FTPServers;
import com.ibm.soatf.config.master.OSBReporting;
import com.ibm.soatf.config.master.Operation;
import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance;
import com.ibm.soatf.component.database.DatabaseComponent;
import com.ibm.soatf.component.ftp.FTPComponent;
import com.ibm.soatf.component.jms.JMSComponent;
import com.ibm.soatf.component.soap.SOAPComponent;
import com.ibm.soatf.component.util.UtilityComponent;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author user
 */
public class FlowExecutor {
    private final Logger logger = LogManager.getLogger(FlowExecutor.class.getName());

    private String envName;
    private String interfaceId;
    private String interfaceFlowPatternId;
    private String interfaceTestScenarioId;
    private String interfaceExecutionBlockId;
    private String operationName;

    private final List<FlowExecutionListener> flowExecutionListeners = new ArrayList<>();
    
    private static final FrameworkConfiguration FCFG = FrameworkConfiguration.getInstance();
    private static final MasterConfiguration MCFG = MasterConfiguration.getInstance();
    private final InterfaceConfiguration ICFG;
    
    private boolean executedOnOperationLevel = false;

    public FlowExecutor(String envName, String interfaceId, String interfaceFlowPatternId, String interfaceTestScenarioId, String interfaceExecutionBlockId, String operationName) {
        this.envName = envName;
        this.interfaceId = interfaceId;
        this.interfaceFlowPatternId = interfaceFlowPatternId;
        this.interfaceTestScenarioId = interfaceTestScenarioId;
        this.interfaceExecutionBlockId = interfaceExecutionBlockId;
        this.operationName = operationName;
        ICFG = FCFG.getInterfaceConfig(interfaceId);
    }

    public FlowExecutor(String envName, String interfaceId, String interfaceFlowPatternId, String interfaceTestScenarioId, String interfaceExecutionBlockId) {
        this(envName, interfaceId, interfaceFlowPatternId, interfaceTestScenarioId, interfaceExecutionBlockId, null);
    }

    public FlowExecutor(String envName, String interfaceId, String interfaceFlowPatternId, String interfaceTestScenarioId) {
        this(envName, interfaceId, interfaceFlowPatternId, interfaceTestScenarioId, null, null);
    }

    public FlowExecutor(String envName, String interfaceId, String interfaceFlowPatternId) {
        this(envName, interfaceId, interfaceFlowPatternId, null, null, null);
    }

    public FlowExecutor(String envName, String interfaceId) {
        this(envName, interfaceId, null, null, null, null);
    }

    public List<ComponentResult> execute() {
        return execute(interfaceId, interfaceFlowPatternId, interfaceTestScenarioId, interfaceExecutionBlockId, operationName);
    }

    //concrete operation
    private List<ComponentResult> execute(String interfaceId, String ifaceFlowPatternId, String ifaceTestScenarioId, String ifaceExecutionBlockId, String operationName) {
        executedOnOperationLevel = true;
        
        if (operationName == null) {
            return execute(interfaceId, ifaceFlowPatternId, ifaceTestScenarioId, ifaceExecutionBlockId);
        } else {
            Operation operation = ICFG.getOperation(ifaceFlowPatternId, ifaceTestScenarioId, ifaceExecutionBlockId, operationName);
            fireOperationStarted(operation);
            
            String component = getComponentTypeName(operation.getName().value());
            logger.trace("Operation " + operation.getName() + " has been identified as a type of " + component + " component operation type.");
            ComponentResult componentResult = new ComponentResult();
            
            IfaceExecBlock ifaceExecBlock = ICFG.getIfaceExecBlock(ifaceFlowPatternId, ifaceTestScenarioId, ifaceExecutionBlockId);
            //ExecutionBlock executionBlock = MCFG.getExecutionBlock(interfaceFlowPatternId, interfaceTestScenarioId, interfaceExecutionBlockId);
            FlowPatternCompositeKey key = new FlowPatternCompositeKey(ifaceFlowPatternId, ifaceTestScenarioId, ifaceExecutionBlockId);
            //key.setDescriptor("INTERFACE");
            //key.setExecBlockDirection(executionBlock.getDirection());
            //key.setExecBlockSeqId(executionBlock.getSequenceId());
            key.setIfaceDesc(MCFG.getInterface(interfaceId).getDescription());
            key.setIfaceName(interfaceId);
            key.setSource(ifaceExecBlock.getSource());
            key.setTarget(ifaceExecBlock.getTarget());
            key.setTestName(ICFG.getIfaceFlowPattern(ifaceFlowPatternId).getTestName());
            
            //key.setTestScenarioExecBlockCount("" + ICFG.getInterfaceTestScenarios(interfaceFlowPatternId).size());
            //key.setTestScenarioType(MCFG.getTestScenario(interfaceFlowPatternId, interfaceTestScenarioId).getType());
            key.setUtilConfiguration(ICFG.getUtilConfig());
            //key.setWorkingDir(""); //???
            
            // variables
            DBConfig dbConfig = null;
            OracleFusionMiddlewareInstance ofmInstance = null;
            FTPConfig ftpConfig =  null;
            JMSConfig jmsConfig = null;
            SOAPConfig soapConfig = null;
            DatabaseComponent dbComp = null;
            switch (component) {
                case "DB":
                     for (SOATFIfaceConfig.IfaceEndPoints.IfaceEndPoint ifaceEndPoint : ICFG.getIfaceEndPoint(ifaceExecBlock, operation.getExecuteOn())){
                        if (ifaceEndPoint.getDatabase() != null){
                            dbConfig = ifaceEndPoint.getDatabase();
                            break;
                        } 
                    }
                    
 
                    // Database configuration
                    DatabaseInstance dbInstance = MCFG.getDatabaseInstance(envName, dbConfig.getRefId());
                    dbComp = new DatabaseComponent(dbInstance, dbConfig, componentResult, key);
                    dbComp.execute(operation);
                    break;
                case "FILE":
                    throw new FrameworkConfigurationException("File Component is not implemented yet!");
                case "FTP":
                    for (IfaceEndPoint ifaceEndPoint : ICFG.getIfaceEndPoint(ifaceExecBlock,operation.getExecuteOn())){
                        if (ifaceEndPoint.getFtpServer() != null){
                            ftpConfig = ifaceEndPoint.getFtpServer();
                            break;
                        } 
                    }
                    FTPServers.FtpServer.FtpServerInstance ftpServerInstance = MCFG.getFtpServerInstance(envName, ftpConfig.getRefId());
                    FTPComponent ftpComp = new FTPComponent(ifaceExecBlock, ftpServerInstance, ftpConfig, componentResult, key);
                    ftpComp.execute(operation);
                    break;
                /*case "OSB":
                    OSBConfiguration osbConfig;
                    if (operation.getExecuteOn().equals(SOURCE)) {
                        osbConfig = interfaceExecutionBlock.getSource().getOsb();
                    } else {
                        osbConfig = interfaceExecutionBlock.getTarget().getOsb();
                    }
                    OracleFusionMiddleware.OracleFusionMiddlewareInstance ofmInstance = MCFG.getOracleFusionMiddlewareInstance(envName);
                    OSBComponent osbComp = new OSBComponent(ofmInstance, osbConfig, componentResult, key);
                    osbComp.executeOperation(CompOperType.valueOf(operation.getName()));
                    break;
                        */
                case "JMS":
                    for (IfaceEndPoint ifaceEndPoint : ICFG.getIfaceEndPoint(ifaceExecBlock, operation.getExecuteOn())){
                        if (ifaceEndPoint.getJmsSubsystem() != null){
                            jmsConfig = ifaceEndPoint.getJmsSubsystem();
                            break;
                        } 
                    }
                    ofmInstance = MCFG.getOracleFusionMiddlewareInstance(envName);
                    JMSComponent jmsComp = new JMSComponent(ofmInstance, jmsConfig, componentResult, key);
                    jmsComp.executeOperation(operation);
                    break;
                case "SOAP":
                     for (IfaceEndPoint ifaceEndPoint : ICFG.getIfaceEndPoint(ifaceExecBlock, operation.getExecuteOn())){
                        if (ifaceEndPoint.getSoap() != null){
                            soapConfig = ifaceEndPoint.getSoap();
                            break;
                        } 
                    }
                   
                    ofmInstance = MCFG.getOracleFusionMiddlewareInstance(envName);
                    SOAPComponent soapComp = new SOAPComponent(ofmInstance, soapConfig, componentResult, key);
                    soapComp.execute(operation);
                    break;
                case "UTIL":
                    UTILConfig utilConfig = ICFG.getUtilConfig();
                    OSBReporting.OsbReportingInstance osbReportingInstance = MCFG.getOSBReportingInstance(envName);
                    UtilityComponent utilComp = new UtilityComponent(ifaceExecBlock, osbReportingInstance, utilConfig, componentResult, key);
                    utilComp.executeOperation(operation);
                    break;
                default:
                    throw new FrameworkConfigurationException("Unsupported type of component to be created: " + component);
            }
            componentResult.toString();
            fireOperationFinished(operation, componentResult);
            List<ComponentResult> resultList = new ArrayList<>();
            resultList.add(componentResult);
            return resultList;
        }
    }
    
    //operations - inside execution block
    private List<ComponentResult> execute(String interfaceId, String interfaceFlowPatternId, String interfaceTestScenarioId, String interfaceExecutionBlockId) {
        if (interfaceExecutionBlockId == null) {
            return execute(interfaceId, interfaceFlowPatternId, interfaceTestScenarioId);
        } else {
            List<ComponentResult> resultList = new ArrayList<>();
            for (Operation o : ICFG.getOperations(interfaceFlowPatternId, interfaceTestScenarioId, interfaceExecutionBlockId)) {
                List<ComponentResult> r = execute(interfaceId, interfaceFlowPatternId, interfaceTestScenarioId, interfaceExecutionBlockId, o.getName().value());
                resultList.addAll(r);
            }
            return resultList;
        }
    }

    //execution blocks - inside test scenario
    private List<ComponentResult> execute(String interfaceId, String interfaceFlowPatternId, String interfaceTestScenarioId) {
        if (interfaceTestScenarioId == null) {
            return execute(interfaceId, interfaceFlowPatternId);
        } else {
            List<ComponentResult> resultList = new ArrayList<>();
            for (IfaceExecBlock ieb : ICFG.getIfaceExecBlocks(interfaceFlowPatternId, interfaceTestScenarioId)) {
                final List<ComponentResult> r = execute(interfaceId, interfaceFlowPatternId, interfaceTestScenarioId, ieb.getRefId());
                resultList.addAll(r);
            }
            return resultList;
        }
    }

    //test scenarios - inside flow pattern
    private List<ComponentResult> execute(String interfaceId, String interfaceFlowPatternId) {
        if (interfaceFlowPatternId == null) {
            return execute(interfaceId);
        } else {
            List<ComponentResult> resultList = new ArrayList<>();
            for (IfaceTestScenario its : ICFG.getIfaceTestScenarios(interfaceFlowPatternId)) {
                List<ComponentResult> r = execute(interfaceId, interfaceFlowPatternId, its.getRefId());
                resultList.addAll(r);
            }
            return resultList;
        }
    }

    //flow patterns - whole interface
    private List<ComponentResult> execute(String interfaceId) {
        if (interfaceId == null) {
            throw new FlowExecutionRuntimeException("At least an interface must be selected when trying to execute test framework");
        } else {
            List<ComponentResult> resultList = new ArrayList<>();
            for (IfaceFlowPattern ifp : ICFG.getIfaceFlowPatterns()) {
                List<ComponentResult> r = execute(interfaceId, ifp.getRefId());
                resultList.addAll(r);
            }
            return resultList;
        }
    }

    public void addFlowExecutionListener(FlowExecutionListener l) {
        flowExecutionListeners.add(l);
    }

    public void removeFlowExecutionListener(FlowExecutionListener l) {
        flowExecutionListeners.remove(l);
    }

    private void fireOperationStarted(Operation operation) {
        for (FlowExecutionListener l : flowExecutionListeners) {
            l.operationStarted(new FlowExecutionEvent(operation));
        }
    }

    private void fireOperationFinished(Operation operation, ComponentResult componentResult) {
        for (FlowExecutionListener l : flowExecutionListeners) {
            l.operationFinished(new FlowExecutionEvent(operation, componentResult));
        }
    }
    
    private String getComponentTypeName(String operationName) {
        return operationName.substring(0, operationName.indexOf("_"));
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    public void setInterfaceFlowPatternId(String interfaceFlowPatternId) {
        this.interfaceFlowPatternId = interfaceFlowPatternId;
    }

    public void setInterfaceTestScenarioId(String interfaceTestScenarioId) {
        this.interfaceTestScenarioId = interfaceTestScenarioId;
    }

    public void setInterfaceExecutionBlockId(String interfaceExecutionBlockId) {
        this.interfaceExecutionBlockId = interfaceExecutionBlockId;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }
}

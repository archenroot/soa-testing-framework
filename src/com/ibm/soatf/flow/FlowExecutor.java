/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.soatf.flow;

import com.ibm.soatf.FrameworkException;
import com.ibm.soatf.FrameworkExecutionException;
import com.ibm.soatf.component.database.DatabaseComponent;
import com.ibm.soatf.component.ftp.FTPComponent;
import com.ibm.soatf.component.jms.JMSComponent;
import com.ibm.soatf.component.soap.SOAPComponent;
import com.ibm.soatf.component.util.UtilityComponent;
import com.ibm.soatf.config.ConfigurationManager;
import com.ibm.soatf.config.FrameworkConfigurationException;
import com.ibm.soatf.config.InterfaceConfiguration;
import com.ibm.soatf.config.MasterConfiguration;
import com.ibm.soatf.config.iface.IfaceExecBlock;
import com.ibm.soatf.config.iface.IfaceFlowPattern;
import com.ibm.soatf.config.iface.IfaceTestScenario;
import com.ibm.soatf.config.iface.SOATFIfaceConfig;
import com.ibm.soatf.config.iface.SOATFIfaceConfig.IfaceEndPoints.IfaceEndPoint;
import com.ibm.soatf.config.iface.db.DBConfig;
import com.ibm.soatf.config.iface.db.DbObject;
import com.ibm.soatf.config.iface.ftp.FTPConfig;
import com.ibm.soatf.config.iface.jms.JMSConfig;
import com.ibm.soatf.config.iface.soap.SOAPConfig;
import com.ibm.soatf.config.iface.util.UTILConfig;
import com.ibm.soatf.config.master.Databases.Database.DatabaseInstance;
import com.ibm.soatf.config.master.FTPServers;
import com.ibm.soatf.config.master.FTPServers.FtpServer.Directories;
import com.ibm.soatf.config.master.Interface;
import com.ibm.soatf.config.master.OSBReporting;
import com.ibm.soatf.config.master.Operation;
import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance;
import com.ibm.soatf.config.master.TestScenario;
import java.io.File;
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
    private String ifaceId;

    private final List<FlowExecutionListener> flowExecutionListeners = new ArrayList<>();
    private final MasterConfiguration MCFG;
    private final InterfaceConfiguration ICFG;
    private final boolean inboundOnly;

    private Interface iface;
    private IfaceFlowPattern ifaceFlowPattern;
    private IfaceTestScenario ifaceTestScenario;
    private IfaceExecBlock ifaceExecutionBlock;
    private Operation operation;

    public FlowExecutor(boolean inboundOnly, String envName, String interfaceId) throws FrameworkConfigurationException {
        this.inboundOnly = inboundOnly;
        this.envName = envName;
        this.ifaceId = interfaceId;
        MCFG = ConfigurationManager.getInstance().getMasterConfig();
        ICFG = MCFG.getInterfaceConfig(interfaceId);
        iface = MCFG.getInterface(interfaceId);
    }

    public FlowExecutor(String envName, String interfaceId) throws FrameworkConfigurationException {
        this(false, envName, interfaceId);
    }

    public void execute() throws FrameworkException {
        execute(iface, ifaceFlowPattern, ifaceTestScenario, ifaceExecutionBlock, operation);
    }

    private void execute(Interface interfaceObj, IfaceFlowPattern interfaceFlowPattern, IfaceTestScenario interfaceTestScenario, IfaceExecBlock interfaceExecutionBlock, Operation operation) throws FrameworkException {
        if (operation == null) {
            execute(interfaceObj, interfaceFlowPattern, interfaceTestScenario, interfaceExecutionBlock);
        } else {
            TestScenario.ExecutionBlock executionBlock = MCFG.getExecutionBlock(interfaceFlowPattern.getRefId(), interfaceTestScenario.getRefId(), interfaceExecutionBlock.getRefId());
            if (inboundOnly && "OUTBOUND".equalsIgnoreCase(executionBlock.getDirection())) {
                return;
            }
            OperationResult.reset();
            final String operationName = operation.getName().value();
            fireOperationStarted(operationName);
            try {
                String component = getComponentTypeName(operationName);
                logger.trace("Operation " + operationName + " has been identified as a type of " + component + " component operation type.");

                FlowPatternCompositeKey key = new FlowPatternCompositeKey(interfaceFlowPattern.getRefId(), interfaceTestScenario.getRefId(), interfaceExecutionBlock.getRefId());
                //key.setDescriptor("INTERFACE");
                //key.setExecBlockDirection(executionBlock.getDirection());
                //key.setExecBlockSeqId(executionBlock.getSequenceId());
                key.setIfaceDesc(MCFG.getInterface(ifaceId).getDescription());
                key.setIfaceName(ifaceId);
                key.setSource(interfaceExecutionBlock.getSource());
                key.setTarget(interfaceExecutionBlock.getTarget());
                key.setTestName(interfaceFlowPattern.getInstanceMetadata().getTestName());

                //key.setTestScenarioExecBlockCount("" + ICFG.getInterfaceTestScenarios(interfaceFlowPatternId).size());
                //key.setTestScenarioType(MCFG.getTestScenario(interfaceFlowPatternId, interfaceTestScenarioId).getType());
                key.setUtilConfiguration(ICFG.getUtilConfig());
                //key.setWorkingDir(""); //???

                // variables
                DBConfig dbConfig = null;
                OracleFusionMiddlewareInstance ofmInstance = null;
                FTPConfig ftpConfig = null;
                JMSConfig jmsConfig = null;
                SOAPConfig soapConfig = null;
                DatabaseComponent dbComp = null;
                File workingDir = ICFG.getComponentWorkingDir(ifaceId, interfaceFlowPattern, interfaceTestScenario.getRefId(), component.toLowerCase());
                File rootWorkingDir = ICFG.getComponentWorkingDir(ifaceId, interfaceFlowPattern, interfaceTestScenario.getRefId(), null);
                switch (component) {
                    case "DB":
                        List<DbObject> dbObjects = ICFG.getIfaceDbObjectList(this.envName,interfaceExecutionBlock, operation.getExecuteOn());
                        if (dbObjects.isEmpty()) {
                            String msg = "There exists no Database endpoint within config.xml file for interface "
                                    + this.ifaceId + ", execution block " + interfaceExecutionBlock.getRefId()
                                    + " targeting " + operation.getExecuteOn().value() + ".";
                            logger.error(msg);
                            throw new FrameworkConfigurationException(msg);
                        }
                        // Database configuration
                        DatabaseInstance dbInstance = MCFG.getDatabaseInstance(envName, dbConfig.getRefId());
                        dbComp = new DatabaseComponent(dbInstance, dbObjects, workingDir);
                        dbComp.execute(operation);
                        break;
                    case "FILE":
                        throw new FrameworkConfigurationException("File Component is not implemented yet!");
                    case "FTP":

                        for (IfaceEndPoint ifaceEndPoint : ICFG.getIfaceEndPoint(interfaceExecutionBlock, operation.getExecuteOn())) {
                            if (ifaceEndPoint.getFtpServer() != null) {
                                ftpConfig = ifaceEndPoint.getFtpServer();
                                break;
                            }
                        }

                        FTPServers.FtpServer.FtpServerInstance ftpServerInstance = MCFG.getFtpServerInstance(envName, ftpConfig.getRefId());
                        Directories directories = MCFG.getFTPServerDirectories(ftpConfig.getRefId());

                        FTPComponent ftpComp = new FTPComponent(
                                interfaceExecutionBlock,
                                ftpServerInstance,
                                ftpConfig,
                                ftpDefaultConfig,
                                directories,
                                workingDir);
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
                        for (IfaceEndPoint ifaceEndPoint : ICFG.getIfaceEndPoint(interfaceExecutionBlock, operation.getExecuteOn())) {
                            if (ifaceEndPoint.getJmsSubsystem() != null) {
                                jmsConfig = ifaceEndPoint.getJmsSubsystem();
                                break;
                            }
                        }
                        ofmInstance = MCFG.getOracleFusionMiddlewareInstance(envName);
                        JMSComponent jmsComp = new JMSComponent(ofmInstance, jmsConfig, key, workingDir);
                        jmsComp.execute(operation);
                        break;
                    case "SOAP":
                        for (IfaceEndPoint ifaceEndPoint : ICFG.getIfaceEndPoint(interfaceExecutionBlock, operation.getExecuteOn())) {
                            if (ifaceEndPoint.getSoap() != null) {
                                soapConfig = ifaceEndPoint.getSoap();
                                break;
                            }
                        }

                        ofmInstance = MCFG.getOracleFusionMiddlewareInstance(envName);
                        SOAPComponent soapComp = new SOAPComponent(ofmInstance, soapConfig, workingDir);
                        soapComp.execute(operation);
                        break;
                    case "UTIL":
                        UTILConfig utilConfig = ICFG.getUtilConfig();
                        OSBReporting.OsbReportingInstance osbReportingInstance = MCFG.getOSBReportingInstance(envName);
                        UtilityComponent utilComp = new UtilityComponent(interfaceExecutionBlock, osbReportingInstance, utilConfig, key, rootWorkingDir);
                        utilComp.executeOperation(operation);
                        break;
                    default:
                        throw new FrameworkConfigurationException("Unsupported type of component to be created: " + component);
                }
            } finally {
                fireOperationFinished(operationName, OperationResult.getInstance());
            }
        }
    }

    private void execute(Interface interfaceObj, IfaceFlowPattern interfaceFlowPattern, IfaceTestScenario interfaceTestScenario, IfaceExecBlock interfaceExecutionBlock) throws FrameworkException {
        if (interfaceExecutionBlock == null) {
            execute(interfaceObj, interfaceFlowPattern, interfaceTestScenario);
        } else {
            TestScenario.ExecutionBlock executionBlock = MCFG.getExecutionBlock(interfaceFlowPattern.getRefId(), interfaceTestScenario.getRefId(), interfaceExecutionBlock.getRefId());
            for (Operation o : executionBlock.getOperation()) {
                execute(interfaceObj, interfaceFlowPattern, interfaceTestScenario, interfaceExecutionBlock, o);
            }
        }
    }

    private void execute(Interface interfaceObj, IfaceFlowPattern interfaceFlowPattern, IfaceTestScenario interfaceTestScenario) throws FrameworkException {
        if (interfaceTestScenario == null) {
            execute(interfaceObj, interfaceFlowPattern);
        } else {
            for (IfaceExecBlock ieb : interfaceTestScenario.getIfaceExecBlock()) {
                execute(interfaceObj, interfaceFlowPattern, interfaceTestScenario, ieb);
            }
        }
    }

    private void execute(Interface interfaceObj, IfaceFlowPattern interfaceFlowPattern) throws FrameworkException {
        if (interfaceFlowPattern == null) {
            execute(interfaceObj);
        } else {
            for (IfaceTestScenario its : interfaceFlowPattern.getIfaceTestScenario()) {
                execute(interfaceObj, interfaceFlowPattern, its);
            }
        }
    }

    private void execute(Interface interfaceObj) throws FrameworkException {
        if (interfaceObj == null) {
            throw new FrameworkExecutionException("At least an interface must be selected when trying to execute test framework");
        } else {
            InterfaceConfiguration interfaceConfig = MCFG.getInterfaceConfig(interfaceObj);
            for (IfaceFlowPattern ifp : interfaceConfig.getIfaceFlowPatterns()) {
                execute(interfaceObj, ifp);
            }
        }
    }

    public void addFlowExecutionListener(FlowExecutionListener l) {
        flowExecutionListeners.add(l);
    }

    public void removeFlowExecutionListener(FlowExecutionListener l) {
        flowExecutionListeners.remove(l);
    }

    private void fireOperationStarted(String operationName) {
        for (FlowExecutionListener l : flowExecutionListeners) {
            l.operationStarted(new FlowExecutionEvent(operationName));
        }
    }

    private void fireOperationFinished(String operationName, OperationResult componentResult) {
        for (FlowExecutionListener l : flowExecutionListeners) {
            l.operationFinished(new FlowExecutionEvent(operationName, componentResult));
        }
    }

    private String getComponentTypeName(String operationName) {
        return operationName.substring(0, operationName.indexOf("_"));
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public void setIfaceId(String ifaceId) {
        this.ifaceId = ifaceId;
    }

    public void setIface(Interface iface) {
        this.iface = iface;
    }

    public void setIfaceFlowPattern(IfaceFlowPattern ifaceFlowPattern) {
        this.ifaceFlowPattern = ifaceFlowPattern;
    }

    public void setIfaceTestScenario(IfaceTestScenario ifaceTestScenario) {
        this.ifaceTestScenario = ifaceTestScenario;
    }

    public void setIfaceExecutionBlock(IfaceExecBlock ifaceExecutionBlock) {
        this.ifaceExecutionBlock = ifaceExecutionBlock;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }
}

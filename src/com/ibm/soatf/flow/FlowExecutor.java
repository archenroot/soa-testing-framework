/*
 * Copyright (C) 2013 zANGETSu
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
package com.ibm.soatf.flow;

import com.ibm.soatf.FrameworkException;
import com.ibm.soatf.component.database.DatabaseComponent;
import com.ibm.soatf.component.file.FileComponent;
import com.ibm.soatf.component.ftp.FTPComponent;
import com.ibm.soatf.component.jms.JmsComponent;
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
import com.ibm.soatf.config.iface.soap.EnvelopeConfig;
import com.ibm.soatf.config.iface.soap.SOAPConfig;
import com.ibm.soatf.config.iface.util.UTILConfig;
import com.ibm.soatf.config.master.Databases.Database.DatabaseInstance;
import com.ibm.soatf.config.master.FTPServers;
import com.ibm.soatf.config.master.FTPServers.FtpServer.Directories;
import com.ibm.soatf.config.master.Interface;
import com.ibm.soatf.config.master.OSBReporting;
import com.ibm.soatf.config.master.Operation;
import static com.ibm.soatf.config.master.OperationName.SOAP_DISABLE_SERVICE;
import static com.ibm.soatf.config.master.OperationName.SOAP_ENABLE_SERVICE;
import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance;
import com.ibm.soatf.config.master.TestScenario;
import com.ibm.soatf.reporting.ReportComponent;
import com.ibm.soatf.tool.Utils;
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

    /**
     *
     * @param inboundOnly
     * @param envName
     * @param interfaceId
     * @throws FrameworkConfigurationException
     */
    public FlowExecutor(boolean inboundOnly, String envName, String interfaceId) throws FrameworkConfigurationException {
        this.inboundOnly = inboundOnly;
        this.envName = envName;
        this.ifaceId = interfaceId;
        MCFG = ConfigurationManager.getInstance().getMasterConfig();
        ICFG = MCFG.getInterfaceConfig(interfaceId);
        iface = MCFG.getInterface(interfaceId);
    }

    /**
     *
     * @param envName
     * @param interfaceId
     * @throws FrameworkConfigurationException
     */
    public FlowExecutor(String envName, String interfaceId) throws FrameworkConfigurationException {
        this(false, envName, interfaceId);
    }

    /**
     *
     * @throws FrameworkException
     */
    public void execute() throws FrameworkException {
        OperationResult.reset();
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
            OperationResult.nextInstance(); //mozno tu by mal ist scenarioName, blockName i operation ako argument
            OperationResult.getInstance().setScenarioName(interfaceTestScenario.getRefId());
            OperationResult.getInstance().setExecBlockName(interfaceExecutionBlock.getRefId());
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
                        for (SOATFIfaceConfig.IfaceEndPoints.IfaceEndPoint ifaceEndPoint : ICFG.getIfaceEndPoint(interfaceExecutionBlock, operation.getExecuteOn())) {
                            if (ifaceEndPoint.getDatabase() != null) {
                                dbConfig = ifaceEndPoint.getDatabase();
                                break;
                            }
                        }
                        List<DbObject> dbObjects = ICFG.getIfaceDbObjectList(this.envName, interfaceExecutionBlock, operation.getExecuteOn());
                        if (dbObjects.isEmpty()) {
                            String msg = "There exists no Database endpoint within config.xml file for interface "
                                    + this.ifaceId + ", execution block " + interfaceExecutionBlock.getRefId()
                                    + " targeting " + operation.getExecuteOn().value() + ".";
                            logger.error(msg);
                            throw new FrameworkConfigurationException(msg);
                        }
                        
                        // Database configuration
                        DatabaseInstance dbInstance = MCFG.getDatabaseInstance(envName, dbConfig.getRefId());
                        dbComp = new DatabaseComponent(dbInstance, dbObjects, workingDir, dbConfig.getRefId());
                        dbComp.execute(operation);

                        // Database configuration
                        break;
                    case "FILE":
                        ofmInstance = MCFG.getOracleFusionMiddlewareInstance(envName);
                        com.ibm.soatf.config.iface.file.File file = ICFG.getFile(envName, interfaceExecutionBlock, operation.getExecuteOn());
                        FileComponent fileComp = new FileComponent(ofmInstance, file, workingDir);
                        fileComp.execute(operation);
                        break;
                    case "FTP":
                        for (IfaceEndPoint ifaceEndPoint : ICFG.getIfaceEndPoint(interfaceExecutionBlock, operation.getExecuteOn())) {
                            if (ifaceEndPoint.getFtpServer() != null) {
                                ftpConfig = ifaceEndPoint.getFtpServer();
                                break;
                            }
                        }
                        
                        FTPConfig.File ftpFile = ICFG.getIfaceFtpFile(this.envName, interfaceExecutionBlock, operation.getExecuteOn());
                        if (ftpFile == null) {
                            String msg = "There exists no FTPServer.File within config.xml file for interface "
                                    + this.ifaceId + ", execution block " + interfaceExecutionBlock.getRefId()
                                    + " targeting " + operation.getExecuteOn().value() + ".";
                            logger.error(msg);
                            throw new FrameworkConfigurationException(msg);
                        }
                        
                        FTPServers.FtpServer.FtpServerInstance ftpServerInstance = MCFG.getFtpServerInstance(envName, ftpConfig.getRefId());
                        Directories directories = MCFG.getFTPServerDirectories(ftpConfig.getRefId());

                        FTPComponent ftpComp = new FTPComponent(
                                interfaceExecutionBlock,
                                ftpServerInstance,
                                ftpFile,
                                directories,
                                workingDir);
                        ftpComp.execute(operation);
                        break;
                    /*
                        Deprecated - included in SOAP, but keeped here as soon as 
                        future design might split SOAP into SOAP, OSB and SOA, 
                        but currently I am not sure about how to make it done.
                        I have creational crisis in the moment....!!!
                        
                        case "OSB":
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
                        JmsComponent jmsComp = new JmsComponent(ofmInstance, jmsConfig, key, workingDir);
                        jmsComp.execute(operation);
                        break;
                    case "SOAP":
                        soapConfig = ICFG.getSoapConfig(interfaceExecutionBlock, operation.getExecuteOn());
                        ofmInstance = MCFG.getOracleFusionMiddlewareInstance(envName);
                        /* This is workaround as soon as we sometimes do not provide any envelope config.
                        * The particular case is related to OSB or SOA service management operations,
                        * where the envelope itself is not required
                        */
                        final List<EnvelopeConfig.Element> soapEnvelopeElements;
                        if (operation.getName() == SOAP_DISABLE_SERVICE || operation.getName() == SOAP_ENABLE_SERVICE){
                            soapEnvelopeElements = new ArrayList<>();
                        } else {
                            soapEnvelopeElements = ICFG.getSoapEnvelopeElements(envName, interfaceExecutionBlock, operation.getExecuteOn());
                        }
                        SOAPComponent soapComp = new SOAPComponent(ofmInstance, soapConfig, workingDir, soapEnvelopeElements);
                        soapComp.execute(operation);
                        break;
                    case "UTIL":
                        UTILConfig utilConfig = ICFG.getUtilConfig();
                        OSBReporting.OsbReportingInstance osbReportingInstance = MCFG.getOSBReportingInstance(envName);
                        UtilityComponent utilComp = new UtilityComponent(envName,interfaceExecutionBlock, osbReportingInstance, utilConfig, key, rootWorkingDir,operation);
                        utilComp.execute(operation);
                        break;
                    case "REPORT":
                        ReportComponent reportComp = new ReportComponent(ConfigurationManager.getInstance().getFrameworkConfig().getMasterConfigFile(), MCFG.getIfaceConfigFile(interfaceObj), interfaceFlowPattern.getRefId(), interfaceFlowPattern.getInstanceMetadata().getTestName(), workingDir);
                        reportComp.execute(operation);
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
                OperationResult.reset();
                execute(interfaceObj, ifp);
            }
        }
    }

    /**
     *
     * @param l
     */
    public void addFlowExecutionListener(FlowExecutionListener l) {
        flowExecutionListeners.add(l);
    }

    /**
     *
     * @param l
     */
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

    /**
     *
     * @param envName
     */
    public void setEnvName(String envName) {
        this.envName = envName;
    }

    /**
     *
     * @param ifaceId
     */
    public void setIfaceId(String ifaceId) {
        this.ifaceId = ifaceId;
    }

    /**
     *
     * @param iface
     */
    public void setIface(Interface iface) {
        this.iface = iface;
    }

    /**
     *
     * @param ifaceFlowPattern
     */
    public void setIfaceFlowPattern(IfaceFlowPattern ifaceFlowPattern) {
        this.ifaceFlowPattern = ifaceFlowPattern;
    }

    /**
     *
     * @param ifaceTestScenario
     */
    public void setIfaceTestScenario(IfaceTestScenario ifaceTestScenario) {
        this.ifaceTestScenario = ifaceTestScenario;
    }

    /**
     *
     * @param ifaceExecutionBlock
     */
    public void setIfaceExecutionBlock(IfaceExecBlock ifaceExecutionBlock) {
        this.ifaceExecutionBlock = ifaceExecutionBlock;
    }

    /**
     *
     * @param operation
     */
    public void setOperation(Operation operation) {
        this.operation = operation;
    }
}

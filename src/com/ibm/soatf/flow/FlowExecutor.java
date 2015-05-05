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
import com.ibm.soatf.component.email.EmailComponent;
import com.ibm.soatf.component.file.FileComponent;
import com.ibm.soatf.component.ftp.FTPComponent;
import com.ibm.soatf.component.jms.JmsComponent;
import com.ibm.soatf.component.reporting.ReportComponent;
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
import com.ibm.soatf.config.iface.email.EMAILConfig;
import com.ibm.soatf.config.iface.ftp.FTPConfig;
import com.ibm.soatf.config.iface.jms.JMSConfig;
import com.ibm.soatf.config.iface.soap.EnvelopeConfig;
import com.ibm.soatf.config.iface.soap.SOAPConfig;
import com.ibm.soatf.config.iface.util.UTILConfig;
import com.ibm.soatf.config.master.Databases.Database.DatabaseInstance;
import com.ibm.soatf.config.master.EmailServers;
import com.ibm.soatf.config.master.ExecBlockOperation;
import com.ibm.soatf.config.master.ExecuteOn;
import com.ibm.soatf.config.master.FTPServers;
import com.ibm.soatf.config.master.FTPServers.FtpServer.Directories;
import com.ibm.soatf.config.master.FlowPatternPreOrPostExecutionBlock;
import com.ibm.soatf.config.master.Interface;
import com.ibm.soatf.config.master.OSBReporting;
import com.ibm.soatf.config.master.Operation;
import static com.ibm.soatf.config.master.OperationName.SOAP_DISABLE_SERVICE;
import static com.ibm.soatf.config.master.OperationName.SOAP_ENABLE_SERVICE;
import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance;
import com.ibm.soatf.config.master.PreOrPostExecBlockOperation;
import com.ibm.soatf.config.master.TestScenario;
import com.ibm.soatf.config.master.TestScenarioPreOrPostExecutionBlock;
import com.ibm.soatf.flow.OperationResult.CommonResult;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
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

    private Interface selectedInterface;
    private IfaceFlowPattern selectedFlowPattern;
    private IfaceTestScenario selectedTestScenario;
    private IfaceExecBlock selectedExecutionBlock;
    private Operation selectedOperation;
    private static IfaceTestScenario lastTestScenario;
    
    private static Date actualRunDate = null;
    private static java.sql.Timestamp actualRunDBDate = null;
    
    private boolean stopped = false;
    
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
        selectedInterface = MCFG.getInterface(interfaceId);
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
    public void execute() throws Throwable {
        OperationResult.reset();
        if (actualRunDate == null || selectedTestScenario != lastTestScenario || selectedExecutionBlock == null) {
            lastTestScenario = selectedTestScenario;
            actualRunDate = new Date();
        }
        setActualRunDBDate(null);        
        execute(this.selectedInterface, this.selectedFlowPattern, this.selectedTestScenario, this.selectedExecutionBlock, this.selectedOperation);
    }

    private void execute(Interface interfaceObj, IfaceFlowPattern interfaceFlowPattern, IfaceTestScenario interfaceTestScenario, IfaceExecBlock interfaceExecutionBlock, Operation operation) throws FrameworkException {
        if (operation == null) {
            execute(interfaceObj, interfaceFlowPattern, interfaceTestScenario, interfaceExecutionBlock);
        } else {
            TestScenario.ExecutionBlock executionBlock = MCFG.getExecutionBlock(interfaceFlowPattern.getRefId(), interfaceTestScenario.getRefId(), interfaceExecutionBlock.getRefId());
            if (inboundOnly && "OUTBOUND".equalsIgnoreCase(executionBlock.getDirection())) {
                return;
            }

            executeOperation(interfaceObj, interfaceFlowPattern, interfaceTestScenario, interfaceExecutionBlock, operation);
        }
    }
    
    private void scenarioPreOrPostBlockExecute(Interface interfaceObj, IfaceFlowPattern interfaceFlowPattern, IfaceTestScenario interfaceTestScenario, TestScenarioPreOrPostExecutionBlock executionBlock, PreOrPostExecBlockOperation operation) throws FrameworkException {
        if (executionBlock != null && operation != null) {            
            IfaceExecBlock ifaceExecBlock = null;
            if (!ExecuteOn.NA.equals(operation.getExecuteOn())) {
                ifaceExecBlock = ICFG.getIfaceExecBlock(interfaceTestScenario, operation.getExecutionBlockRef());
            }
            executeOperation(interfaceObj, interfaceFlowPattern, interfaceTestScenario, ifaceExecBlock, (Operation)operation);
        }
    } 
    
    private void flowPatternPreOrPostBlockExecute(Interface interfaceObj, IfaceFlowPattern interfaceFlowPattern, FlowPatternPreOrPostExecutionBlock executionBlock, Operation operation) throws FrameworkException {
        if (executionBlock != null && operation != null) {
            executeFlowPatternOperation(interfaceObj, interfaceFlowPattern, operation);
        }
    }    

    private void execute(Interface interfaceObj, IfaceFlowPattern interfaceFlowPattern, IfaceTestScenario interfaceTestScenario, IfaceExecBlock interfaceExecutionBlock) throws FrameworkException {
        if (interfaceExecutionBlock == null) {
            execute(interfaceObj, interfaceFlowPattern, interfaceTestScenario);
        } else {            
            TestScenario.ExecutionBlock executionBlock = MCFG.getExecutionBlock(interfaceFlowPattern.getRefId(), interfaceTestScenario.getRefId(), interfaceExecutionBlock.getRefId());
            for (Operation o : executionBlock.getOperation()) {
                if (stopped) break;                
                execute(interfaceObj, interfaceFlowPattern, interfaceTestScenario, interfaceExecutionBlock, o);
            }
        }
    }
    
    private void scenarioPreOrPostBlockExecute(Interface interfaceObj, IfaceFlowPattern interfaceFlowPattern, IfaceTestScenario interfaceTestScenario, TestScenarioPreOrPostExecutionBlock executionBlock) throws FrameworkException {
        if (executionBlock != null) {
            for (PreOrPostExecBlockOperation o : executionBlock.getOperation()) {
                scenarioPreOrPostBlockExecute(interfaceObj, interfaceFlowPattern, interfaceTestScenario, executionBlock, o);
            }
        }
    }
    
    private void flowPatternPreOrPostBlockExecute(Interface interfaceObj, IfaceFlowPattern interfaceFlowPattern, FlowPatternPreOrPostExecutionBlock executionBlock) throws FrameworkException {
        if (executionBlock != null) {
            for (Operation o : executionBlock.getOperation()) {
                flowPatternPreOrPostBlockExecute(interfaceObj, interfaceFlowPattern, executionBlock, o);
            }
        }
    }

    private void execute(Interface interfaceObj, IfaceFlowPattern interfaceFlowPattern, IfaceTestScenario interfaceTestScenario) throws FrameworkException {
        if (interfaceTestScenario == null) {
            execute(interfaceObj, interfaceFlowPattern);
        } else {
            final String fpId = interfaceFlowPattern.getRefId();
            final String tId = interfaceTestScenario.getRefId();
            //for now, skip all scenarios that are flagged as nonStandard in master-config
            final TestScenario testScenario = MCFG.getTestScenario(fpId, tId);
            if (testScenario.isNonStandard()) {
                logger.warn("Execution of non-standard scenarios is not supported right now. Skipping scenario \""
                        + tId + "\" in flow pattern \"" + fpId + "\" referenced in interface \""
                        + interfaceObj.getName() + " - " + interfaceObj.getDescription() + "\"");
                return;
            }
            if (interfaceTestScenario != lastTestScenario) {
                lastTestScenario = interfaceTestScenario;
                actualRunDate = new Date();
            }            
            if (interfaceTestScenario.getIfaceExecBlock() != null && interfaceTestScenario.getIfaceExecBlock().size() > 0) {
                //if one scenario was selected, run flow pattern pre/post here also
                if (selectedTestScenario != null)  {
                    FlowPatternPreOrPostExecutionBlock patternPreExecutionBlock = MCFG.getFlowPattern(interfaceFlowPattern.getRefId()).getPreExecutionBlock();
                    flowPatternPreOrPostBlockExecute(interfaceObj, interfaceFlowPattern, patternPreExecutionBlock);    
                }
                fireBlockChanged(tId);
                //scenario pre-execution block
                TestScenarioPreOrPostExecutionBlock preExecutionBlock = MCFG.getPreExecutionBlock(interfaceFlowPattern.getRefId(), interfaceTestScenario.getRefId());
                scenarioPreOrPostBlockExecute(interfaceObj, interfaceFlowPattern, interfaceTestScenario, preExecutionBlock);
                //scenario execution blocks
                try {
                    for (IfaceExecBlock ieb : interfaceTestScenario.getIfaceExecBlock()) {
                        if (stopped) break;                        
                        execute(interfaceObj, interfaceFlowPattern, interfaceTestScenario, ieb);
                    }
                } finally {
                    //scenario post-execution block
                    TestScenarioPreOrPostExecutionBlock postExecutionBlock = MCFG.getPostExecutionBlock(interfaceFlowPattern.getRefId(), interfaceTestScenario.getRefId());
                    scenarioPreOrPostBlockExecute(interfaceObj, interfaceFlowPattern, interfaceTestScenario, postExecutionBlock);
                    //if one scenario was selected, run flow pattern pre/post here also
                    if (selectedTestScenario != null)  {
                        FlowPatternPreOrPostExecutionBlock patternPostExecutionBlock = MCFG.getFlowPattern(interfaceFlowPattern.getRefId()).getPostExecutionBlock();
                        flowPatternPreOrPostBlockExecute(interfaceObj, interfaceFlowPattern, patternPostExecutionBlock);    
                    }
                }
            } else {
                logger.warn("No execution blocks in the scenario "+ tId);
            }
        }
    }

    private void execute(Interface interfaceObj, IfaceFlowPattern interfaceFlowPattern) throws FrameworkException {
        if (interfaceFlowPattern == null) {
            execute(interfaceObj);
        } else {
            if (interfaceFlowPattern.getInstanceMetadata() != null) {
                fireBlockChanged(interfaceFlowPattern.getInstanceMetadata().getTestName());
            }
            //flow pattern pre-execution block
            FlowPatternPreOrPostExecutionBlock preExecutionBlock = MCFG.getFlowPattern(interfaceFlowPattern.getRefId()).getPreExecutionBlock();
            flowPatternPreOrPostBlockExecute(interfaceObj, interfaceFlowPattern, preExecutionBlock);
            //flow pattern scenarios
            int standardScenariosRan = 0;
            try {
                for (IfaceTestScenario its : interfaceFlowPattern.getIfaceTestScenario()) {
                    if (stopped) break;                    
                    try {
                        final String fpId = interfaceFlowPattern.getRefId();
                        final TestScenario testScenario = MCFG.getTestScenario(fpId, its.getRefId());
                        if (!testScenario.isNonStandard()) {
                            ++standardScenariosRan;
                        }
                        execute(interfaceObj, interfaceFlowPattern, its);
                    } catch(FrameworkException e) {
                        if (stopped) {
                            throw e;
                        } else {
                            logger.error("Test Scenario '"+its.getRefId()+"' for the Test Case '"+interfaceFlowPattern.getInstanceMetadata().getTestName()+"' threw an exception: ", e);
                        }
                    }
                }
            } finally {
                if (standardScenariosRan > 0) {
                    //flow pattern post-execution block
                    FlowPatternPreOrPostExecutionBlock postExecutionBlock = MCFG.getFlowPattern(interfaceFlowPattern.getRefId()).getPostExecutionBlock();
                    flowPatternPreOrPostBlockExecute(interfaceObj, interfaceFlowPattern, postExecutionBlock);
                } else {
                    logger.warn("No standard scenarios to execute in the pattern "+ interfaceFlowPattern.getRefId());
                }
            }
        }
    }

    private void execute(Interface interfaceObj) throws FrameworkException {
        if (interfaceObj == null) {
            throw new FrameworkExecutionException("At least an interface must be selected when trying to execute test framework");
        } else {
            InterfaceConfiguration interfaceConfig = MCFG.getInterfaceConfig(interfaceObj);
            for (IfaceFlowPattern ifp : interfaceConfig.getIfaceFlowPatterns()) {
                if (stopped) break;                
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

    private void fireOperationStarted(String operationName, boolean prePostOperation) {
        for (FlowExecutionListener l : flowExecutionListeners) {
            l.operationStarted(new FlowExecutionEvent(operationName, prePostOperation));
        }
    }

    private void fireOperationFinished(String operationName, boolean prePostOperation, OperationResult componentResult) {
        for (FlowExecutionListener l : flowExecutionListeners) {
            l.operationFinished(new FlowExecutionEvent(operationName, prePostOperation, componentResult));
        }
    }
    
    private void fireBlockChanged(String infoMsg) {
        for (FlowExecutionListener l : flowExecutionListeners) {
            l.blockChanged(infoMsg);
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
        this.selectedInterface = iface;
    }

    /**
     *
     * @param ifaceFlowPattern
     */
    public void setIfaceFlowPattern(IfaceFlowPattern ifaceFlowPattern) {
        this.selectedFlowPattern = ifaceFlowPattern;
    }

    /**
     *
     * @param ifaceTestScenario
     */
    public void setIfaceTestScenario(IfaceTestScenario ifaceTestScenario) {
        this.selectedTestScenario = ifaceTestScenario;
    }

    /**
     *
     * @param ifaceExecutionBlock
     */
    public void setIfaceExecutionBlock(IfaceExecBlock ifaceExecutionBlock) {
        this.selectedExecutionBlock = ifaceExecutionBlock;
    }

    /**
     *
     * @param operation
     */
    public void setOperation(Operation operation) {
        this.selectedOperation = operation;
    }

    private void executeOperation(Interface interfaceObj, IfaceFlowPattern interfaceFlowPattern, IfaceTestScenario interfaceTestScenario, IfaceExecBlock interfaceExecutionBlock, Operation operation) throws FrameworkException {
        final String execBlockId = interfaceExecutionBlock != null ? interfaceExecutionBlock.getRefId() : "na";
        OperationResult.nextInstance();
        final OperationResult cor = OperationResult.getInstance();        
        cor.setScenarioName(interfaceTestScenario.getRefId());
        cor.setExecBlockName(execBlockId);
        cor.setOperation(operation);
        cor.setCommmonResult(CommonResult.FAILURE);
        
        final String operationName = operation.getName().value();
        final boolean prePostOperation = !(operation instanceof ExecBlockOperation);        
        fireOperationStarted(operationName, prePostOperation);
        try {
            String component = getComponentTypeName(operationName);
            logger.trace("Operation " + operationName + " has been identified as a type of " + component + " component operation type.");

            

            // variables
            DBConfig dbConfig = null;
            OracleFusionMiddlewareInstance ofmInstance = null;
            FTPConfig ftpConfig = null;
            JMSConfig jmsConfig = null;
            SOAPConfig soapConfig = null;
            EMAILConfig emailConfig = null;
            DatabaseComponent dbComp = null;
            File workingDir = ICFG.getComponentWorkingDir(ifaceId, interfaceFlowPattern, interfaceTestScenario.getRefId(), component.toLowerCase());
            File rootWorkingDir = ICFG.getComponentWorkingDir(ifaceId, interfaceFlowPattern, interfaceTestScenario.getRefId(), null);
            File patternWorkingDir = ICFG.getFlowPatternInstanceDir(ifaceId, interfaceFlowPattern);
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
                                + this.ifaceId + ", execution block " + execBlockId
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
                    JmsComponent jmsComp = new JmsComponent(ofmInstance, jmsConfig, workingDir);
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
                case "EMAIL":
                    emailConfig = ICFG.getEmailConfig(interfaceExecutionBlock, operation.getExecuteOn());
                    EmailServers.EmailServer.EmailServerInstance emailServerInstance = MCFG.getEmailServerInstance(envName, emailConfig.getRefId());
                    EMAILConfig.Email email = ICFG.getIfaceEmail(this.envName, interfaceExecutionBlock, operation.getExecuteOn());
                    if (email == null) {
                        String msg = "There exists no Email configurations within config.xml file for interface "
                                + this.ifaceId + ", execution block " + interfaceExecutionBlock.getRefId()
                                + " targeting " + operation.getExecuteOn().value() + ".";
                        logger.error(msg);
                        throw new FrameworkConfigurationException(msg);
                    }
                    
                    EmailComponent emailComp = new EmailComponent(interfaceExecutionBlock, emailServerInstance, email, null, workingDir);
                    emailComp.execute(operation);
                    break;
                case "UTIL":
                    UTILConfig utilConfig = ICFG.getUtilConfig();
                    OSBReporting.OsbReportingInstance osbReportingInstance = MCFG.getOSBReportingInstance(envName);
                    OSBReporting.DbObjects osbDbObjects = MCFG.getOsbReportingInstance().getDbObjects();
                    UtilityComponent utilComp = new UtilityComponent(interfaceObj.getName(), envName, ICFG, interfaceTestScenario, osbDbObjects, osbReportingInstance, utilConfig, rootWorkingDir,operation);
                    utilComp.execute(operation);
                    break;
                case "REPORT":
                    // reports should not be generated while running only one execution block or operation
                    if (this.selectedExecutionBlock == null) {
                        ReportComponent reportComp = new ReportComponent(interfaceObj.getName(), interfaceFlowPattern.getRefId(), interfaceFlowPattern.getInstanceMetadata().getTestName(), (this.selectedTestScenario != null)?this.selectedTestScenario.getRefId():null, patternWorkingDir);
                        reportComp.execute(operation);
                    }
                    break;
                default:
                    throw new FrameworkConfigurationException("Unsupported type of component to be created: " + component);
            }
        } catch(Throwable e) {
            if(operation instanceof ExecBlockOperation) {
                if(((ExecBlockOperation) operation).isContinueOnFailure()) {
                    if (cor.getCommmonResult() == CommonResult.FAILURE) {
                        cor.setCommmonResult(CommonResult.WARNING);
                    }
                    logger.warn("Operation " + operation.getName() + " failed but continueOnFailure is set to true, continuing.", e);
                } else {
                    throw e;
                }
            } else {
                //Scenario pre/post operations
                if (!cor.getCommmonResult().equals(CommonResult.SUCCESS)) {
                    cor.setCommmonResult(CommonResult.WARNING);
                }
                logger.warn("Operation " + operation.getName() + " failed but in pre/post execution block.", e);
            }
        } finally {
            fireOperationFinished(operationName, prePostOperation, OperationResult.getInstance());
        }
    }
    
    private void executeFlowPatternOperation(Interface interfaceObj, IfaceFlowPattern interfaceFlowPattern, Operation operation) throws FrameworkException {
        if (this.selectedExecutionBlock != null) {
            return;
        }        
        OperationResult.nextInstance();
        
        final String operationName = operation.getName().value();
        fireOperationStarted(operationName, true);
        try {
            String component = getComponentTypeName(operationName);
            logger.trace("Operation " + operationName + " has been identified as a type of " + component + " component operation type.");
            File patternWorkingDir = ICFG.getFlowPatternInstanceDir(ifaceId, interfaceFlowPattern);
            switch (component) {
                  case "UTIL":
                    OSBReporting.OsbReportingInstance osbReportingInstance = MCFG.getOSBReportingInstance(envName);
                    OSBReporting.DbObjects osbDbObjects = MCFG.getOsbReportingInstance().getDbObjects();
                    UtilityComponent utilComp = new UtilityComponent(interfaceObj.getName(), envName, osbDbObjects, osbReportingInstance, patternWorkingDir, operation);
                    utilComp.execute(operation);
                    break;
                  case "REPORT":
                    // reports should not be generated while running only one execution block or operation
                    ReportComponent reportComp = new ReportComponent(interfaceObj.getName(), interfaceFlowPattern.getRefId(), interfaceFlowPattern.getInstanceMetadata().getTestName(), (this.selectedTestScenario != null)?this.selectedTestScenario.getRefId():null, patternWorkingDir);
                    reportComp.execute(operation);
                    break;
                default:
                    throw new FrameworkConfigurationException("Unsupported type of component to be created in flow-pattern level: " + component);
            }          
        } finally {
            fireOperationFinished(operationName,true, OperationResult.getInstance());
        }
    }

    public static Date getActualRunDate() {
        return actualRunDate;
    }
    
    public static java.sql.Timestamp getActualRunDBDate() {
        return actualRunDBDate;
    }    
    
    public static String getActualRunDBDateString() {
        if (actualRunDBDate == null) {
            return null;
        }
        return String.format("TO_DATE('%s', 'YYYY/MM/DD HH24:MI:SS')", DatabaseComponent.DATE_FORMAT.format(actualRunDBDate));
    }
    
    public static void setActualRunDBDate(java.sql.Timestamp dbTimestamp) {
        actualRunDBDate = dbTimestamp;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }
    
    
}

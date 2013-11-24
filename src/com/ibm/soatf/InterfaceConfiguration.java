/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.soatf;

import com.ibm.soatf.config.iface.IfaceExecBlock;
import com.ibm.soatf.config.iface.IfaceExecBlock.Source;
import com.ibm.soatf.config.iface.IfaceExecBlock.Target;
import com.ibm.soatf.config.iface.IfaceFlowPattern;
import com.ibm.soatf.config.iface.IfaceTestScenario;
import com.ibm.soatf.config.iface.SOATFIfaceConfig;
import com.ibm.soatf.config.iface.SOATFIfaceConfig.IfaceEndPoints.IfaceEndPoint;

import com.ibm.soatf.config.iface.util.UTILConfig;
import com.ibm.soatf.config.master.ExecuteOn;
import com.ibm.soatf.config.master.ExecutionBlock;
import com.ibm.soatf.config.master.Operation;
import java.io.File;
import java.util.ArrayList;
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
public class InterfaceConfiguration {

    private final SOATFIfaceConfig icfg;
    private final File ifaceConfigFile;

    private static final MasterConfiguration MCFG = MasterConfiguration.getInstance();

    public InterfaceConfiguration(File ifaceConfigFile) {
        this.ifaceConfigFile = ifaceConfigFile;
        icfg = getSOATFIfaceConfig();
    }

    public final SOATFIfaceConfig getSOATFIfaceConfig() {
        JAXBContext jaxbContext;
        Unmarshaller jaxbUnmarshaller;
        try {
            jaxbContext = JAXBContext.newInstance("com.ibm.soatf.config.iface");
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return ((JAXBElement<SOATFIfaceConfig>) jaxbUnmarshaller.unmarshal(ifaceConfigFile)).getValue();
        } catch (JAXBException jbex) {
            throw new IllegalStateException("Error while unmarshalling interface configuration object from XML file " + ifaceConfigFile, jbex);
        } finally {
            jaxbContext = null;
            jaxbUnmarshaller = null;
        }
    }

    public List<IfaceFlowPattern> getIfaceFlowPatterns() {
        if (icfg.getIfaceFlowPatternConfig().getIfaceFlowPattern().isEmpty()) {
            throw new FrameworkConfigurationException("No flow patterns configuration found in interface config file '" + ifaceConfigFile + "'.");
        }
        return icfg.getIfaceFlowPatternConfig().getIfaceFlowPattern();
    }

    public IfaceFlowPattern getIfaceFlowPattern(String flowPatternId) {
        for (IfaceFlowPattern refFlowPattern : getIfaceFlowPatterns()) {
            if (refFlowPattern.getRefId().equals(flowPatternId)) {
                return refFlowPattern;
            }
        }
        throw new IllegalStateException("No flow pattern found using reference id '" + flowPatternId + "'.");
    }

    public List<IfaceTestScenario> getIfaceTestScenarios(String ifaceFlowPatternId) {
        final List<IfaceTestScenario> ifaceTestScenarios = getIfaceFlowPattern(ifaceFlowPatternId).getIfaceTestScenario();
        if (ifaceTestScenarios.isEmpty()) {
            throw new IllegalStateException("No test scenarios found for flow pattern: " + ifaceFlowPatternId + "'.");
        }
        return ifaceTestScenarios;
    }

    public IfaceTestScenario getIfaceTestScenario(String ifaceFlowPatternId, String ifaceTestScenarioId) {
        for (IfaceTestScenario ifaceTestScenario : getIfaceTestScenarios(ifaceFlowPatternId)) {
            if (ifaceTestScenario.getRefId().equals(ifaceTestScenarioId)) {
                return ifaceTestScenario;
            }
        }
        throw new IllegalStateException("No such test scenario found: " + ifaceTestScenarioId + " in flow pattern: " + ifaceFlowPatternId);
    }

    public List<IfaceExecBlock> getIfaceExecBlocks(String ifaceFlowPatternId, String ifaceTestScenarioId) {
        final List<IfaceExecBlock> ifaceExecBlocks = getIfaceTestScenario(ifaceFlowPatternId, ifaceTestScenarioId).getIfaceExecBlock();
        if (ifaceExecBlocks.isEmpty()) {
            throw new IllegalStateException("No execution blocks found in test scenario: " + ifaceTestScenarioId
                    + " in flow pattern: " + ifaceFlowPatternId);
        }
        return ifaceExecBlocks;
    }

    public IfaceExecBlock getIfaceExecBlock(String ifaceFlowPatternId, String ifaceTestScenarioId, String ifaceExecBlockId) {
        for (IfaceExecBlock refExecBlock : getIfaceExecBlocks(ifaceFlowPatternId, ifaceTestScenarioId)) {
            if (refExecBlock.getRefId().equals(ifaceExecBlockId)) {
                return refExecBlock;
            }
        }
        throw new IllegalStateException("No such execution block found: " + ifaceExecBlockId + " in test scenario: " + ifaceTestScenarioId
                + " in flow pattern: " + ifaceFlowPatternId);
    }

    public List<Source> getSource(String ifaceFlowPatternId, String ifaceTestScenarioId, String ifaceExecBlockId) {
        return getIfaceExecBlock(ifaceFlowPatternId, ifaceTestScenarioId, ifaceExecBlockId).getSource();
    }

    public List<Target> getTarget(String ifaceFlowPatternId, String ifaceTestScenarioId, String ifaceExecBlockId) {
        return getIfaceExecBlock(ifaceFlowPatternId, ifaceTestScenarioId, ifaceExecBlockId).getTarget();
    }

    public List<Operation> getOperations(String ifaceFlowPatternId, String ifaceTestScenarioId, String ifaceExecBlockId) {
        ExecutionBlock executionBlock = MCFG.getExecutionBlock(ifaceFlowPatternId, ifaceTestScenarioId, ifaceExecBlockId);
        return executionBlock.getOperation();
    }

    public Operation getOperation(String ifaceFlowPatternId, String ifaceTestScenarioId, String ifaceExecBlockId, String operationName) {
        for (Operation operation : getOperations(ifaceFlowPatternId, ifaceTestScenarioId, ifaceExecBlockId)) {
            if (operation.getName().value().equals(operationName)) {
                return operation;
            }
        }
        throw new IllegalStateException("No such operation found within interface configuration file: " + operationName + " in test scenario: " + ifaceTestScenarioId
                + " in flow pattern: " + ifaceFlowPatternId + " in execution block: " + ifaceExecBlockId);
    }

    public List<IfaceEndPoint> getIfaceEndPoints() {
        return icfg.getIfaceEndPoints().getIfaceEndPoint();

    }

    public IfaceEndPoint getIfaceEndPoint(String endPointLocalIdRef) {
        for (IfaceEndPoint ifaceEndPoint : getIfaceEndPoints()) {
            if (ifaceEndPoint.getEndPointLocalId().equals(endPointLocalIdRef)) {
                return ifaceEndPoint;
            }
        }
        throw new IllegalStateException("There doesn't exist such endpoint within interface configuration file for end-point identificator '"
                + endPointLocalIdRef + "'.");
    }

    public List<IfaceEndPoint> getIfaceEndPoint(IfaceExecBlock ifaceExecBlock, ExecuteOn execOn) {
        if ( ifaceExecBlock == null || execOn == null ) {
            throw new IllegalArgumentException("Some of the input parameters are null. IfaceExecBlock='" + ifaceExecBlock.toString()
                    + "', ExecuteOn='" + execOn.value() + "'.");
        }
       
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
        
        if (ifaceEndPoints.size() == 0){
            throw new FrameworkConfigurationException("The lookup for relevant endpoints for execution block='"
            + ifaceExecBlock.getRefId() + "' resulted in empty list. It look like there is configuration inconsistency in " 
                    + this.ifaceConfigFile + " file.");
        }
        return ifaceEndPoints;

    }

    public UTILConfig getUtilConfig() {
        return icfg.getUtilConfig();
    }

}

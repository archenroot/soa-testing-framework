/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.soatf;

import static com.ibm.soatf.FrameworkConfiguration.SOATF_CURRENT_INTERFACE_FILE;
import com.ibm.soatf.config._interface.ReferencedExecutionBlock;
import com.ibm.soatf.config._interface.ReferencedExecutionBlock.Source;
import com.ibm.soatf.config._interface.ReferencedExecutionBlock.Target;
import com.ibm.soatf.config._interface.ReferencedFlowPattern;
import com.ibm.soatf.config._interface.ReferencedTestScenario;
import com.ibm.soatf.config._interface.SOATestingFrameworkInterfaceConfiguration;
import com.ibm.soatf.config._interface.SOATestingFrameworkInterfaceConfiguration.Interface.InterfaceSource;
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
public class InterfaceConfigurationUnmarshaller {

    public static SOATestingFrameworkInterfaceConfiguration getSOATestingFrameworkInterfaceConfiguration() throws FrameworkConfigurationException {
        JAXBContext jaxbContext;
        Unmarshaller jaxbUnmarshaller;
        try {
            jaxbContext = JAXBContext.newInstance("com.ibm.soatf.config._interface");
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return ((JAXBElement<SOATestingFrameworkInterfaceConfiguration>) jaxbUnmarshaller.unmarshal(new File(SOATF_CURRENT_INTERFACE_FILE))).getValue();
        } catch (JAXBException jbex) {
            throw new FrameworkConfigurationException("Error while unmarshalling interface configuration object from XML file " + SOATF_CURRENT_INTERFACE_FILE, jbex);
        } finally {
            jaxbContext = null;
            jaxbUnmarshaller = null;
        }
    }

    public static InterfaceSource getInterfaceSource() throws FrameworkConfigurationException {
        if (getSOATestingFrameworkInterfaceConfiguration().getInterface().getInterfaceSource() == null) {
            throw new FrameworkConfigurationException();
        }
        return getSOATestingFrameworkInterfaceConfiguration().getInterface().getInterfaceSource();
    }

    public static List<ReferencedFlowPattern> getReferencedFlowPatterns() throws FrameworkConfigurationException {
        if (getSOATestingFrameworkInterfaceConfiguration().getReferencedFlowPattern().isEmpty()) {
            throw new FrameworkConfigurationException();
        }
        return getSOATestingFrameworkInterfaceConfiguration().getReferencedFlowPattern();
    }

    public static ReferencedFlowPattern getReferencedFlowPattern(String referencedFlowPatternIdentificator) throws FrameworkConfigurationException {
        ListIterator<ReferencedFlowPattern> referencedFlowPatterns = getReferencedFlowPatterns().listIterator();
        ReferencedFlowPattern referencedFlowPattern = null;
        boolean found = false;
        while (referencedFlowPatterns.hasNext()) {
            referencedFlowPattern = referencedFlowPatterns.next();
            if (referencedFlowPattern.getIdentificator().equals(referencedFlowPatternIdentificator)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new FrameworkConfigurationException("Interface flow pattern identified by " + referencedFlowPatternIdentificator
                    + " cannot be found.");
        }
        return referencedFlowPattern;
    }

    public static List<ReferencedTestScenario> getReferencedTestScenarios(String referencedFlowPatternIdentificator) throws FrameworkConfigurationException {
        if (getReferencedFlowPattern(referencedFlowPatternIdentificator).getReferencedTestScenario().isEmpty()) {
            throw new FrameworkConfigurationException("There are no configured test scenarios for referenced flow pattern identified by " + referencedFlowPatternIdentificator);
        }
        return getReferencedFlowPattern(referencedFlowPatternIdentificator).getReferencedTestScenario();
    }

    public static ReferencedTestScenario getReferencedTestScenario(String referencedFlowPatternIdentificator, String referenceTestScenarioIdentificator) throws FrameworkConfigurationException {
        ListIterator<ReferencedTestScenario> referencedTestScenarios = getReferencedTestScenarios(referencedFlowPatternIdentificator).listIterator();
        ReferencedTestScenario referencedTestScenario = null;
        boolean found = false;
        while (referencedTestScenarios.hasNext()) {
            referencedTestScenario = referencedTestScenarios.next();
            if (referencedTestScenario.getIdentificator().equals(referenceTestScenarioIdentificator)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new FrameworkConfigurationException("");
        }
        return referencedTestScenario;
    }

    public static List<ReferencedExecutionBlock> getReferencedExecutionBlocks(String referencedFlowPatternIdentificator, String referenceTestScenarioIdentificator) throws FrameworkConfigurationException {
        if (getReferencedTestScenario(referencedFlowPatternIdentificator, referenceTestScenarioIdentificator).getReferencedExecutionBlock().isEmpty()) {
            throw new FrameworkConfigurationException("There are no referenced execution block within test scenario identified by " + referenceTestScenarioIdentificator
                    + " within referenced flow pattern identified by " + referencedFlowPatternIdentificator + ".");
        }
        return getReferencedTestScenario(referencedFlowPatternIdentificator, referenceTestScenarioIdentificator).getReferencedExecutionBlock();
    }

    public static ReferencedExecutionBlock getReferencedExecutionBlock(
            String referencedFlowPatternIdentificator,
            String referenceTestScenarioIdentificator,
            String referenceExecutionBlockIdentificator) throws FrameworkConfigurationException {
        ListIterator<ReferencedExecutionBlock> referencedExecutionBlocks
                = getReferencedExecutionBlocks(
                        referencedFlowPatternIdentificator,
                        referenceTestScenarioIdentificator).listIterator();
        ReferencedExecutionBlock referencedExecutionBlock = null;
        boolean found = false;
        while (referencedExecutionBlocks.hasNext()) {
            referencedExecutionBlock = referencedExecutionBlocks.next();
            if (referencedExecutionBlock.getIdentificator().equals(referenceExecutionBlockIdentificator)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new FrameworkConfigurationException("There is no reference block identified by " + referenceExecutionBlockIdentificator + " within test scenario identified by "
                    + referenceTestScenarioIdentificator + " within referenced flow pattern identified by " + referencedFlowPatternIdentificator + ".");
        }
        return referencedExecutionBlock;
    }
    
    public Source getSource(String referencedFlowPatternIdentificator,
            String referenceTestScenarioIdentificator,
            String referenceExecutionBlockIdentificator) throws FrameworkConfigurationException{
        return getReferencedExecutionBlock(referencedFlowPatternIdentificator, referenceTestScenarioIdentificator, referenceExecutionBlockIdentificator).getSource();
    }
    
    public Target getTarget(String referencedFlowPatternIdentificator,
            String referenceTestScenarioIdentificator,
            String referenceExecutionBlockIdentificator) throws FrameworkConfigurationException{
        return getReferencedExecutionBlock(referencedFlowPatternIdentificator, referenceTestScenarioIdentificator, referenceExecutionBlockIdentificator).getTarget();
    }
    
    
}

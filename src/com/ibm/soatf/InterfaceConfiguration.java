/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.soatf;


import static com.ibm.soatf.FrameworkConfiguration.SOA_TEST_HOME;
import com.ibm.soatf.config._interface.AbstractScenarioTargeting;
import com.ibm.soatf.config._interface.InterfaceExecutionBlock;
import com.ibm.soatf.config._interface.InterfaceExecutionBlock.Source;
import com.ibm.soatf.config._interface.InterfaceExecutionBlock.Target;
import com.ibm.soatf.config._interface.InterfaceFlowPattern;
import com.ibm.soatf.config._interface.InterfaceTestScenario;
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
public class InterfaceConfiguration {

    private SOATestingFrameworkInterfaceConfiguration soaTestingFrameworkInterfaceConfiguration;
    private File interfaceConfigurationFile;
    
    
    public InterfaceConfiguration(File interfaceConfigurationFile) throws FrameworkConfigurationException {
        this.interfaceConfigurationFile = interfaceConfigurationFile;
        soaTestingFrameworkInterfaceConfiguration = getSOATestingFrameworkInterfaceConfiguration();
    }
    
    
    public SOATestingFrameworkInterfaceConfiguration getSOATestingFrameworkInterfaceConfiguration() throws FrameworkConfigurationException {
        JAXBContext jaxbContext;
        Unmarshaller jaxbUnmarshaller;
        try {
            jaxbContext = JAXBContext.newInstance("com.ibm.soatf.config._interface");
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return ((JAXBElement<SOATestingFrameworkInterfaceConfiguration>) jaxbUnmarshaller.unmarshal(interfaceConfigurationFile)).getValue();
        } catch (JAXBException jbex) {
            throw new FrameworkConfigurationException("Error while unmarshalling interface configuration object from XML file " + interfaceConfigurationFile, jbex);
        } finally {
            jaxbContext = null;
            jaxbUnmarshaller = null;
        }
    }

    public InterfaceSource getInterfaceSource() throws FrameworkConfigurationException {
        if (soaTestingFrameworkInterfaceConfiguration.getInterface().getInterfaceSource() == null) {
            throw new FrameworkConfigurationException();
        }
        return getSOATestingFrameworkInterfaceConfiguration().getInterface().getInterfaceSource();
    }

    public List<InterfaceFlowPattern> getInterfaceFlowPatterns() throws FrameworkConfigurationException {
        if (soaTestingFrameworkInterfaceConfiguration.getInterfaceFlowPattern().isEmpty()) {
            throw new FrameworkConfigurationException();
        }
        return getSOATestingFrameworkInterfaceConfiguration().getInterfaceFlowPattern();
    }

    public InterfaceFlowPattern getInterfaceFlowPattern(FlowPatternCompositeKey fpck) throws FrameworkConfigurationException {
        ListIterator<InterfaceFlowPattern> referencedFlowPatterns = getInterfaceFlowPatterns().listIterator();
        InterfaceFlowPattern referencedFlowPattern = null;
        boolean found = false;
        while (referencedFlowPatterns.hasNext()) {
            referencedFlowPattern = referencedFlowPatterns.next();
            if (referencedFlowPattern.getIdentificator().equals(fpck.getFlowPatternIdentificator())) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new FrameworkConfigurationException("Interface flow pattern identified by " + fpck.getFlowPatternIdentificator()
                    + " cannot be found.");
        }
        return referencedFlowPattern;
    }

    public List<InterfaceTestScenario> getInterfaceTestScenarios(FlowPatternCompositeKey fpck) throws FrameworkConfigurationException {
        if (getInterfaceFlowPattern(fpck).getInterfaceTestScenario().isEmpty()) {
            throw new FrameworkConfigurationException("There are no configured test scenarios for referenced flow pattern identified by " + fpck.getFlowPatternIdentificator());
        }
        return getInterfaceFlowPattern(fpck).getInterfaceTestScenario();
    }

    public InterfaceTestScenario getInterfaceTestScenario(FlowPatternCompositeKey fpck) throws FrameworkConfigurationException {
        ListIterator<InterfaceTestScenario> referencedTestScenarios = getInterfaceTestScenarios(fpck).listIterator();
        InterfaceTestScenario referencedTestScenario = null;
        boolean found = false;
        while (referencedTestScenarios.hasNext()) {
            referencedTestScenario = referencedTestScenarios.next();
            if (referencedTestScenario.getIdentificator().equals(fpck.getTestScenarioIdentificator())) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new FrameworkConfigurationException("");
        }
        return referencedTestScenario;
    }

    public List<InterfaceExecutionBlock> getInterfaceExecutionBlocks(FlowPatternCompositeKey fpck) throws FrameworkConfigurationException {
        if (getInterfaceTestScenario(fpck).getInterfaceExecutionBlock().isEmpty()) {
            throw new FrameworkConfigurationException("There are no referenced execution block within test scenario identified by " + fpck.getTestScenarioIdentificator()
                    + " within referenced flow pattern identified by " + fpck.getFlowPatternIdentificator() + ".");
        }
        return getInterfaceTestScenario(fpck).getInterfaceExecutionBlock();
    }

    public InterfaceExecutionBlock getInterfaceExecutionBlock(
            FlowPatternCompositeKey fpck) throws FrameworkConfigurationException {
        ListIterator<InterfaceExecutionBlock> referencedExecutionBlocks
                = getInterfaceExecutionBlocks(
                        fpck).listIterator();
        InterfaceExecutionBlock referencedExecutionBlock = null;
        boolean found = false;
        while (referencedExecutionBlocks.hasNext()) {
            referencedExecutionBlock = referencedExecutionBlocks.next();
            if (referencedExecutionBlock.getIdentificator().equals(fpck.getExecutionBlockIdentificator())) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new FrameworkConfigurationException("There is no reference block identified by " + fpck.getExecutionBlockIdentificator() + " within test scenario identified by "
                    + fpck.getTestScenarioIdentificator() + " within referenced flow pattern identified by " + fpck.getFlowPatternIdentificator() + ".");
        }
        return referencedExecutionBlock;
    }
    
    public Source getSource(FlowPatternCompositeKey fpck) throws FrameworkConfigurationException{
        return getInterfaceExecutionBlock(fpck).getSource();
    }
    
    public Target getTarget(FlowPatternCompositeKey fpck) throws FrameworkConfigurationException{
        return getInterfaceExecutionBlock(fpck).getTarget();
    }
}

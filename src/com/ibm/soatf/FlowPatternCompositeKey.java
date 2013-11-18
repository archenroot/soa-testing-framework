package com.ibm.soatf;


import com.ibm.soatf.config._interface.InterfaceExecutionBlock;
import com.ibm.soatf.config._interface.util.UtilConfiguration;
import com.ibm.soatf.tool.UniqueIdGenerator;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.ListIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FlowPatternCompositeKey {

        private final Logger logger = LogManager.getLogger(FlowPatternCompositeKey.class.getName());

        private String uniqueInternalKey;
        private String descriptor;
        private String interfaceName;
        private String testName;
        private String flowPatternIdentificator;
        private String testScenarioIdentificator;
        private String executionBlockIdentificator;
        private boolean hasSource;
        private boolean hasTarget;
        private boolean hasCommon;
        private InterfaceExecutionBlock.Source source;
        private InterfaceExecutionBlock.Target target;
        private UtilConfiguration util;

        FlowPatternCompositeKey() {
            this.uniqueInternalKey = UniqueIdGenerator.generateUniqueId();
        }

        FlowPatternCompositeKey(FlowPatternCompositeKey fpck) {
            this.uniqueInternalKey = UniqueIdGenerator.generateUniqueId();
            this.descriptor = fpck.descriptor;
            this.interfaceName = fpck.interfaceName;
            this.testName = fpck.testName;
            this.flowPatternIdentificator = fpck.getFlowPatternIdentificator();
            this.testScenarioIdentificator = fpck.getTestScenarioIdentificator();
            this.executionBlockIdentificator = fpck.getExecutionBlockIdentificator();
            this.hasSource = fpck.hasSource();
            this.hasTarget = fpck.hasTarget();
            this.hasCommon = fpck.hasCommon();
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

        public boolean hasSource() {
            return hasSource;
        }

        public boolean hasTarget() {
            return hasTarget;
        }

        public boolean hasCommon() {
            return hasCommon;
        }

        public String getUniqueInternalKey() {
            return uniqueInternalKey;
        }

        public InterfaceExecutionBlock.Source getSource() {
            return source;
        }

        public InterfaceExecutionBlock.Target getTarget() {
            return target;
        }

        public String getDescriptor() {
            return descriptor;
        }

        public String getInterfaceName() {
            return interfaceName;
        }

    public String getTestName() {
        return testName;
    }

        public void setFlowPatternIdentificator(String flowPatternIdentificator) {
            this.flowPatternIdentificator = flowPatternIdentificator;
            //logger.debug("FlowPattern key part identified: " + flowPatternIdentificator);
        }

        public void setTestScenarioIdentificator(String testScenarioIdentificator) {
            this.testScenarioIdentificator = testScenarioIdentificator;
            //logger.debug("TestScenario key part identified :" + testScenarioIdentificator);
        }

        public void setExecutionBlockIdentificator(String executionBlockIdentificator) {
            this.executionBlockIdentificator = executionBlockIdentificator;
            //logger.debug("ExecutionBlock key part identified :" + executionBlockIdentificator);
        }

        public void setHasSource(boolean hasSource) {
            this.hasSource = hasSource;
        }

        public void setHasTarget(boolean hasTarget) {
            this.hasTarget = hasTarget;
        }

        public void setHasCommon(boolean hasCommon) {
            this.hasCommon = hasCommon;
        }

        public void setSource(InterfaceExecutionBlock.Source source) {
            this.source = source;
            if (this.source != null) {
                this.hasSource = true;
            }

        }

        public void setUtilConfiguration(UtilConfiguration util) {
            this.util = util;
            if (this.util != null) {
                this.hasCommon = true;
            }
        }

        public void setTarget(InterfaceExecutionBlock.Target target) {
            this.target = target;
            if (this.target != null) {
                this.hasTarget = true;
            }
        }

        public void setInterfaceName(String interfaceName) {
            this.interfaceName = interfaceName;
        }

         
        public void setDescriptor(String descriptor) {
            this.descriptor = descriptor;
        }

    public void setTestName(String testName) {
        this.testName = testName;
    }

        public FlowPatternCompositeKey copyToNewInstance(FlowPatternCompositeKey fpck) {
            return new FlowPatternCompositeKey(fpck);
        }

        public String getCompositeKeyDigest() throws FrameworkConfigurationException {
            String compositeKey;
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-512");
                md.update(
                        (this.flowPatternIdentificator
                        + this.testScenarioIdentificator
                        + this.executionBlockIdentificator).getBytes("UTF-8")
                );
                byte[] digesta = md.digest();
                compositeKey = new String(digesta, 0, digesta.length, "UTF-8");
            } catch (UnsupportedEncodingException ueex) {
                throw new FrameworkConfigurationException("Cannot get bytes from string using UTF-8.", ueex);
            } catch (NoSuchAlgorithmException nsaex) {
                throw new FrameworkConfigurationException("Trying to use not supported message digest algorithm SHA-512.", nsaex);
            }
            return compositeKey;
        }

        public FlowPatternCompositeKey getMatchingKey(List<FlowPatternCompositeKey> flowPatternCompositeKeys) throws FrameworkConfigurationException {
            ListIterator<FlowPatternCompositeKey> flowPatternCompositeKeyIterator = flowPatternCompositeKeys.listIterator();
            FlowPatternCompositeKey flowPatternCompositeKey = null;
            boolean found = false;
            while (flowPatternCompositeKeyIterator.hasNext()) {
                flowPatternCompositeKey = flowPatternCompositeKeyIterator.next();
                //logger.trace("key to search for: " + this.getCompositeKeyDigest());
                //logger.trace("current key from search set: " + flowPatternCompositeKey.getCompositeKeyDigest());
                if (flowPatternCompositeKey.getCompositeKeyDigest().equals(this.getCompositeKeyDigest())) {
                    //logger.trace("Composite key " + this.getCompositeKeyDigest() + " found within the provided composite key set.");
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new FrameworkConfigurationException("The provided composite key cannot be found within composite key set, there is probably XML configuration issue. Key content: " + this.toString());
            }
            return flowPatternCompositeKey;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(", Composite key " + this.uniqueInternalKey + " of type " + descriptor + " content:"); //\n");
            //sb.append(" internal unique key: " + this.uniqueInternalKey); // + "\n");
            sb.append(" flowPatternIdentificator: " + this.flowPatternIdentificator); // + "\n");
            sb.append(", testScenarioIdentificator: " + this.testScenarioIdentificator); // + "\n");
            sb.append(", executionBlockIdentificator: " + this.executionBlockIdentificator); // + "\n");
            sb.append(", hasSource: " + this.hasSource); // + "\n");
            sb.append(", hasTarget: " + this.hasTarget); // + "\n");
            sb.append(", hasCommon: " + this.hasCommon); // + "\n");
            return sb.toString();
        }
    }
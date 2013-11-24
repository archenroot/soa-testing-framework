package com.ibm.soatf.flow;

import com.ibm.soatf.FrameworkConfigurationException;
import com.ibm.soatf.config.iface.EndPoint;
import com.ibm.soatf.config.iface.IfaceExecBlock;
import com.ibm.soatf.config.iface.IfaceExecBlock.Source;
import com.ibm.soatf.config.iface.IfaceExecBlock.Target;
import com.ibm.soatf.config.iface.util.UTILConfig;
import com.ibm.soatf.tool.UniqueIdGenerator;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FlowPatternCompositeKey {

    private final Logger logger = LogManager.getLogger(FlowPatternCompositeKey.class.getName());

    private String uniqueInternalKey;
    private String descriptor;
    private String ifaceName;
    private String ifaceDesc;
    private String workingDir;
    private String testName;
    private String flowPatternId;
    private int testCasePatternCount;
    private String testScenarioId;
    private String testScenarioType;
    private String testScenarioExecBlockCount;
    private String execBlockId;
    private int execBlockSeqId;
    private String execBlockDirection;
    private boolean hasSource;
    private boolean hasTarget;
    private boolean hasUtil;
    private List<Source> source =  new ArrayList<>();
    private List<Target> target =  new ArrayList<>();
    private UTILConfig util;
    private List<EndPoint> endPoints =  new ArrayList<>();
    
    FlowPatternCompositeKey() {
        this.uniqueInternalKey = UniqueIdGenerator.generateUniqueId();
        
    }

    FlowPatternCompositeKey(FlowPatternCompositeKey fpck) {
        this.uniqueInternalKey = UniqueIdGenerator.generateUniqueId();
        this.descriptor = fpck.descriptor;
        this.ifaceName = fpck.ifaceName;
        this.ifaceDesc = fpck.ifaceDesc;
        this.workingDir = fpck.workingDir;
        this.testName = fpck.testName;
        this.flowPatternId = fpck.getFlowPatternId();
        this.testScenarioId = fpck.getTestScenarioId();
        this.execBlockId = fpck.getExecBlockId();
        this.hasSource = fpck.hasSource();
        this.hasTarget = fpck.hasTarget();
        this.hasUtil = fpck.hasUtil();
    }

    public FlowPatternCompositeKey(String flowPatternIdentificator) {
        this.flowPatternId = flowPatternIdentificator;
    }

    public FlowPatternCompositeKey(String flowPatternIdentificator, String testScenarioIdentificator) {
        this.flowPatternId = flowPatternIdentificator;
        this.testScenarioId = testScenarioIdentificator;
    }

    public FlowPatternCompositeKey(String flowPatternIdentificator, String testScenarioIdentificator, String executionBlockIdentificator) {
        this.flowPatternId = flowPatternIdentificator;
        this.testScenarioId = testScenarioIdentificator;
        this.execBlockId = executionBlockIdentificator;
    }

    public String getFlowPatternId() {
        return flowPatternId;
    }

    public int getTestCasePatternCount() {
        return testCasePatternCount;
    }

    public String getTestScenarioId() {
        return testScenarioId;
    }

    public String getTestScenarioType() {
        return testScenarioType;
    }

    public String getTestScenarioExeBlockCount() {
        return testScenarioExecBlockCount;
    }

    public String getExecBlockId() {
        return execBlockId;
    }
    
    public int getExecBlockSeqId() {
        return execBlockSeqId;
    }
    
    public String getExecBlockDirection() {
        return execBlockDirection;
    }

    public boolean hasSource() {
        return hasSource;
    }

    public boolean hasTarget() {
        return hasTarget;
    }

    public boolean hasUtil() {
        return hasUtil;
    }

    public String getUniqueInternalKey() {
        return uniqueInternalKey;
    }

    public List<Source> getSource() {
        return source;
    }

    public List<Target> getTarget() {
        return target;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public String getIfaceName() {
        return ifaceName;
    }

    public String getIfaceDesc() {
        return ifaceDesc;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public String getTestName() {
        return testName;
    }

    public void setFlowPatternId(String flowPatternId) {
        this.flowPatternId = flowPatternId;
        //logger.trace("");
    }

    public void setTestCasePatternCount(int testCasePatternCount) {
        this.testCasePatternCount = testCasePatternCount;
    }

    public void setTestScenarioId(String testScenarioId) {
        this.testScenarioId = testScenarioId;
        //logger.trace("TestScenario key part identified :" + testScenarioId);
    }

    public void setTestScenarioType(String testScenarioType) {
        this.testScenarioType = testScenarioType;
    }

    public void setTestScenarioExecBlockCount(String testScenarioExecBlockCount) {
        this.testScenarioExecBlockCount = testScenarioExecBlockCount;
    }

    public void setExecBlockId(String execBlockId) {
        this.execBlockId = execBlockId;
        //logger.trace("ExecutionBlock key part identified :" + execBlockId);
    }

    public void setExecBlockSeqId(int execBlockSeqId) {
        this.execBlockSeqId = execBlockSeqId;
    }
    
    public void setExecBlockDirection(String execBlockDirection) {
        this.execBlockDirection = execBlockDirection;
    }

    public void setHasSource(boolean hasSource) {
        this.hasSource = hasSource;
    }

    public void setHasTarget(boolean hasTarget) {
        this.hasTarget = hasTarget;
    }

    public void setHasUtil(boolean hasUtil) {
        this.hasUtil = hasUtil;
    }

    public void setSource(List<Source> source) {
        this.source = source;
        if (this.source != null) {
            this.hasSource = true;
        }
    }
    
    public void setTarget(List<Target> target) {
        this.target = target;
        if (this.target != null) {
            this.hasTarget = true;
        }
    }
    
    public void setUtilConfiguration(UTILConfig util) {
        this.util = util;
        if (this.util != null) {
            this.hasUtil = true;
        }
    }
    public void setIfaceName(String ifaceName) {
        this.ifaceName = ifaceName;
    }
    void setIfaceDesc(String ifaceDesc) {
        this.ifaceDesc = ifaceDesc;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
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
                    (this.flowPatternId
                    + this.testScenarioId
                    + this.execBlockId).getBytes("UTF-8")
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

    public FlowPatternCompositeKey getMatchingKey(List<FlowPatternCompositeKey> fpcks) throws FrameworkConfigurationException {
        ListIterator<FlowPatternCompositeKey> fpckIt = fpcks.listIterator();
        FlowPatternCompositeKey fpck = null;
        boolean found = false;
        while (fpckIt.hasNext()) {
            fpck = fpckIt.next();
                //logger.trace("key to search for: " + this.getCompositeKeyDigest());
            //logger.trace("current key from search set: " + flowPatternCompositeKey.getCompositeKeyDigest());
            if (fpck.getCompositeKeyDigest().equals(this.getCompositeKeyDigest())) {
                //logger.trace("Composite key " + this.getCompositeKeyDigest() + " found within the provided composite key set.");
                found = true;
                break;
            }
        }
        if (!found) {
            throw new FrameworkConfigurationException("The provided composite key cannot be found within composite key set, there is probably XML configuration issue. Key content: " + this.toString());
        }
        return fpck;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(", Composite key " + this.uniqueInternalKey + " of type " + descriptor + " content:"); //\n");
        //sb.append(" internal unique key: " + this.uniqueInternalKey); // + "\n");
        sb.append(" flowPatternIdentificator: " + this.flowPatternId); // + "\n");
        sb.append(", testScenarioIdentificator: " + this.testScenarioId); // + "\n");
        sb.append(", executionBlockIdentificator: " + this.execBlockId); // + "\n");
        sb.append(", hasSource: " + this.hasSource); // + "\n");
        sb.append(", hasTarget: " + this.hasTarget); // + "\n");
        sb.append(", hasCommon: " + this.hasUtil); // + "\n");
        return sb.toString();
    }
}

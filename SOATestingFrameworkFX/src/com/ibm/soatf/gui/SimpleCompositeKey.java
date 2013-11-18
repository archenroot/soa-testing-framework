/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ibm.soatf.gui;

/**
 *
 * @author zANGETSu
 */
class SimpleCompositeKey {
    private String interfaceName;
    private String flowPatternName;
    private String testScenarioName;
    private String executionBlockName;
    
    SimpleCompositeKey(){
        
    }

    public String getInterfaceName(){
    return interfaceName;
}
    public String getFlowPatternName() {
        return flowPatternName;
    }

    public String getTestScenarioName() {
        return testScenarioName;
    }

    public String getExecutionBlockName() {
        return executionBlockName;
    }

    public void setInterfaceName(String interfaceName){
        this.interfaceName = interfaceName;
    }
    public void setFlowPatternName(String flowPatternName) {
        this.flowPatternName = flowPatternName;
    }

    public void setTestScenarioName(String testScenarioName) {
        this.testScenarioName = testScenarioName;
    }

    public void setExecutionBlockName(String executionBlockName) {
        this.executionBlockName = executionBlockName;
    }
    
}

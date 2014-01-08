/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ibm.soatf.gui;

/**
 *
 * @author user
 */
final class GUIObjects {
    static class GUIObject<T> {
        protected final T object;
        protected String guiName;
        private GUIObject(T object) {
            this.object = object;
        }
        
        public T getWrappedObject() {
            return object;
        }
        
        @Override
        public final String toString() {
            return guiName;
        }
    }
    
    static class Interface extends GUIObject<com.ibm.soatf.config.master.Interface> {

        public Interface(com.ibm.soatf.config.master.Interface object) {
            super(object);
            guiName = object.getName() + " - " + object.getDescription();
        }
        
        
    }

    static class Project extends GUIObject<com.ibm.soatf.config.master.Project> {
        
        public Project(com.ibm.soatf.config.master.Project object) {
            super(object);
            guiName = "OSB Project: " + object.getName();
        }
    }

    static class FlowPattern extends GUIObject<com.ibm.soatf.config.master.FlowPattern> {

        public FlowPattern(com.ibm.soatf.config.master.FlowPattern object) {
            super(object);
            guiName = object.getIdentificator();
        }
    }

    static class TestScenario extends GUIObject<com.ibm.soatf.config.master.TestScenario> {

        public TestScenario(com.ibm.soatf.config.master.TestScenario object) {
            super(object);
            guiName = object.getIdentificator();
        }
    }

    static class ExecutionBlock extends GUIObject<com.ibm.soatf.config.master.ExecutionBlock> {

        public ExecutionBlock(com.ibm.soatf.config.master.ExecutionBlock object) {
            super(object);
            guiName = object.getIdentificator();
        }
    }
    
    static class Operation extends GUIObject<com.ibm.soatf.config.master.Operation> {

        public Operation(com.ibm.soatf.config.master.Operation object) {
            super(object);
            guiName = "Operation: " + object.getName().value();
        }
    }

    static class IfaceFlowPattern extends GUIObject<com.ibm.soatf.config.iface.IfaceFlowPattern> {

        public IfaceFlowPattern(com.ibm.soatf.config.iface.IfaceFlowPattern object) {
            super(object);
            guiName = "FlowPattern: " + object.getRefId() + " (" + object.getTestName() + ")";
        }
    }

    static class InterfaceTestScenario extends GUIObject<com.ibm.soatf.config.iface.IfaceTestScenario> {

        public InterfaceTestScenario(com.ibm.soatf.config.iface.IfaceTestScenario object) {
            super(object);
            guiName = "TestScenario: " + object.getRefId();
        }
    }

    static class InterfaceExecutionBlock extends GUIObject<com.ibm.soatf.config.iface.IfaceExecBlock> {

        public InterfaceExecutionBlock(com.ibm.soatf.config.iface.IfaceExecBlock object) {
            super(object);
            guiName = "ExecutionBlock: " + object.getRefId();
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.soatf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public abstract class SOATFComponent {

    Logger logger = LogManager.getLogger(SOATFComponent.class.getName());

    protected SOATFCompType componentType;
    protected ComponentResult componentOperationResult;
    protected String identificator;
    protected String workingDirectory;

    private boolean initialized = false;

    public SOATFComponent(SOATFCompType componentType, ComponentResult componentOperationResult) {
        componentOperationResult.setSoaTFCompType(componentType);
        this.componentType = componentType;
        this.componentOperationResult = componentOperationResult;
    }

    public SOATFComponent(SOATFCompType componentType, ComponentResult componentOperationResult, String identificator) {
        this(componentType, componentOperationResult);
        this.identificator = identificator;

    }

    protected abstract void constructComponent();

    public void execute(CompOperType componentOperationType) {
        logger.info("Executing operation: " + componentOperationType.toString());
        componentOperationResult.setCompOperType(componentOperationType);
        executeOperation(componentOperationType);
        logger.info("Result of operation execution: " + componentOperationResult.isOverallResultSuccess());
    }

    protected abstract void executeOperation(CompOperType componentOperationType);

    public boolean easyExecute(CompOperType componentOperationType) {
        SOATFComponent soaTFComponent = SOATFCompFactory.buildSOATFComponent(CompOperType.getComponentType(componentOperationType), new ComponentResult());
        soaTFComponent.execute(componentOperationType);
        return componentOperationResult.isOverallResultSuccess();
    }

    public SOATFCompType getComponentType() {
        return this.componentType;
    }

    public void setComponentType(SOATFCompType componentType) {
        this.componentType = componentType;
    }

    public String getIdentificator() {
        return this.identificator;
    }

    public void setIdentificator(String identificator) {
        this.identificator = identificator;
    }

    public ComponentResult getComponentOperationResult() {
        return this.componentOperationResult;
    }

    public void setComponentOperationResult(ComponentResult componentOperationResult) {
        this.componentOperationResult = componentOperationResult;
    }
}

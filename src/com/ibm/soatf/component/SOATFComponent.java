/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.soatf.component;

import com.ibm.soatf.component.SOATFCompType;
import com.ibm.soatf.component.ComponentResult;
import com.ibm.soatf.config.master.Operation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public abstract class SOATFComponent {

    Logger logger = LogManager.getLogger(SOATFComponent.class.getName());

    protected SOATFCompType componentType;
    protected ComponentResult compOperResult;
    protected String identificator;
    protected String workingDirectory;

    private boolean initialized = false;

    public SOATFComponent(SOATFCompType componentType, ComponentResult componentOperationResult) {
        componentOperationResult.setSoaTFCompType(componentType);
        this.componentType = componentType;
        this.compOperResult = componentOperationResult;
    }

    public SOATFComponent(SOATFCompType componentType, ComponentResult componentOperationResult, String identificator) {
        this(componentType, componentOperationResult);
        this.identificator = identificator;

    }

    protected abstract void constructComponent();

    public void execute(Operation operation) {
        logger.info("Executing operation: " + operation.getName());
        compOperResult.setOperation(operation);
        executeOperation(operation);
        logger.info("Result of operation execution: " + compOperResult.isOverallResultSuccess());
    }

    protected abstract void executeOperation(Operation operation);

    public boolean easyExecute(Operation operation) {
        //SOATFComponent soaTFComponent = SOATFCompFactory.buildSOATFComponent(CompOperType.getComponentType(operation), new ComponentResult());
        //soaTFComponent.execute(operation);
        return compOperResult.isOverallResultSuccess();
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
        return this.compOperResult;
    }

    public void setComponentOperationResult(ComponentResult componentOperationResult) {
        this.compOperResult = componentOperationResult;
    }
}

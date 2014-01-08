/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.soatf.component;

import com.ibm.soatf.FrameworkException;
import com.ibm.soatf.config.master.Operation;
import com.ibm.soatf.flow.OperationResult;
import java.io.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public abstract class AbstractSOATFComponent {

    private static final Logger logger = 
            LogManager.getLogger(AbstractSOATFComponent.class.getName());

    protected SOATFCompType componentType;
    protected String identificator;
    protected File workingDir;

    public AbstractSOATFComponent(SOATFCompType componentType) {
        OperationResult.getInstance().setSoaTFCompType(componentType);
        this.componentType = componentType;
    }

    public AbstractSOATFComponent(SOATFCompType componentType, String identificator) {
        this(componentType);
        this.identificator = identificator;

    }

    protected abstract void constructComponent() throws FrameworkException;
    protected abstract void destructComponent();

    public void execute(Operation operation) throws FrameworkException {
        logger.info("Executing operation: " + operation.getName());
        OperationResult.getInstance().setOperation(operation);
        executeOperation(operation);
        logger.info("Result of " + operation.getName() + " execution: " + OperationResult.getInstance().isSuccessful());
    }

    protected abstract void executeOperation(Operation operation) throws FrameworkException;

    public boolean easyExecute(Operation operation) {
        //SOATFComponent soaTFComponent = SOATFCompFactory.buildSOATFComponent(CompOperType.getComponentType(operation), new ComponentResult());
        //soaTFComponent.execute(operation);
        return OperationResult.getInstance().isSuccessful();
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
}

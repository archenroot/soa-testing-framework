
package com.ibm.soatf.component;

import com.ibm.soatf.FrameworkException;
import com.ibm.soatf.config.master.Operation;

import com.ibm.soatf.flow.OperationResult;
import com.ibm.soatf.flow.OperationResult.CommonResult;
import java.io.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Ladislav Jech <archenroot@gmail.com>
 */
public abstract class AbstractSoaTFComponent {

    private static final Logger logger = 
            LogManager.getLogger(AbstractSoaTFComponent.class.getName());

    /**
     *
     */
    protected SOATFCompType componentType;

    /**
     *
     */
    protected String identificator;

    /**
     *
     */
    protected File workingDir;

    /**
     *
     * @param componentType
     */
    public AbstractSoaTFComponent(SOATFCompType componentType) {
        OperationResult.getInstance().setSoaTFCompType(componentType);
        this.componentType = componentType;
    }

    /**
     *
     * @param componentType
     * @param identificator
     */
    public AbstractSoaTFComponent(SOATFCompType componentType, String identificator) {
        this(componentType);
        this.identificator = identificator;

    }

    /**
     *
     * @throws FrameworkException
     */
    protected abstract void constructComponent() throws FrameworkException;

    /**
     *
     */
    protected abstract void destructComponent();

    /**
     *
     * @param operation
     * @throws FrameworkException
     */
    public void execute(Operation operation) throws FrameworkException {
        logger.info("Executing operation: " + operation.getName());
        OperationResult.getInstance().setOperation(operation);
        OperationResult.getInstance().setCommmonResult(CommonResult.FAILURE);
        executeOperation(operation);
        logger.info("Result of " + operation.getName() + " execution: " + OperationResult.getInstance().getCommmonResult());
    }

    /**
     *
     * @param operation
     * @throws FrameworkException
     */
    protected abstract void executeOperation(Operation operation) throws FrameworkException;

    /**
     *
     * @param operation
     * @return
     */
    public boolean easyExecute(Operation operation) {
        //SOATFComponent soaTFComponent = SOATFCompFactory.buildSOATFComponent(CompOperType.getComponentType(operation), new ComponentResult());
        //soaTFComponent.execute(operation);
        return OperationResult.getInstance().isSuccessful();
    }

    /**
     *
     * @return
     */
    public SOATFCompType getComponentType() {
        return this.componentType;
    }

    /**
     *
     * @param componentType
     */
    public void setComponentType(SOATFCompType componentType) {
        this.componentType = componentType;
    }

    /**
     *
     * @return
     */
    public String getIdentificator() {
        return this.identificator;
    }

    /**
     *
     * @param identificator
     */
    public void setIdentificator(String identificator) {
        this.identificator = identificator;
    }
}

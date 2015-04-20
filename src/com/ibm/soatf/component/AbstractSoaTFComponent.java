
package com.ibm.soatf.component;

import com.ibm.soatf.FrameworkException;
import com.ibm.soatf.config.master.Operation;
import com.ibm.soatf.flow.OperationResult;
import com.ibm.soatf.gui.ProgressMonitor;
import java.io.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Superclass to all of the SOATF components, declares 3 abstract methods common to all components
 * @author Ladislav Jech <archenroot@gmail.com>
 */
public abstract class AbstractSoaTFComponent {

    private static final Logger logger = 
            LogManager.getLogger(AbstractSoaTFComponent.class.getName());

    /**
     * Component type variable.
     */
    protected SOATFCompType componentType;


    /**
     * local directory where current test run instance of Component stores generated/received data files
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
     * all time intensive initialization routines should be placed here instead of constructor
     * not implemented correctly yet
     * @throws FrameworkException
     */
    protected abstract void constructComponent() throws FrameworkException;

    /**
     * no real use in the framework at the moment
     */
    protected abstract void destructComponent();

    /**
     * Flow Executor entry point for operation execution - contains common initialization and logging tasks for every operation, 
     * afterward the <clode>executeOperation</code> method is called, which contains actual body of the operation handling
     * @param operation
     * @throws FrameworkException
     */
    public void execute(Operation operation) throws FrameworkException {
        logger.info("Executing operation: " + operation.getName());
        final OperationResult cor = OperationResult.getInstance();        
        try {
            ProgressMonitor.setIndeterminate();
            executeOperation(operation);
            logger.info("Result of " + operation.getName() + " execution: " + cor.getCommmonResult());
        } catch (Throwable e) {
            final String msg = "Error during execution of operation " + operation.getName() + " in component " + componentType.name() + ".";
            cor.addMsg(msg);
            throw new FrameworkException(msg, e);
        } finally {
            ProgressMonitor.markDone();
        }
    }

    /**
     * Implementation should contain how the component handles any given operation
     * @param operation
     * @throws FrameworkException
     */
    protected abstract void executeOperation(Operation operation) throws FrameworkException;

    /**
     * getter for component type set by every subclass in constructor
     * @return actual component type
     */
    public SOATFCompType getComponentType() {
        return this.componentType;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ibm.soatest;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public abstract class SOATFComponent {

    Logger logger = LogManager.getLogger(SOATFComponent.class.getName());

    protected SOATFCompType componentType;
    protected CompOperResult componentOperationResult;
    protected String identificator;
    
    private boolean initialized = false;
    
    public SOATFComponent(SOATFCompType componentType, CompOperResult componentOperationResult) {
        componentOperationResult.setSoaTFCompType(componentType);
        this.componentType = componentType;
        this.componentOperationResult = componentOperationResult;
    }

    public SOATFComponent(SOATFCompType componentType, CompOperResult componentOperationResult, String identificator) {
        this(componentType, componentOperationResult);
        this.identificator = identificator;

    }
    
    protected abstract void constructComponent();

    public void execute(CompOperType componentOperationType) {
        /*if (!initialized) {
            initialized = true;
            CompOperResult cor = constructComponent();
            if (!cor.isOverallResultSuccess()) {
                componentOperationResult.merge(cor);
                return;
            }
        }*/
        logger.info("Executing operation: " + componentOperationType.toString());
        componentOperationResult.setCompOperType(componentOperationType);            
        executeOperation(componentOperationType);
        logger.info("Result of operation execution: " + componentOperationResult.isOverallResultSuccess());
        //CompOperResult cor = executeOperation(componentOperationType);
        //componentOperationResult.merge(cor);
    }
    
    protected abstract void executeOperation(CompOperType componentOperationType);
    //public abstract CompOperResult executeOperation(CompOperType componentOperationType);
    
    public SOATFCompType getComponentType() {
        return this.componentType;
    }

    public void setComponentType(SOATFCompType componentType) {
        this.componentType = componentType;
    }
    
    public String getIdentificator() {
        return this.identificator;
    }
    
    public void setIdentificator(String identificator){
        this.identificator = identificator;
    }
    
    public CompOperResult getComponentOperationResult() {
        return this.componentOperationResult;
    }
    
    public void setComponentOperationResult(CompOperResult componentOperationResult) {
        this.componentOperationResult = componentOperationResult;
    }
    
}

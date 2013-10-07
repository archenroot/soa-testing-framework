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

    private SOATFCompType componentType = null;
    private CompOperResult componentOperationResult = null;
    
    public SOATFComponent(SOATFCompType componentType) {
        this.componentType = componentType;

    }

    protected abstract void constructComponent();

    //public abstract void executeOperation(CompOperType componentOperationType);
    public abstract CompOperResult executeOperation(CompOperType componentOperationType);
    
    public SOATFCompType getComponentType() {
        return this.componentType;
    }

    public void setComponentType(SOATFCompType componentType) {
        this.componentType = componentType;
       
    }
    
}

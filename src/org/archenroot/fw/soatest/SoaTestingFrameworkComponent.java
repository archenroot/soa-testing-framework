/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archenroot.fw.soatest;

import java.io.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.archenroot.fw.soatest.SoaTestingFrameworkComponentType.ComponentOperation;

/**
 *
 * @author zANGETSu
 */
public abstract class SoaTestingFrameworkComponent {

    Logger logger = LogManager.getLogger(SoaTestingFrameworkComponent.class.getName());

    private SoaTestingFrameworkComponentType componentType = null;
    
    
    public SoaTestingFrameworkComponent(SoaTestingFrameworkComponentType componentType) {
        this.componentType = componentType;

    }

    protected abstract void constructComponent();

    public abstract void executeOperation(ComponentOperation componentOperation);

    public SoaTestingFrameworkComponentType getComponentType() {
        return this.componentType;
    }

    public void setComponentType(SoaTestingFrameworkComponentType componentType) {
        this.componentType = componentType;
    }
    
}

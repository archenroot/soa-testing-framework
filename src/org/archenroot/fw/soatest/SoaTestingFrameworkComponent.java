/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archenroot.fw.soatest;

import java.io.File;
import java.io.IOException;
import org.archenroot.fw.soatest.database.DatabaseTestComponent;
import org.archenroot.fw.soatest.file.FileTestComponent;
import org.archenroot.fw.soatest.jms.JmsTestComponent;
import org.archenroot.fw.soatest.ftp.FtpTestComponent;
import org.archenroot.fw.soatest.osbservicemanager.OsbTestComponent;
import org.archenroot.fw.soatest.soap.SoapTestComponent;
import org.archenroot.fw.soatest.xml.XmlTestComponent;

/**
 *
 * @author zANGETSu
 */
public abstract class SoaTestingFrameworkComponent {
    private SoaTestingFrameworkComponentType componentType = null;
    
    public SoaTestingFrameworkComponent(SoaTestingFrameworkComponentType componentType){
        this.componentType = componentType;
    }
    
    protected abstract void constructComponent();
    
    public SoaTestingFrameworkComponentType getComponentType(){
        return this.componentType;
    }
    
    public void setComponentType(SoaTestingFrameworkComponentType componentType){
        this.componentType = componentType;
    }
}

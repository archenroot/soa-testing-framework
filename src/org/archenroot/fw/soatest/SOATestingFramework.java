/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archenroot.fw.soatest;

import java.io.File;
import org.archenroot.fw.soatest.database.DatabaseTestComponent;
import org.archenroot.fw.soatest.file.FileTestComponent;
import org.archenroot.fw.soatest.jms.JMSTestComponent;
import org.archenroot.fw.soatest.ftp.FTPTestComponent;
import org.archenroot.fw.soatest.osbservicemanager.OSBServiceManagerTestComponent;
import org.archenroot.fw.soatest.soap.SOAPTestComponent;
import org.archenroot.fw.soatest.xml.XMLTestComponent;


/**
 *
 * @author zANGETSu
 */
public class SOATestingFramework {
    
    File soaTFConfigFile;
    
    
    SOATestingFramework(){
        
    }
    
    SOATestingFramework(String soaTFConfigFilePath){
        this.soaTFConfigFile = new File(soaTFConfigFilePath);
    }
    
    public void setSOATFConfigFile(String soaTFConfigFilePath){
        this.soaTFConfigFile = new File(soaTFConfigFilePath);
    }
    
    public void setSOATFConfigFile(File soaTFConfigFilePath){
        this.soaTFConfigFile = soaTFConfigFilePath;
    }
    
    public void initSOATF(){
        
    }
    
    public DatabaseTestComponent getDatabaseTestComponent(){
        return null;
    }
    public FileTestComponent getFileTestComponent(){
        return null;
    }
    public FTPTestComponent getFTPTestComponent(){
        return null;
    }
    public JMSTestComponent getJMSTestComponent(){
        return null;
    }
    public OSBServiceManagerTestComponent getOSBServiceManagerTestComponent(){
        return null;
    }
    public SOAPTestComponent getSOAPTestComponent(){
        return null;
    }
    public XMLTestComponent getXMLTestComponent(){
        return null;
    }  
}

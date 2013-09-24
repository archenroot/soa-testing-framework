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
import org.archenroot.fw.soatest.jms.JMSTestComponent;
import org.archenroot.fw.soatest.ftp.FTPTestComponent;
import org.archenroot.fw.soatest.osbservicemanager.OSBServiceManagerTestComponent;
import org.archenroot.fw.soatest.soap.SoapTestComponent;
import org.archenroot.fw.soatest.xml.XMLTestComponent;

/**
 *
 * @author zANGETSu
 */
public class SOATestingFramework {
    
    private File soaTFConfigFile;
    private SOATestingFrameworkConfiguration soaTFConfig;
    private DatabaseTestComponent dbTC;
    private FileTestComponent fileTC;
    private FTPTestComponent ftpTC;
    private JMSTestComponent jmsTC;
    private SoapTestComponent soapTC;
    
    // Dummy constructor not for use
    private SOATestingFramework(){
        
    }
    
    public SOATestingFramework(String soaTFConfigFilePath){
        this.soaTFConfigFile = new File(soaTFConfigFilePath);
        readConfiguration();
    }
    
    public void readConfiguration(){
        this.soaTFConfig = new SOATestingFrameworkConfiguration(this.soaTFConfigFile);
        
    }
    public void setSOATFConfigFile(String soaTFConfigFilePath){
        this.soaTFConfigFile = new File(soaTFConfigFilePath);
    }
    
    public void setSOATFConfigFile(File soaTFConfigFilePath){
        this.soaTFConfigFile = soaTFConfigFilePath;
    }
    
    public void initSOATF(){
        
    }
    
    public DatabaseTestComponent getDatabaseTestComponent() throws IOException{
        return new DatabaseTestComponent(
                this.soaTFConfig.getDatabaseType());
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
    public SoapTestComponent getSOAPTestComponent(){
        return null;
    }
    public XMLTestComponent getXMLTestComponent(){
        return null;
    }  

    public File getSoaTFConfigFile() {
        return soaTFConfigFile;
    }

    public SOATestingFrameworkConfiguration getSoaTFConfig() {
        return soaTFConfig;
    }
    
    public void setSoaTFConfigFile(File soaTFConfigFile) {
        this.soaTFConfigFile = soaTFConfigFile;
    }

    public void setSoaTFConfig(SOATestingFrameworkConfiguration soaTFConfig) {
        this.soaTFConfig = soaTFConfig;
    }

    
    
    
}

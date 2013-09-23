/*
 * Copyright (C) 2013 zANGETSu
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.archenroot.fw.soatest.xml;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.archenroot.fw.soatest.configuration.AdminServerType;
import org.archenroot.fw.soatest.configuration.DatabaseType;
import org.archenroot.fw.soatest.configuration.DatabaseTypeEnumType;
import org.archenroot.fw.soatest.configuration.EndPointType;
import org.archenroot.fw.soatest.configuration.HostsType;
import org.archenroot.fw.soatest.configuration.JMSServerType;
import org.archenroot.fw.soatest.configuration.JMSType;
import org.archenroot.fw.soatest.configuration.ManagedServerType;
import org.archenroot.fw.soatest.configuration.SOAPType;

/**
 *
 * @author zANGETSu
 */
public class SOATestingFrameworkConfiguration {
    private File SOATFXMLConfigFile;
    
    SOATestingFrameworkConfiguration(){
        // dummy constructor, dont' use it
    }
    
    public SOATestingFrameworkConfiguration(String xmlConfigFile){
        this.SOATFXMLConfigFile = new File(xmlConfigFile);
    }
    
    public SOATestingFrameworkConfiguration (File xmlConfigFile){
        this.SOATFXMLConfigFile = xmlConfigFile;
    }
    
    public EndPointType getEndPointType(){
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(EndPointType.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            File xmlFile = this.SOATFXMLConfigFile;
            EndPointType endPointType = (EndPointType) jaxbUnmarshaller.unmarshal(xmlFile);
           
            return endPointType;
            
        } catch (JAXBException ex) {
            Logger.getLogger(SOATestingFrameworkConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public DatabaseType getDatabaseType(){
        return this.getEndPointType().getDatabase();   
    }
 
    public AdminServerType getAdminServerType(){
        return this.getEndPointType().getJMS().getAdminServer();
    }
    
    public DatabaseTypeEnumType getDatabaseTypeEnumType(){
        return this.getEndPointType().getDatabase().getDatabaseType();
    }
    
    public JMSServerType getJMSServerType(){
        return this.getEndPointType().getJMS().getJmsServer();
    }
    
    public JMSType getJMSType(){
        return this.getEndPointType().getJMS();
    }
    
    public List<ManagedServerType> getManagedServerType(){
        return this.getEndPointType().getJMS().getManagedServer();
    }
    
    public SOAPType getSOAPType(){
        return this.getEndPointType().getSOAP();
    }
}

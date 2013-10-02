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
package com.ibm.fm.soatest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.ibm.fm.soatest.database.DatabaseComponent;

/**
 *
 * @author zANGETSu
 */
public abstract class SoaTestingFrameworkConfiguration {
    
    private static final Logger logger = LogManager.getLogger(DatabaseComponent.class.getName());
    
    private String soaTestingFrameworkXmlConfigFileName;
    
    public SoaTestingFrameworkConfiguration (String soaTestingFrameworkXmlConfigFile){
        this.soaTestingFrameworkXmlConfigFileName = soaTestingFrameworkXmlConfigFile;
    }
    
    protected abstract void loadConfiguration(String soaTestingFrameworkXmlConfigFile);
    
    
    /*
    
    public SoaTestingFramework getSoaTestingFramework() throws IOException{
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(SoaTestingFramework.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            File xmlFile = this.soaTestingFrameworkXmlConfigFileName;
            System.out.println("Current dir: " + new File(".").getCanonicalPath());
            boolean fileExists = xmlFile.exists();
            SoaTestingFramework soaTestingFramework = (SoaTestingFramework) jaxbUnmarshaller.unmarshal(xmlFile);
           
            return soaTestingFramework;
            
        } catch (JAXBException ex) {
            Logger.getLogger(SoaTestingFrameworkConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
   */

}

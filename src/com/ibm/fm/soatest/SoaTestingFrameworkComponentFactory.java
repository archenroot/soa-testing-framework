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

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.ibm.fm.soatest.configuration.SoaTestingFramework;


import com.ibm.fm.soatest.database.DatabaseComponent;
import com.ibm.fm.soatest.file.FileComponent;
import com.ibm.fm.soatest.ftp.FtpComponent;
import com.ibm.fm.soatest.jms.JmsComponent;
import com.ibm.fm.soatest.osb.OsbComponent;
import com.ibm.fm.soatest.rest.RestComponent;
import com.ibm.fm.soatest.soap.SoapComponent;
import com.ibm.fm.soatest.tool.ToolComponent;
import com.ibm.fm.soatest.xml.XmlComponent;


/**
 *
 * @author zANGETSu
 */
public class SoaTestingFrameworkComponentFactory {

    private static final Logger logger = LogManager.getLogger(SoaTestingFrameworkComponentFactory.class.getName());
    private static final String configurationXmlFileNameProperty = "soa_testing_framework_configuration_file";
    
    private static Properties initProperties;
    private static File xmlConfigurationFileName;
    
    Class currentBuilderClass = null;
    private JAXBContext jaxbContext = null;
    
    public static SoaTestingFrameworkComponent buildSoaTestingFrameworkComponent(
            SoaTestingFrameworkComponentType soaTestingFrameworkComponentType) {
        logger.debug("Started to build framework component.");
        SoaTestingFrameworkComponent soaTestingFrameworkComponent = null;
        initProperties = ConfigurationInit.getCoinfigurationInit();
        xmlConfigurationFileName = new File(initProperties.getProperty(configurationXmlFileNameProperty));
        
        String currentDir = null;
        if (!xmlConfigurationFileName.exists()){
            try {
                currentDir = new File(".").getCanonicalPath().toString();
                throw new IOException();
            } catch (IOException ex) {
                logger.error("XML configuration file doesn't exist in expected location: " + currentDir +  xmlConfigurationFileName.getName() + ex.getMessage().toString());
            }
        }
        
        
        switch (soaTestingFrameworkComponentType) {
            case DATABASE:
                soaTestingFrameworkComponent = new DatabaseComponent(getUnmarshalledConfiguration().getDatabaseConfiguration());
                break;
            case FILE:
                soaTestingFrameworkComponent = new FileComponent();
                break;
            case FTP:
                soaTestingFrameworkComponent = new FtpComponent();
                break;
            case JMS:
                soaTestingFrameworkComponent = new JmsComponent(getUnmarshalledConfiguration().getJmsConfiguration());
                break;
            case OSB:
                soaTestingFrameworkComponent = new OsbComponent(getUnmarshalledConfiguration().getOsbConfiguration());
                break;
            case REST:
                soaTestingFrameworkComponent = new RestComponent();
                break;
            case SOAP:
                soaTestingFrameworkComponent = new SoapComponent();
                break;
            case TOOL:
                soaTestingFrameworkComponent = new ToolComponent();
                break;
            case XML:
                soaTestingFrameworkComponent = new XmlComponent();
                break;
        }

        return soaTestingFrameworkComponent;

    }

  
    private static SoaTestingFramework getUnmarshalledConfiguration(){
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(SoaTestingFramework.class);
            
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            
            SoaTestingFramework myJAXBObject;            
            


//System.out.println("Current dir: " + new File(".").getCanonicalPath());
            Object my =  jaxbUnmarshaller.unmarshal(xmlConfigurationFileName);
            
            
            
            Object o = jaxbUnmarshaller.unmarshal(xmlConfigurationFileName);
            SoaTestingFramework soaTestingFramework = (SoaTestingFramework) jaxbUnmarshaller.unmarshal(xmlConfigurationFileName);
           
            return soaTestingFramework;
            
        } catch (JAXBException ex) {
            ex.printStackTrace();
            logger.error(ex.toString());
        }
        return null;
    }
}

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
package ibm.soatest;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ibm.soatest.config.SOATestingFrameworkConfiguration;


import ibm.soatest.database.DatabaseComponent;
import ibm.soatest.file.FILEComponent;
import ibm.soatest.ftp.FTPComponent;
import ibm.soatest.jms.JMSComponent;
import ibm.soatest.osb.OSBComponent;
import ibm.soatest.rest.RESTComponent;
import ibm.soatest.soap.SOAPComponent;
import ibm.soatest.tool.ToolComponent;
import ibm.soatest.xml.XMLComponent;


/**
 *
 * @author zANGETSu
 */
public class SOATFCompFactory {

    private static final Logger logger = LogManager.getLogger(SOATFCompFactory.class.getName());
    private static final String configurationXmlFileNameProperty = "soa_testing_framework_configuration_file";
    
    private static Properties initProperties;
    private static File xmlConfigurationFileName;
    
    
    Class currentBuilderClass = null;
    private JAXBContext jaxbContext = null;
    
    public static SOATFComponent builSOATFComponent(
            SOATFCompType soaTestingFrameworkComponentType) {
        logger.debug("Started to build framework component.");
        SOATFComponent soaTestingFrameworkComponent = null;
        
        initProperties = ConfigInit.getCoinfigurationInit();
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
                soaTestingFrameworkComponent = new DatabaseComponent(getUnmarshalledConfiguration().getDatabase());
                break;
            case FILE:
                soaTestingFrameworkComponent = new FILEComponent();
                break;
            case FTP:
                soaTestingFrameworkComponent = new FTPComponent();
                break;
            case JMS:
                soaTestingFrameworkComponent = new JMSComponent(getUnmarshalledConfiguration()..getJms());
                break;
            case OSB:
                soaTestingFrameworkComponent = new OSBComponent(getUnmarshalledConfiguration().getOsb());
                break;
            case REST:
                soaTestingFrameworkComponent = new RESTComponent();
                break;
            case SOAP:
                soaTestingFrameworkComponent = new SOAPComponent();
                break;
            case TOOL:
                soaTestingFrameworkComponent = new ToolComponent();
                break;
            case XML:
                soaTestingFrameworkComponent = new XMLComponent();
                break;
        }

        return soaTestingFrameworkComponent;

    }

  
    private static SOATestingFrameworkConfiguration getUnmarshalledConfiguration(){
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(SOATestingFrameworkConfiguration.class);
            
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            
            SOATestingFrameworkConfiguration myJAXBObject;            
            


//System.out.println("Current dir: " + new File(".").getCanonicalPath());
            Object my =  jaxbUnmarshaller.unmarshal(xmlConfigurationFileName);
            
            
            
            Object o = jaxbUnmarshaller.unmarshal(xmlConfigurationFileName);
            SOATestingFrameworkConfiguration soaTestingFrameworkConfiguration
                    = (SOATestingFrameworkConfiguration) jaxbUnmarshaller.unmarshal(xmlConfigurationFileName);
           
            return soaTestingFrameworkConfiguration;
            
        } catch (JAXBException ex) {
            ex.printStackTrace();
            logger.error(ex.toString());
        }
        return null;
    }
}

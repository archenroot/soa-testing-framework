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

import ibm.soatest.config.DatabaseConfiguration;
import ibm.soatest.config.JMSConfiguration;
import ibm.soatest.config.MappingConfiguration;
import ibm.soatest.config.OSBConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ibm.soatest.config.SOATestingFrameworkConfiguration;
import ibm.soatest.config.Service;

import ibm.soatest.database.DatabaseComponent;
import ibm.soatest.file.FileComponent;
import ibm.soatest.ftp.FTPComponent;
import ibm.soatest.jms.JMSComponent;
import ibm.soatest.mapping.MappingComponent;
import ibm.soatest.osb.OSBComponent;
import ibm.soatest.rest.RESTComponent;
import ibm.soatest.soap.SOAPComponent;
import ibm.soatest.tool.ToolComponent;
import ibm.soatest.xml.XMLComponent;
import java.util.ListIterator;

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

    public static SOATFComponent buildSOATFComponent(
            SOATFCompType soaTestingFrameworkComponentType,
            String identificator, CompOperResult componentOperationResult) {
        logger.debug("Factory started to build "+soaTestingFrameworkComponentType.name()+" component with id: "+ identificator +".");
        SOATFComponent soaTestingFrameworkComponent = null;

        initProperties = ConfigInit.getCoinfigurationInit();
        xmlConfigurationFileName = new File(initProperties.getProperty(configurationXmlFileNameProperty));

        String currentDir = null;
        if (!xmlConfigurationFileName.exists()) {
            try {
                currentDir = new File(".").getCanonicalPath().toString();
                throw new IOException();
            } catch (IOException ex) {
                logger.error("XML configuration file doesn't exist in expected location: " + 
                        currentDir + 
                        xmlConfigurationFileName.getName() + 
                        ex.getLocalizedMessage().toString());
            }
        }

        switch (soaTestingFrameworkComponentType) {
            case DATABASE:
                soaTestingFrameworkComponent = new DatabaseComponent(getUnmarshalledDatabaseConfiguration(identificator), componentOperationResult);
                break;
            case FILE:
                soaTestingFrameworkComponent = new FileComponent(componentOperationResult);
                break;
            case FTP:
                soaTestingFrameworkComponent = new FTPComponent(componentOperationResult);
                break;
            case JMS:
                soaTestingFrameworkComponent = new JMSComponent(getUnmarshalledJmsConfiguration(identificator), getUnmarshalledOSBConfiguration(), componentOperationResult, identificator);
                break;
            case OSB:
                soaTestingFrameworkComponent = new OSBComponent(getUnmarshalledOSBConfiguration(), componentOperationResult, identificator);
                break;
            case REST:
                soaTestingFrameworkComponent = new RESTComponent(componentOperationResult);
                break;
            case SOAP:
                soaTestingFrameworkComponent = new SOAPComponent(componentOperationResult);
                break;
            case TOOL:
                soaTestingFrameworkComponent = new ToolComponent(componentOperationResult);
                break;
            case XML:
                //soaTestingFrameworkComponent = new XMLComponent(getUnmarshalledJmsConfiguration(identificator), componentOperationResult, identificator);
                break;
            case MAPPING:
                soaTestingFrameworkComponent = new MappingComponent(getUnmarshalledMappingConfiguration(identificator), componentOperationResult, identificator);
                break;                
        }
        if (soaTestingFrameworkComponent != null) {
            logger.debug("Factory successfuly built the "+soaTestingFrameworkComponentType.name()+" component.");
        } else {
            componentOperationResult.setOverallResultSuccess(false);
            componentOperationResult.addMsg("Factory failed to build the "+soaTestingFrameworkComponentType.name()+" component.");
            logger.error("Factory failed to build the "+soaTestingFrameworkComponentType.name()+" component.");
        }
        return soaTestingFrameworkComponent;

    }

    private static DatabaseConfiguration getUnmarshalledDatabaseConfiguration(String identificator) {
        JAXBContext jaxbContext;
        Unmarshaller jaxbUnmarshaller;
        DatabaseConfiguration dc;
        
        try {
            jaxbContext = JAXBContext.newInstance(SOATestingFrameworkConfiguration.class);
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            SOATestingFrameworkConfiguration soaTestingFrameworkConfiguration
                    = (SOATestingFrameworkConfiguration) jaxbUnmarshaller.unmarshal(xmlConfigurationFileName);
            ListIterator<DatabaseConfiguration> i = soaTestingFrameworkConfiguration.getDatabase().listIterator();
            
            while (i.hasNext()) {
                dc = i.next();
                if (dc.getIdentificator().equals(identificator)) {
                    return dc;
                }
            }
            // Throwing exception if the database configuration cannot be found by using identificator atribute
            throw new MissingXMLConfigurationException();
        } catch (JAXBException ex) {
            ex.printStackTrace();
            logger.error("Error while unmarshalling DatabaseConfiguration object from XML: " + ex.getLocalizedMessage());
        } catch (MissingXMLConfigurationException ex) {
            logger.error("Cannot find database configuration element with specific identificator provided while creating component. Identificator lookup value: " + identificator + ". " + ex.getLocalizedMessage());
        }
        return null;

    }
    
    private static OSBConfiguration getUnmarshalledOSBConfiguration() {
        JAXBContext jaxbContext;
        Unmarshaller jaxbUnmarshaller;
        OSBConfiguration oc;
        Service s = null;
        
        try {
            jaxbContext = JAXBContext.newInstance(SOATestingFrameworkConfiguration.class);
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            SOATestingFrameworkConfiguration soaTestingFrameworkConfiguration
                    = (SOATestingFrameworkConfiguration) jaxbUnmarshaller.unmarshal(xmlConfigurationFileName);
            oc = soaTestingFrameworkConfiguration.getOsb();
            if (oc != null) {
                
                return oc;
            }
            // Throwing exception if the osb configuration cannot be found by using identificator atribute
            throw new MissingXMLConfigurationException();
        } catch (JAXBException ex) {
            ex.printStackTrace();
            logger.error("Error while unmarshalling OSBConfiguration object from XML: " + ex.getLocalizedMessage());
        } catch (MissingXMLConfigurationException ex) {
            logger.error("Cannot find OSB service configuration. " + ex.getLocalizedMessage());
        }
        return null;

    }
    
    private static JMSConfiguration getUnmarshalledJmsConfiguration(String identificator) {
        JAXBContext jaxbContext;
        Unmarshaller jaxbUnmarshaller;
        JMSConfiguration jc;
        
        try {
            jaxbContext = JAXBContext.newInstance(SOATestingFrameworkConfiguration.class);
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            SOATestingFrameworkConfiguration soaTestingFrameworkConfiguration
                    = (SOATestingFrameworkConfiguration) jaxbUnmarshaller.unmarshal(xmlConfigurationFileName);
            ListIterator<JMSConfiguration> i = soaTestingFrameworkConfiguration.getJms().listIterator();
            
            while (i.hasNext()) {
                jc = i.next();
                if (jc.getIdentificator().equals(identificator)) {
                    return jc;
                }
            }
            // Throwing exception if the database configuration cannot be found by using identificator atribute
            throw new MissingXMLConfigurationException();
        } catch (JAXBException ex) {
            ex.printStackTrace();
            logger.error("Error while unmarshalling JmsConfiguration object from XML: " + ex.getLocalizedMessage());
        } catch (MissingXMLConfigurationException ex) {
            logger.error("Cannot find JMS configuration element with specific identificator provided while creating component. Identificator lookup value: " + identificator + ". " + ex.getLocalizedMessage());
        }
        return null;

    } 
    
    private static MappingConfiguration getUnmarshalledMappingConfiguration(String identificator) {
        JAXBContext jaxbContext;
        Unmarshaller jaxbUnmarshaller;
        MappingConfiguration mc;
        
        try {
            jaxbContext = JAXBContext.newInstance(SOATestingFrameworkConfiguration.class);
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            SOATestingFrameworkConfiguration soaTestingFrameworkConfiguration
                    = (SOATestingFrameworkConfiguration) jaxbUnmarshaller.unmarshal(xmlConfigurationFileName);
            ListIterator<MappingConfiguration> i = soaTestingFrameworkConfiguration.getMapping().listIterator();
            
            while (i.hasNext()) {
                mc = i.next();
                if (mc.getIdentificator().equals(identificator)) {
                    return mc;
                }
            }
            // Throwing exception if the database configuration cannot be found by using identificator atribute
            throw new MissingXMLConfigurationException();
        } catch (JAXBException ex) {
            ex.printStackTrace();
            logger.error("Error while unmarshalling MappingConfiguration object from XML: " + ex.getLocalizedMessage());
        } catch (MissingXMLConfigurationException ex) {
            logger.error("Cannot find Mapping configuration element with specific identificator provided while creating component. Identificator lookup value: " + identificator + ". " + ex.getLocalizedMessage());
        }
        return null;

    }    

    /*private static SOATestingFrameworkConfiguration getUnmarshalledConfiguration(String identificator) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(SOATestingFrameworkConfiguration.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            SOATestingFrameworkConfiguration soaTestingFrameworkConfiguration
                    = (SOATestingFrameworkConfiguration) jaxbUnmarshaller.unmarshal(xmlConfigurationFileName);
            
            Object o = jaxbUnmarshaller.unmarshal(xmlConfigurationFileName);


//System.out.println("Current dir: " + new File(".").getCanonicalPath());

            return soaTestingFrameworkConfiguration;

        } catch (JAXBException ex) {
            ex.printStackTrace();
            logger.error(ex.toString());
        }
        return null;
    }*/
}

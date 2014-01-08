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
package com.ibm.soatf.component;

import com.ibm.soatf.flow.OperationResult;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import javax.xml.bind.JAXBContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private JAXBContext jaxbContext;

    /**
     * Factory master method for build 
     * @param soaTestingFrameworkComponentType type of component to be created
     * @param identificator identificator for the component defined within configuration file
     * @param cor instance of operation result
     * @return
     */
    public static AbstractSOATFComponent buildSOATFComponent(
           
            
            SOATFCompType soaTestingFrameworkComponentType,
            OperationResult cor) {
        logger.debug("Factory started to build " + soaTestingFrameworkComponentType.name() + " component.");
        AbstractSOATFComponent soaTestingFrameworkComponent = null;

        //initProperties = FrameworkConfiguration.getCoinfigurationInit();
        xmlConfigurationFileName = new File(initProperties.getProperty(configurationXmlFileNameProperty));

        String currentDir = null;
        if (!xmlConfigurationFileName.exists()) {
            try {
                currentDir = new File(".").getCanonicalPath().toString();
                throw new IOException();
            } catch (IOException ex) {
                logger.error("XML configuration file doesn't exist in expected location: "
                        + currentDir
                        + xmlConfigurationFileName.getName()
                        + ex.getLocalizedMessage().toString());
            }
        }
/*
        switch (soaTestingFrameworkComponentType) {
            case OSB:
                soaTestingFrameworkComponent = new OSBComponent();
                break;
            case DATABASE:
                soaTestingFrameworkComponent = new DatabaseComponent();
                break;
            case FILE:
                soaTestingFrameworkComponent = new FileComponent();
                break;
            case FTP:
                soaTestingFrameworkComponent = new FTPComponent();
                break;
            case JMS:
                soaTestingFrameworkComponent = new JMSComponent();
                break;
            case REST:
                soaTestingFrameworkComponent = new RESTComponent();
                break;
            case SOAP:
                soaTestingFrameworkComponent = new SOAPComponent();
                break;
            case TOOL:
                soaTestingFrameworkComponent = new ToolComponent(componentOperationResult);
                break;
            case XML:
                //soaTestingFrameworkComponent = new XMLComponent(getUnmarshalledJmsConfiguration(identificator), componentOperationResult, identificator);
                break;
            case MAPPING:
                soaTestingFrameworkComponent = new MappingComponent(getMappingConfiguration(identificator), componentOperationResult, identificator);
                break;
        }
        */
        if (soaTestingFrameworkComponent != null) {
            logger.debug("Factory successfuly built the " + soaTestingFrameworkComponentType.name() + " component.");
            cor.markSuccessful();
        } else {
            cor.addMsg("Factory failed to build the " + soaTestingFrameworkComponentType.name() + " component.");
            logger.error("Factory failed to build the " + soaTestingFrameworkComponentType.name() + " component.");
        }
        return soaTestingFrameworkComponent;

    }

    
}

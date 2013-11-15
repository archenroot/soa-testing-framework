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
package com.ibm.soatf;

import com.ibm.soatf.config.master.FileSystemProjectStructure;
import com.ibm.soatf.config.master.Interface;
import com.ibm.soatf.config.master.Project;
import com.ibm.soatf.config.master.SOATestingFrameworkMasterConfiguration;
import static com.ibm.soatf.util.JavaEnvironment.printJavaEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.ListIterator;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.helpers.FileUtils;

/**
 *
 * @author zANGETSu
 */
public class FrameworkConfiguration {

    private static final Logger logger = LogManager.getLogger(FrameworkConfiguration.class.getName());

    public static File propertiesFile;
    public static final String SOA_TEST_HOME = getSOATestHome();
    public static final String SOATF_HOME = getSOATFHome();
    public static final String SOA_TEST_HOME_ENV_VAR = "SOA_TEST_HOME";
    public static final String SOATF_HOME_ENV_VAR = "SOATF_HOME";
    public static final String JAXB_CONTEXT_PACKAGE = "com.ibm.soatf.config.master";

    public static final String SOATF_PARRENT_CONFIGURATION_FILE = "SOATestingFrameworkMasterConfiguration.xml";
    
    public static final String SOATF_CONFIGURATION_FILE = SOA_TEST_HOME + SOATF_PARRENT_CONFIGURATION_FILE;
    public static String INTERFACE;
    public static String SOATF_CURRENT_INTERFACE_FILE = SOA_TEST_HOME + INTERFACE + "\\" + INTERFACE + ".xml";

    public static SOATestingFrameworkMasterConfiguration soaTFMasterConfig;
    public static FileSystemProjectStructure fsps;

    public static void init() {
        checkConfiguration();
    }

    public static void createFileSystemStructure() {

    }

    private static void checkConfiguration() {
        printJavaEnvironment();
        checkEnvironment();
        checkFrameworkFileSystemStructure();
    }

    private static void checkEnvironment() {
        logger.debug("Checking environment.");

        try {
            // Checking environment variable
            if (FrameworkConfiguration.SOA_TEST_HOME == null) {
                throw new FrameworkConfigurationException("Environment variable SOA_TEST_HOME not set.");
            } else {
                logger.debug("Environemnt variable SOA_TEST_HOME is set to: " + FrameworkConfiguration.SOA_TEST_HOME);
            }
            // Checking environment variable
            if (FrameworkConfiguration.SOATF_HOME == null) {
                throw new FrameworkConfigurationException("Environment variable SOATF_HOME not set.");
            } else {
                logger.debug("Environemnt variable SOATF_HOME is set to: " + FrameworkConfiguration.SOATF_HOME);
            }
            // Checking root directory
            if (new File(FrameworkConfiguration.SOA_TEST_HOME).exists()) {
                logger.debug("Root testing file system object " + FrameworkConfiguration.SOA_TEST_HOME + " exists.");
                if (new File(FrameworkConfiguration.SOA_TEST_HOME).isDirectory()) {
                    logger.debug("Root testing file system object " + FrameworkConfiguration.SOA_TEST_HOME + " is directory.");
                } else {
                    throw new FrameworkConfigurationException("Root testing file system object " + FrameworkConfiguration.SOA_TEST_HOME + " is not directory!");
                }
            } else {
                throw new FrameworkConfigurationException("Root testing file system object " + FrameworkConfiguration.SOA_TEST_HOME + " doesn't exists!");
            }

            // Checking parrent configuration file
            if (new File(FrameworkConfiguration.SOA_TEST_HOME + FrameworkConfiguration.SOATF_PARRENT_CONFIGURATION_FILE).exists()) {
                logger.debug("Framework parrent configuration object "
                        + FrameworkConfiguration.SOA_TEST_HOME + FrameworkConfiguration.SOATF_PARRENT_CONFIGURATION_FILE
                        + " exists.");
                if (new File(FrameworkConfiguration.SOA_TEST_HOME + FrameworkConfiguration.SOATF_PARRENT_CONFIGURATION_FILE).isFile()) {
                    logger.debug("Framework parrent configuration object "
                            + FrameworkConfiguration.SOA_TEST_HOME + FrameworkConfiguration.SOATF_PARRENT_CONFIGURATION_FILE
                            + " is file.");
                } else {
                    throw new FrameworkConfigurationException("Framework parrent configuration object "
                            + FrameworkConfiguration.SOA_TEST_HOME + FrameworkConfiguration.SOATF_PARRENT_CONFIGURATION_FILE
                            + " exists, but is not file!");
                }
            } else {
                throw new FrameworkConfigurationException("Framework parrent configuration object "
                        + FrameworkConfiguration.SOA_TEST_HOME + FrameworkConfiguration.SOATF_PARRENT_CONFIGURATION_FILE
                        + " doesn't exists!");
            }
        } catch (FrameworkConfigurationException fce) {
            logger.error("Environment configuration exception: ", fce);
        } finally {
            logger.debug("Framework environment configuration is ok.");
        }
    }

    private static Map<String, String> getEnvironment() {
        Map<String, String> env = System.getenv();
        logger.debug("Reading system environment.");
        for (String envName : env.keySet()) {
            logger.debug("Variable " + envName + "=" + env.get(envName));
        }
        return System.getenv();
    }

    private static String getSOATFHome() {
        return System.getenv().get(FrameworkConfiguration.SOATF_HOME_ENV_VAR);
    }

    private static String getSOATestHome() {
        return System.getenv().get(FrameworkConfiguration.SOA_TEST_HOME_ENV_VAR);
    }

    private static void checkFrameworkFileSystemStructure() {
        try {
            soaTFMasterConfig = getSOATFMasterConfiguration();
            if (soaTFMasterConfig == null) {
                throw new FrameworkConfigurationException("Looks like master XML configuration file is corrupted or not in required format and therefore the object is null.");
            }
            fsps = soaTFMasterConfig.getFileSystemStructure();
            if (fsps == null) {
                throw new FrameworkConfigurationException("Looks like the default soa testing framework filesystem structure definition is missing in the master XML configuration file.");
            }
            ListIterator<Interface> interfaces = soaTFMasterConfig.getInterfaces().getInterface().listIterator();
            Interface projectInterface;
            while (interfaces.hasNext()) {
                validateInterfaceStructure(projectInterface = interfaces.next());
            }
        } catch (FrameworkConfigurationException fce) {
            logger.error("There is something wrong with the framework file system structure configuration.", fce);
        }
    }

    private static void validateInterfaceStructure(Interface projectInterface) throws FrameworkConfigurationException {

        /*
        // create master interface folder
        createFolder(SOA_TEST_HOME + projectInterface.getName());
        /*
         * Create interface dummy projects folders. There will be nothing saved under those folders.
         * Main purpose is just to have the user view on all projects under the interface, because there doesn't exist 
         * one general naming interace convention, which is wrong!!!
         */
        
        /*
        ListIterator<Project> projects = projectInterface.getProject().listIterator();
        while (projects.hasNext()) {
            createFolder(SOA_TEST_HOME + projectInterface.getName() + "\\" + projects.next().getName());
        }

        // Create endpoint testing folders
        // Root folder
        createFolder(SOA_TEST_HOME + projectInterface.getName() + "\\" + fsps.getRoot().getName());
        
        // Current test folder
        createFolder(SOA_TEST_HOME + projectInterface.getName() + "\\" + fsps.getRoot().getName() + "\\" + fsps.getRoot().getCurrentTest().getName());
        // Database
        createFolder(SOA_TEST_HOME + projectInterface.getName() + "\\" + fsps.getRoot().getName() + "\\" + fsps.getRoot().getCurrentTest().getName() + "\\" + fsps.getRoot().getDatabase().getName());
        // JMS
        createFolder(SOA_TEST_HOME + projectInterface.getName() + "\\" + fsps.getRoot().getName() + "\\" + fsps.getRoot().getCurrentTest().getName() + "\\" + fsps.getRoot().getJms().getName());
        // FTP
        createFolder(SOA_TEST_HOME + projectInterface.getName() + "\\" + fsps.getRoot().getName() + "\\" + fsps.getRoot().getCurrentTest().getName() + "\\" + fsps.getRoot().getFtp().getName());
        // SOAP
        createFolder(SOA_TEST_HOME + projectInterface.getName() + "\\" + fsps.getRoot().getName() + "\\" + fsps.getRoot().getCurrentTest().getName() + "\\" + fsps.getRoot().getSoap().getName());
        // Mapping
        createFolder(SOA_TEST_HOME + projectInterface.getName() + "\\" + fsps.getRoot().getName() + "\\" + fsps.getRoot().getCurrentTest().getName() + "\\" + fsps.getRoot().getMapping().getName());
        // Reporting
        createFolder(SOA_TEST_HOME + projectInterface.getName() + "\\" + fsps.getRoot().getName() + "\\" + fsps.getRoot().getCurrentTest().getName() + "\\" + fsps.getRoot().getReporting().getName());
        // Reporting
        createFolder(SOA_TEST_HOME + projectInterface.getName() + "\\" + fsps.getRoot().getName() + "\\" + fsps.getRoot().getCurrentTest().getName() + "\\" + fsps.getRoot().getTemporary().getName());

        // Previous test folder
        createFolder(SOA_TEST_HOME + projectInterface.getName() + "\\" + fsps.getRoot().getName() + "\\" + fsps.getRoot().getPreviousTest().getName());
        
        // Database
        createFolder(SOA_TEST_HOME + projectInterface.getName() + "\\" + fsps.getRoot().getName() + "\\" + fsps.getRoot().getPreviousTest().getName() + "\\" + fsps.getRoot().getDatabase().getName());
        // JMS
        createFolder(SOA_TEST_HOME + projectInterface.getName() + "\\" + fsps.getRoot().getName() + "\\" + fsps.getRoot().getPreviousTest().getName() + "\\" + fsps.getRoot().getJms().getName());
        // FTP
        createFolder(SOA_TEST_HOME + projectInterface.getName() + "\\" + fsps.getRoot().getName() + "\\" + fsps.getRoot().getPreviousTest().getName() + "\\" + fsps.getRoot().getFtp().getName());
        // SOAP
        createFolder(SOA_TEST_HOME + projectInterface.getName() + "\\" + fsps.getRoot().getName() + "\\" + fsps.getRoot().getPreviousTest().getName() + "\\" + fsps.getRoot().getSoap().getName());
        // Mapping
        createFolder(SOA_TEST_HOME + projectInterface.getName() + "\\" + fsps.getRoot().getName() + "\\" + fsps.getRoot().getPreviousTest().getName() + "\\" + fsps.getRoot().getMapping().getName());
        // Reporting
        createFolder(SOA_TEST_HOME + projectInterface.getName() + "\\" + fsps.getRoot().getName() + "\\" + fsps.getRoot().getPreviousTest().getName() + "\\" + fsps.getRoot().getReporting().getName());
        // Reporting
        createFolder(SOA_TEST_HOME + projectInterface.getName() + "\\" + fsps.getRoot().getName() + "\\" + fsps.getRoot().getPreviousTest().getName() + "\\" + fsps.getRoot().getTemporary().getName());
        */
    }

    private static void createFolder(String folder) throws FrameworkConfigurationException {
        try {
            FileUtils.mkdir(new File(folder), true);
        } catch (IOException ioex) {
            throw new FrameworkConfigurationException("Directory " + folder + " cannot be created.", ioex);
        }
    }

    public static SOATestingFrameworkMasterConfiguration getSOATFMasterConfiguration() {
        JAXBContext jaxbContext;
        Unmarshaller jaxbUnmarshaller;
        JAXBElement<SOATestingFrameworkMasterConfiguration> allMasterJAXBConfig = null;
        SOATestingFrameworkMasterConfiguration soatfmc = null;
        try {

            jaxbContext = JAXBContext.newInstance(FrameworkConfiguration.JAXB_CONTEXT_PACKAGE);
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            allMasterJAXBConfig = (JAXBElement<SOATestingFrameworkMasterConfiguration>) jaxbUnmarshaller.unmarshal(
                    new File(FrameworkConfiguration.SOA_TEST_HOME + FrameworkConfiguration.SOATF_PARRENT_CONFIGURATION_FILE));
            soatfmc = allMasterJAXBConfig.getValue();

        } catch (JAXBException jaxbex) {
            logger.fatal("Error on unmarshalling master configuration.", jaxbex);
        } finally {

        }
        return soatfmc;
    }
    
    private void checkConfigurationConsistency(){
        
    }
    private void prepareConfigurationFiles(){
        //vem xsd -> vygeneruj -> instanci XML
    }
}

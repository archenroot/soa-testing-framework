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

import static com.ibm.soatf.MasterConfiguration.getSOATestingFrameworkMasterConfiguration;
import com.ibm.soatf.config._interface.InterfaceFlowPattern;
import com.ibm.soatf.config._interface.InterfaceTestScenario;
import com.ibm.soatf.config.master.FileSystemProjectStructure;
import com.ibm.soatf.config.master.FileSystemProjectStructure.TestRoot.Directory;
import com.ibm.soatf.config.master.Interface;
import com.ibm.soatf.config.master.Project;
import com.ibm.soatf.config.master.SOATestingFrameworkMasterConfiguration;
import static com.ibm.soatf.util.JavaEnvironment.printJavaEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamResult;
import jlibs.xml.sax.XMLDocument;
import jlibs.xml.xsd.XSInstance;
import jlibs.xml.xsd.XSParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.helpers.FileUtils;
import org.apache.xerces.xs.XSModel;


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
    public static final String PATTERN_DIRECTORY_PREFIX = "FlowPattern - ";

    public static SOATestingFrameworkMasterConfiguration soaTFMasterConfig;
    public static FileSystemProjectStructure fsps;

    public static void init() {
        try {
            soaTFMasterConfig = getSOATestingFrameworkMasterConfiguration();
            if (soaTFMasterConfig == null) {
                throw new FrameworkConfigurationException("Looks like master XML configuration file is corrupted or not in required format and therefore the object is null.");
            }
            fsps = soaTFMasterConfig.getFileSystemStructure();
            if (fsps == null) {
                throw new FrameworkConfigurationException("Looks like the default soa testing framework filesystem structure definition is missing in the master XML configuration file.");
            }
            
        } catch (FrameworkConfigurationException ex) {
            java.util.logging.Logger.getLogger(FrameworkConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void checkConfiguration() {
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
            
            ListIterator<Interface> interfaces = soaTFMasterConfig.getInterfaces().getInterface().listIterator();
            Interface projectInterface;
            while (interfaces.hasNext()) {
                validateInterfaceStructure(projectInterface = interfaces.next());
            }
        } catch (FrameworkConfigurationException fce) {
            logger.error("There is something wrong with the framework file system structure configuration.", fce);
        }
    }

    public static void validateInterfaceStructure(Interface _interface) throws FrameworkConfigurationException {

        // create master interface folder
        createFolder(SOA_TEST_HOME + _interface.getName());
        /*
         * Create interface dummy projects folders. There will be nothing saved under those folders.
         * Main purpose is just to have the user view on all projects under the interface, because there doesn't exist 
         * one general naming interace convention, which is wrong!!!
         */

        ListIterator<Project> projectIt = _interface.getProjects().getProject().listIterator();
        while (projectIt.hasNext()) {
            createFolder(SOA_TEST_HOME + _interface.getName() + "\\" + "OSB reference project - " + projectIt.next().getName());
        }
        File interfaceConfigFile = new File(SOA_TEST_HOME + _interface.getName() + "\\" + _interface.getName() + ".xml");
        if (interfaceConfigFile.exists()) {
            InterfaceConfiguration ic = new InterfaceConfiguration(interfaceConfigFile);
            ListIterator<InterfaceFlowPattern> ifaceFlowPatternIterator = ic.getInterfaceFlowPatterns().listIterator();
            InterfaceFlowPattern ifaceFlowPattern;
            while (ifaceFlowPatternIterator.hasNext()){
                ifaceFlowPattern = ifaceFlowPatternIterator.next();
                String patternDirectory = SOA_TEST_HOME + _interface.getName() + "\\" + PATTERN_DIRECTORY_PREFIX + ifaceFlowPattern.getIdentificator();
                createFolder(patternDirectory);
                String testScenarioNameDirectory = patternDirectory + "\\" + ifaceFlowPattern.getTestName();
                createFolder(testScenarioNameDirectory);
                
                ListIterator<InterfaceTestScenario> ifaceTCIterator = ifaceFlowPattern.getInterfaceTestScenario().listIterator();
                InterfaceTestScenario interfaceTestScenario;
                while (ifaceTCIterator.hasNext() ){
                    interfaceTestScenario = ifaceTCIterator.next();
                    String testScenario = testScenarioNameDirectory + "\\" + interfaceTestScenario.getIdentificator();
                    createFolder(testScenario);
                    ListIterator<Directory> foldersIterator = fsps.getTestRoot().getDirectory().listIterator();
                    while (foldersIterator.hasNext()){
                        createFolder(testScenario + "\\" + foldersIterator.next().getName());
                    }
                }
            }
        } else {
            generateSampleIfaceConfigFile(interfaceConfigFile);
        }
    }

    private static void createFolder(String folder) throws FrameworkConfigurationException {
        try {
            FileUtils.mkdir(new File(folder), true);
        } catch (IOException ioex) {
            throw new FrameworkConfigurationException("Directory " + folder + " cannot be created.", ioex);
        }
    }

    private static void generateSampleIfaceConfigFile(File interfaceConfigFile) {
        try {
            XSInstance xsInstance = new XSInstance();
            xsInstance.minimumElementsGenerated = 2;
            xsInstance.maximumElementsGenerated = 4;
            xsInstance.generateOptionalElements = Boolean.TRUE; // null means random
            
            XSModel xsModel = new XSParser().parse(SOATF_HOME + "\\schema\\SOATestingFrameworkInterfaceConfiguration\\SOATestingFrameworkInterfaceConfiguration.xsd");
           
            
            QName rootElement = new QName("http://www.ibm.com/SOATF/Config/Interface", "soaTestingFrameworkInterfaceConfiguration", "stfconf");
            XMLDocument sampleXml = new XMLDocument(new StreamResult(System.out), true, 4, null);
            xsInstance.generate(xsModel, rootElement, sampleXml);
            
            /*try {
            /*
            JAXBContext jaxbContext = JAXBContext.newInstance("com.ibm.soatf.config._interface");
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            SOATestingFrameworkInterfaceConfiguration soaTFIC = new SOATestingFrameworkInterfaceConfiguration();
            
            JAXBElement jaxbElement = new JAXBElement(QName.valueOf("com.ibm.soatf.config._interface"));
            jaxbMarshaller.marshal(soaTFIC, interfaceConfigFile);
            } catch (JAXBException ex) {
            java.util.logging.Logger.getLogger(FrameworkConfiguration.class.getName()).log(Level.SEVERE, null, ex);
            }
            */
        } catch (TransformerConfigurationException ex) {
            java.util.logging.Logger.getLogger(FrameworkConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void checkConfigurationConsistency() {

    }

    private void prepareConfigurationFiles() {
        //vem xsd -> vygeneruj -> instanci XML
    }
}

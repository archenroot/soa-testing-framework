/*
 * Copyright (C) 2013 Ladislav Jech
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
package com.ibm.soatf.config;

import com.ibm.soatf.flow.FrameworkExecutionException;
import static com.ibm.soatf.config.MasterFrameworkConfig.FLOW_PATTERN_DIR_NAME_PREFIX;
import static com.ibm.soatf.config.MasterFrameworkConfig.IFACE_CONFIG_FILENAME;
import static com.ibm.soatf.config.MasterFrameworkConfig.OSB_REFERENCE_PROJECT_DIR_NAME_PREFIX;
import com.ibm.soatf.config.iface.IfaceFlowPattern;
import com.ibm.soatf.config.iface.IfaceTestScenario;
import com.ibm.soatf.config.master.FileSystemProjectStructure;
import com.ibm.soatf.config.master.Interface;
import com.ibm.soatf.config.master.Project;
import com.ibm.soatf.config.master.SOATestingFrameworkMasterConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.ListIterator;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.helpers.FileUtils;

/**
 *
 * @author Ladislav Jech <archenroot@gmail.com>
 */
public class DirectoryStructureManager {

    private static final Logger logger = LogManager.getLogger(DirectoryStructureManager.class.getName());

    /**
     *
     * @throws FrameworkConfigurationException
     */
    public static void checkFrameworkDirectoryStructure() throws FrameworkConfigurationException {

        MasterConfiguration masterConfig = ConfigurationManager.getInstance().getMasterConfig();
        final ListIterator<SOATestingFrameworkMasterConfiguration.Interfaces.Interface> interfaces = masterConfig.getXmlConfig().getInterfaces().getInterface().listIterator();
        while (interfaces.hasNext()) {
            validateIfaceStructure(interfaces.next());
        }

    }

    private static void validateIfaceStructure(Interface iface) throws FrameworkConfigurationException {
        try {
            MasterFrameworkConfig frameworkConfig = ConfigurationManager.getInstance().getFrameworkConfig();
            MasterConfiguration masterConfig = ConfigurationManager.getInstance().getMasterConfig();
            File soaTestHome = frameworkConfig.getSoaTestHome();
            final File interfaceFolder = new File(soaTestHome, iface.getName() + "_" + frameworkConfig.getValidFileSystemObjectName(iface.getDescription()));
            logger.trace("Working directory after format: " + interfaceFolder);
            try {
                createFolder(interfaceFolder);
            } catch (FrameworkExecutionException ex) {
                logger.error("Could not create folder " + interfaceFolder.getAbsolutePath());
            }
            
            /*
            * Create interface dummy projects folders. There will be nothing saved under those folders.
            * Main purpose is just to have the user view on all projects under the interface, because there doesn't exist
            * one general naming interace convention, which is wrong!!!
            */
            if (iface.getProjects() != null) {
                final ListIterator<Project> projectIt = iface.getProjects().getProject().listIterator();
                while (projectIt.hasNext()) {
                    File projectFolder = new File(interfaceFolder, OSB_REFERENCE_PROJECT_DIR_NAME_PREFIX + projectIt.next().getName());
                    try {
                        createFolder(projectFolder);
                    } catch (FrameworkExecutionException ex) {
                        logger.error("Could not create project folder " + projectFolder.getAbsolutePath());
                    }
                }
                final File interfaceConfigFile = new File(interfaceFolder, IFACE_CONFIG_FILENAME);
                if (!interfaceConfigFile.exists()) {
                    logger.warn("Interface configuration file is missing for interface " + iface.getName() + " " + iface.getDescription());
                    return;
                }
                //masterConfig.getIfaceConfigFile(iface.getName());
                if (interfaceConfigFile.exists()) {
                    try {
                        InterfaceConfiguration ic = masterConfig.getInterfaceConfig(iface);
                        final ListIterator<IfaceFlowPattern> ifaceFlowPatternIterator = ic.getIfaceFlowPatterns().listIterator();
                        IfaceFlowPattern ifaceFlowPattern;
                        int ifaceFlowPatternIteratorCount = 0;
                        while (ifaceFlowPatternIterator.hasNext()) {
                            ifaceFlowPatternIteratorCount++;
                            ifaceFlowPattern = ifaceFlowPatternIterator.next();
                            logger.debug("Processing " + interfaceConfigFile.getAbsolutePath());
                            final File patternDirectory = new File(interfaceFolder, FLOW_PATTERN_DIR_NAME_PREFIX + frameworkConfig.getValidFileSystemObjectName(ifaceFlowPattern.getRefId()));
                            createFolder(patternDirectory);
                            final String testName = ifaceFlowPattern.getInstanceMetadata().getTestName();
                            if (testName.isEmpty()) {
                                throw new FrameworkConfigurationException("There is missing test name in config.xml instance metadata element in file " + interfaceConfigFile.getAbsolutePath() + ".");
                            }
                            final File testScenarioNameDirectory = new File(patternDirectory, frameworkConfig.getValidFileSystemObjectName(testName));
                            createFolder(testScenarioNameDirectory);
                            
                            final ListIterator<IfaceTestScenario> ifaceTCIterator = ifaceFlowPattern.getIfaceTestScenario().listIterator();
                            IfaceTestScenario ifaceTestScenario;
                            while (ifaceTCIterator.hasNext()) {
                                ifaceTestScenario = ifaceTCIterator.next();
                                File testScenario = new File(testScenarioNameDirectory, frameworkConfig.getValidFileSystemObjectName(ifaceTestScenario.getRefId()));
                                createFolder(testScenario);
                                final ListIterator<FileSystemProjectStructure.TestRoot.Directory> foldersIterator = masterConfig.getXmlConfig().getFileSystemStructure().getTestRoot().getDirectory().listIterator();
                                while (foldersIterator.hasNext()) {
                                    final String fldName = frameworkConfig.getValidFileSystemObjectName(foldersIterator.next().getName());
                                    final File dir = new File(testScenario, fldName);
                                    createFolder(dir);
                                    // This is to check the pattern source folder and don't let it clean up
                                    cleanFolder(dir);
                                }
                            }
                        }
                    } catch (FrameworkConfigurationException fce) {
                        logger.error("Configuration for interface " + iface.getName() + " is invalid", fce);
                    } catch (FrameworkExecutionException fee) {
                        logger.error("Could not create folder.", fee);
                    }
                } else {
                    generateSampleIfaceConfigFile(interfaceConfigFile);
                }
            } else {
                logger.warn("Interface " + iface.getName() + "_" + iface.getDescription() + " has no projects defined.");
            }
        } catch (FrameworkConfigurationException ex) {
            final String msg = "TODO";
            throw new FrameworkConfigurationException(msg,ex);
        }
    }

    private static void createFolder(File folder) throws FrameworkExecutionException {
        try {
            FileUtils.mkdir(folder, true);
        } catch (IOException ioex) {
            throw new FrameworkExecutionException("Directory " + folder + " cannot be created.", ioex);
        }
    }

    private static void cleanFolder(File folder) {
        for (File file : folder.listFiles()) {
            file.delete();
        }
    }

    private static void generateSampleIfaceConfigFile(File interfaceConfigFile) {
        /*
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
         java.util.logging.Logger.getLogger(class.getName()).log(Level.SEVERE, null, ex);
         }
            
         } catch (TransformerConfigurationException ex) {
         java.util.logging.Logger.getLogger(class.getName()).log(Level.SEVERE, null, ex);
         }
         */
    }
}

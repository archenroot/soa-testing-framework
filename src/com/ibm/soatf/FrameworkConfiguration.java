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

import com.ibm.soatf.config.iface.IfaceFlowPattern;
import com.ibm.soatf.config.iface.IfaceTestScenario;
import com.ibm.soatf.config.master.FileSystemProjectStructure;
import com.ibm.soatf.config.master.FileSystemProjectStructure.TestRoot.Directory;
import com.ibm.soatf.config.master.Interface;
import com.ibm.soatf.config.master.Project;
import static com.ibm.soatf.tool.JavaEnvironment.printJavaEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.helpers.FileUtils;

/**
 * Class responsible for SOA Testing Framework initial configuration and
 * validation. Defined as singleton with on-demand initialisation.
 *
 * @author zANGETSu
 */
public class FrameworkConfiguration {

    /*
     * Singleton object itself of type eager singleton, so instantied on class load.
     */
    /*    public static final FrameworkConfiguration FConfig = new FrameworkConfiguration();

     private FrameworkConfiguration() {
     }
     */
    /*
     * Hidden constructor, other instances cannot be created.
     */
    /*
     * Lazy singleton initialisation(on first demnad). The method has to be 
     * synchronized to secure access from many threads.
     */
    private static FrameworkConfiguration instance;

    private FrameworkConfiguration() {
        SOATF_CONFIG_FILE = new File(SOA_TEST_HOME, SOATF_MASTER_CONFIG_FILE);
        if (!SOATF_CONFIG_FILE.exists() || SOATF_CONFIG_FILE.isDirectory()) {
            throw new FrameworkConfigurationException("There is something wronk with framework master configuration file configured as: "
                    + SOATF_CONFIG_FILE.getAbsolutePath()
                    + ". Objet doesn't exist or is not type of file."
            );
        }
    }

    public static synchronized FrameworkConfiguration getInstance() {
        if (instance == null) {
            instance = new FrameworkConfiguration();
        }
        return instance;
    }

    private final Logger logger = LogManager.getLogger(FrameworkConfiguration.class.getCanonicalName());

    public File propertiesFile;
    public final File SOA_TEST_HOME = new File(getSOATestHome());
    public final File SOATF_HOME = new File(getSOATFHome());
    public final String SOA_TEST_HOME_ENV_VAR = "SOA_TEST_HOME";
    public final String SOATF_HOME_ENV_VAR = "SOATF_HOME";
    public final String JAXB_CONTEXT_PACKAGE = "com.ibm.soatf.config.master";

    public final String SOATF_MASTER_CONFIG_FILE = "master-config.xml";
    public final String IFACE_CONFIG_FILE = "config.xml";

    public final String OSB_REFERENCE_PROJECT_DIR_NAME_PREFIX = "OSB_Reference_Project_-_";
    public final String FLOW_PATTERN_DIR_NAME_PREFIX = "FlowPattern_-_";

    public final File SOATF_CONFIG_FILE;

    private FileSystemProjectStructure fsps;

    private final MasterConfiguration MCFG = MasterConfiguration.getInstance();
    private final Map<String, InterfaceConfiguration> ICFG = new HashMap<>();

    private final String fsValidationPattern = "^[.\\\\/:*?\"<>|]?[\\\\/:*?\"<>|]*";

    public void checkConfiguration() {
        getFsps();
        printJavaEnvironment();
        checkEnvironment();
        checkFrameworkFileSystemStructure();
    }

    private void checkEnvironment() {
        logger.debug("Checking environment.");

        try {
            // Checking environment variable
            if (SOA_TEST_HOME == null) {
                throw new FrameworkConfigurationException("Environment variable SOA_TEST_HOME not set.");
            } else {
                logger.debug("Environemnt variable SOA_TEST_HOME is set to: " + SOA_TEST_HOME);
            }
            // Checking environment variable
            if (SOATF_HOME == null) {
                throw new FrameworkConfigurationException("Environment variable SOATF_HOME not set.");
            } else {
                logger.debug("Environemnt variable SOATF_HOME is set to: " + SOATF_HOME);
            }
            // Checking root directory
            if (SOA_TEST_HOME.exists()) {
                logger.debug("Root testing file system object " + SOA_TEST_HOME + " exists.");
                if (SOA_TEST_HOME.isDirectory()) {
                    logger.debug("Root testing file system object " + SOA_TEST_HOME + " is directory.");
                } else {
                    throw new FrameworkConfigurationException("Root testing file system object " + SOA_TEST_HOME + " is not directory!");
                }
            } else {
                throw new FrameworkConfigurationException("Root testing file system object " + SOA_TEST_HOME + " doesn't exists!");
            }

            // Checking parrent configuration file
            if (new File(SOA_TEST_HOME + "\\" + SOATF_MASTER_CONFIG_FILE).exists()) {
                logger.debug("Framework parrent configuration object "
                        + SOA_TEST_HOME + SOATF_MASTER_CONFIG_FILE
                        + " exists.");
                if (new File(SOA_TEST_HOME + "\\" + SOATF_MASTER_CONFIG_FILE).isFile()) {
                    logger.debug("Framework parrent configuration object "
                            + SOA_TEST_HOME + SOATF_MASTER_CONFIG_FILE
                            + " is file.");
                } else {
                    throw new FrameworkConfigurationException("Framework parrent configuration object "
                            + SOA_TEST_HOME + "\\" + SOATF_MASTER_CONFIG_FILE
                            + " exists, but is not file!");
                }
            } else {
                throw new FrameworkConfigurationException("Framework parrent configuration object "
                        + SOA_TEST_HOME + "\\" + SOATF_MASTER_CONFIG_FILE
                        + " doesn't exists!");
            }
        } catch (FrameworkConfigurationException fce) {
            logger.error("Environment configuration exception: ", fce);
        } finally {
            logger.debug("Framework environment configuration is ok.");
        }
    }

    private Map<String, String> getEnvironment() {
        final Map<String, String> env = System.getenv();
        logger.debug("Reading system environment.");
        for (final String envName : env.keySet()) {
            logger.debug("Variable " + envName + "=" + env.get(envName));
        }
        return System.getenv();
    }

    private String getSOATFHome() {
        return getEnvironment().get(SOATF_HOME_ENV_VAR);
    }

    private String getSOATestHome() {
        return getEnvironment().get(SOA_TEST_HOME_ENV_VAR);
    }

    private void checkFrameworkFileSystemStructure() {
        try {
            final ListIterator<Interface> interfaces = getMasterConfig().getXmlConfig().getInterfaces().getInterface().listIterator();
            Interface projectIface;
            while (interfaces.hasNext()) {
                validateIfaceStructure(projectIface = interfaces.next());
            }
        } catch (FrameworkConfigurationException fce) {
            logger.error("There is something wrong with the framework file system structure configuration.", fce);
        }
    }

    public void validateIfaceStructure(Interface iface) throws FrameworkConfigurationException {

        final String interfaceFolder = SOA_TEST_HOME + "\\" + iface.getName() + "_" + getValidFileSystemObjectName(iface.getDescription());
        logger.trace("Working directory after format: " + interfaceFolder);
        createFolder(interfaceFolder);

        /*
         * Create interface dummy projects folders. There will be nothing saved under those folders.
         * Main purpose is just to have the user view on all projects under the interface, because there doesn't exist 
         * one general naming interace convention, which is wrong!!!
         */
        final ListIterator<Project> projectIt = iface.getProjects().getProject().listIterator();
        while (projectIt.hasNext()) {
            createFolder(interfaceFolder + "\\" + OSB_REFERENCE_PROJECT_DIR_NAME_PREFIX + projectIt.next().getName());
        }
        final File interfaceConfigFile = new File(interfaceFolder + "\\" + IFACE_CONFIG_FILE);
        if (interfaceConfigFile.exists()) {
            InterfaceConfiguration ic = new InterfaceConfiguration(interfaceConfigFile);
            final ListIterator<IfaceFlowPattern> ifaceFlowPatternIterator = ic.getIfaceFlowPatterns().listIterator();
            IfaceFlowPattern ifaceFlowPattern;
            while (ifaceFlowPatternIterator.hasNext()) {
                ifaceFlowPattern = ifaceFlowPatternIterator.next();
                final String patternDirectory = interfaceFolder + "\\" + FLOW_PATTERN_DIR_NAME_PREFIX + getValidFileSystemObjectName(ifaceFlowPattern.getRefId());
                createFolder(patternDirectory);
                final String testScenarioNameDirectory = patternDirectory + "\\" + getValidFileSystemObjectName(ifaceFlowPattern.getTestName());
                createFolder(testScenarioNameDirectory);

                final ListIterator<IfaceTestScenario> ifaceTCIterator = ifaceFlowPattern.getIfaceTestScenario().listIterator();
                IfaceTestScenario ifaceTestScenario;
                while (ifaceTCIterator.hasNext()) {
                    ifaceTestScenario = ifaceTCIterator.next();
                    String testScenario = testScenarioNameDirectory + "\\" + getValidFileSystemObjectName(ifaceTestScenario.getRefId());
                    createFolder(testScenario);
                    final ListIterator<Directory> foldersIterator = getFsps().getTestRoot().getDirectory().listIterator();
                    while (foldersIterator.hasNext()) {
                        final String fldName = getValidFileSystemObjectName(foldersIterator.next().getName());
                        createFolder(testScenario + "\\" + fldName);
                        // This is to check the pattern source folder and don't let it clean up
                        cleanFolder(testScenario + "\\" + fldName);
                    }
                }
            }
        } else {
            generateSampleIfaceConfigFile(interfaceConfigFile);
        }
    }

    private void createFolder(String folder) throws FrameworkConfigurationException {
        try {
            FileUtils.mkdir(new File(folder), true);
        } catch (IOException ioex) {
            throw new FrameworkConfigurationException("Directory " + folder + " cannot be created.", ioex);
        }
    }

    private void cleanFolder(String folder) {
        for (File file : new File(folder).listFiles()) {
            file.delete();
        }
    }

    private void generateSampleIfaceConfigFile(File interfaceConfigFile) {
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

    public MasterConfiguration getMasterConfig() {
        return MCFG;
    }

    public synchronized InterfaceConfiguration getInterfaceConfig(String ifaceName) {
        if (!ICFG.containsKey(ifaceName)) {
            InterfaceConfiguration ifaceConfig = new InterfaceConfiguration(getIfaceConfigFile(ifaceName));
            ICFG.put(ifaceName, ifaceConfig);
        }
        return ICFG.get(ifaceName);
    }

    private void checkConfigurationConsistency() {

    }

    private void prepareConfigurationFiles() {
        //vem xsd -> vygeneruj -> instanci XML
    }

    public FileSystemProjectStructure getFsps() throws FrameworkConfigurationException {
        if (fsps == null) {
            fsps = MCFG.getXmlConfig().getFileSystemStructure();
        }
        return fsps;
    }

    public String getInterfaceDirName(String ifaceName) {
        return ifaceName + "_" + getValidFileSystemObjectName(MCFG.getInterface(ifaceName).getDescription());
    }

    public File getIfaceDir(String ifaceName) {
        return new File(SOA_TEST_HOME, getInterfaceDirName(ifaceName));
    }

    public File getIfaceConfigFile(String ifaceName) {
        return new File(getIfaceDir(ifaceName), IFACE_CONFIG_FILE);
    }

    public boolean isFileSystemNameValid(String fileSystemObjectName) {
        return !fileSystemObjectName.matches(fsValidationPattern)
                && getValidFileSystemObjectName(fileSystemObjectName).length() > 0;
    }

    public String getValidFileSystemObjectName(String string) {
        final String fileSystemObjectName = string.replaceAll(fsValidationPattern, "");
        if (fileSystemObjectName.length() == 0) {
            throw new IllegalStateException(
                    "File Name " + fileSystemObjectName + " results in a empty fileSystemObjectName!");
        }

        boolean wordDelimiterFound = false;

        StringBuilder preFormatedName = new StringBuilder();

        for (char c : fileSystemObjectName.toCharArray()) {

            switch (c) {
                case '_':
                    wordDelimiterFound = true;
                    break;
                case ' ':
                    wordDelimiterFound = true;
                    break;
                case '/':
                    wordDelimiterFound = true;
                case '(':
                    wordDelimiterFound = true;
                    break;
                case ')':
                    wordDelimiterFound = true;
                    break;
                default:
                    if (!wordDelimiterFound) {
                        preFormatedName.append(String.valueOf(c));

                    } else {
                        if (!String.valueOf(c).equals("_") && !String.valueOf(c).equals(" ")) {
                            preFormatedName.append(String.valueOf(c).toUpperCase());
                            wordDelimiterFound = false;
                        }

                    }

                    break;
            }
        }
        return preFormatedName.toString();
    }
}

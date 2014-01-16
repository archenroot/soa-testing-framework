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
package com.ibm.soatf.config;

import com.ibm.soatf.config.iface.IfaceExecBlock;
import com.ibm.soatf.config.iface.IfaceExecBlock.Source;
import com.ibm.soatf.config.iface.IfaceExecBlock.Target;
import com.ibm.soatf.config.iface.IfaceFlowPattern;
import com.ibm.soatf.config.iface.IfaceTestScenario;
import com.ibm.soatf.config.iface.SOATFIfaceConfig;
import com.ibm.soatf.config.iface.SOATFIfaceConfig.IfaceEndPoints.IfaceEndPoint;
import com.ibm.soatf.config.iface.db.DBConfig;
import com.ibm.soatf.config.iface.db.DBConfig.DbObjects;
import com.ibm.soatf.config.iface.db.DBConfig.DefaultDbObjects;
import com.ibm.soatf.config.iface.db.DbObject;
import com.ibm.soatf.config.iface.file.EnvSpecificFile;
import com.ibm.soatf.config.iface.file.FileConfig;
import com.ibm.soatf.config.iface.ftp.FTPConfig;
import com.ibm.soatf.config.iface.ftp.FTPConfig.DefaultFile;
import com.ibm.soatf.config.iface.soap.EnvelopeConfig;
import com.ibm.soatf.config.iface.soap.SOAPConfig;
import com.ibm.soatf.config.iface.util.UTILConfig;
import com.ibm.soatf.config.master.ExecuteOn;
import com.ibm.soatf.config.master.ExecutionBlock;
import com.ibm.soatf.config.master.Operation;
import com.ibm.soatf.tool.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Binder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;

/**
 * Class responsible to marschall/unmarschall any data included in interface 
 * configuration files.
 * 
 * @author Ladislav Jech <archenroot@gmail.com>
 */
public class InterfaceConfiguration {

    private final File INTERFACE_CONFIG_FILE;
    private final MasterConfiguration MCFG;
    private final MasterFrameworkConfig FCFG;
    private SOATFIfaceConfig XML_CONFIG;
    private Binder<Node> binder;

    InterfaceConfiguration(File ifaceConfigFile, MasterFrameworkConfig fcfg, MasterConfiguration mcfg) {
        INTERFACE_CONFIG_FILE = ifaceConfigFile;
        FCFG = fcfg;
        MCFG = mcfg;
    }

    /**
     *
     * @throws FrameworkConfigurationException
     */
    public void init() throws FrameworkConfigurationException {
        JAXBContext jaxbContext;
        Unmarshaller jaxbUnmarshaller;
        try {
            jaxbContext = JAXBContext.newInstance("com.ibm.soatf.config.iface");
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            XML_CONFIG = ((JAXBElement<SOATFIfaceConfig>) jaxbUnmarshaller.unmarshal(INTERFACE_CONFIG_FILE)).getValue();
        } catch (JAXBException jbex) {
            throw new FrameworkConfigurationException("Error while unmarshalling interface configuration object from XML file " + INTERFACE_CONFIG_FILE, jbex);
        }
    }

    /**
     *
     * @return
     * @throws com.ibm.soatf.config.InterfaceConfigurationException
     */
    public List<IfaceFlowPattern> getIfaceFlowPatterns() throws InterfaceConfigurationException {
        if (XML_CONFIG == null || XML_CONFIG.getIfaceFlowPatternConfig().getIfaceFlowPattern().isEmpty()) {
            throw new InterfaceConfigurationException("No flow patterns configuration found in interface config file '" + INTERFACE_CONFIG_FILE + "'.");
        }
        return XML_CONFIG.getIfaceFlowPatternConfig().getIfaceFlowPattern();
    }

    /**
     *
     * @param flowPatternId
     * @return
     * @throws com.ibm.soatf.config.InterfaceConfigurationException
     */
    public IfaceFlowPattern getIfaceFlowPattern(String flowPatternId) throws InterfaceConfigurationException {
        for (IfaceFlowPattern refFlowPattern : getIfaceFlowPatterns()) {
            if (refFlowPattern.getRefId().equals(flowPatternId)) {
                return refFlowPattern;
            }
        }
        throw new InterfaceConfigurationException("No flow pattern found using reference id '" + flowPatternId + "'.");
    }

    /**
     *
     * @param ifaceFlowPatternId
     * @return
     * @throws com.ibm.soatf.config.InterfaceConfigurationException
     */
    public List<IfaceTestScenario> getIfaceTestScenarios(String ifaceFlowPatternId) throws InterfaceConfigurationException {
        final List<IfaceTestScenario> ifaceTestScenarios = getIfaceFlowPattern(ifaceFlowPatternId).getIfaceTestScenario();
        if (ifaceTestScenarios.isEmpty()) {
            throw new InterfaceConfigurationException("No test scenarios found for flow pattern: " + ifaceFlowPatternId + "'.");
        }
        return ifaceTestScenarios;
    }

    /**
     *
     * @param ifaceFlowPatternId
     * @param ifaceTestScenarioId
     * @return
     * @throws com.ibm.soatf.config.InterfaceConfigurationException
     */
    public IfaceTestScenario getIfaceTestScenario(String ifaceFlowPatternId, String ifaceTestScenarioId) throws InterfaceConfigurationException {
        for (IfaceTestScenario ifaceTestScenario : getIfaceTestScenarios(ifaceFlowPatternId)) {
            if (ifaceTestScenario.getRefId().equals(ifaceTestScenarioId)) {
                return ifaceTestScenario;
            }
        }
        throw new InterfaceConfigurationException("No such test scenario found: " + ifaceTestScenarioId + " in flow pattern: " + ifaceFlowPatternId);
    }

    /**
     *
     * @param ifaceFlowPatternId
     * @param ifaceTestScenarioId
     * @return
     * @throws com.ibm.soatf.config.InterfaceConfigurationException
     */
    public List<IfaceExecBlock> getIfaceExecBlocks(String ifaceFlowPatternId, String ifaceTestScenarioId) throws InterfaceConfigurationException {
        final List<IfaceExecBlock> ifaceExecBlocks = getIfaceTestScenario(ifaceFlowPatternId, ifaceTestScenarioId).getIfaceExecBlock();
        if (ifaceExecBlocks.isEmpty()) {
            throw new InterfaceConfigurationException("No execution blocks found in test scenario: " + ifaceTestScenarioId
                    + " in flow pattern: " + ifaceFlowPatternId);
        }
        return ifaceExecBlocks;
    }

    /**
     *
     * @param ifaceFlowPatternId
     * @param ifaceTestScenarioId
     * @param ifaceExecBlockId
     * @return
     * @throws com.ibm.soatf.config.InterfaceConfigurationException
     */
    public IfaceExecBlock getIfaceExecBlock(String ifaceFlowPatternId, String ifaceTestScenarioId, String ifaceExecBlockId) throws InterfaceConfigurationException {
        for (IfaceExecBlock refExecBlock : getIfaceExecBlocks(ifaceFlowPatternId, ifaceTestScenarioId)) {
            if (refExecBlock.getRefId().equals(ifaceExecBlockId)) {
                return refExecBlock;
            }
        }
        throw new InterfaceConfigurationException("No such execution block found: " + ifaceExecBlockId + " in test scenario: " + ifaceTestScenarioId
                + " in flow pattern: " + ifaceFlowPatternId);
    }

    /**
     *
     * @param ifaceFlowPatternId
     * @param ifaceTestScenarioId
     * @param ifaceExecBlockId
     * @return
     * @throws com.ibm.soatf.config.InterfaceConfigurationException
     */
    public List<Source> getSource(String ifaceFlowPatternId, String ifaceTestScenarioId, String ifaceExecBlockId) throws InterfaceConfigurationException {
        return getIfaceExecBlock(ifaceFlowPatternId, ifaceTestScenarioId, ifaceExecBlockId).getSource();
    }

    /**
     *
     * @param ifaceFlowPatternId
     * @param ifaceTestScenarioId
     * @param ifaceExecBlockId
     * @return
     * @throws com.ibm.soatf.config.InterfaceConfigurationException
     */
    public List<Target> getTarget(String ifaceFlowPatternId, String ifaceTestScenarioId, String ifaceExecBlockId) throws InterfaceConfigurationException {
        return getIfaceExecBlock(ifaceFlowPatternId, ifaceTestScenarioId, ifaceExecBlockId).getTarget();
    }

    /**
     *
     * @param ifaceFlowPatternId
     * @param ifaceTestScenarioId
     * @param ifaceExecBlockId
     * @return
     * @throws com.ibm.soatf.config.InterfaceConfigurationException
     */
    public List<Operation> getOperations(String ifaceFlowPatternId, String ifaceTestScenarioId, String ifaceExecBlockId) throws InterfaceConfigurationException {
        try {
            ExecutionBlock executionBlock = MCFG.getExecutionBlock(ifaceFlowPatternId, ifaceTestScenarioId, ifaceExecBlockId);
            return executionBlock.getOperation();
        } catch (MasterConfigurationException ex) {
            throw new InterfaceConfigurationException(ex);
        }
    }

    /**
     *
     * @param ifaceFlowPatternId
     * @param ifaceTestScenarioId
     * @param ifaceExecBlockId
     * @param operationName
     * @return
     * @throws com.ibm.soatf.config.InterfaceConfigurationException
     */
    public Operation getOperation(String ifaceFlowPatternId, String ifaceTestScenarioId, String ifaceExecBlockId, String operationName) throws InterfaceConfigurationException {
        for (Operation operation : getOperations(ifaceFlowPatternId, ifaceTestScenarioId, ifaceExecBlockId)) {
            if (operation.getName().value().equals(operationName)) {
                return operation;
            }
        }
        throw new InterfaceConfigurationException("No such operation found within interface configuration file: " + operationName + " in test scenario: " + ifaceTestScenarioId
                + " in flow pattern: " + ifaceFlowPatternId + " in execution block: " + ifaceExecBlockId);
    }

    /**
     *
     * @return
     */
    public List<IfaceEndPoint> getIfaceEndPoints() {
        return XML_CONFIG.getIfaceEndPoints().getIfaceEndPoint();

    }

    /**
     *
     * @param endPointLocalIdRef
     * @return
     * @throws com.ibm.soatf.config.InterfaceConfigurationException
     */
    public IfaceEndPoint getIfaceEndPoint(String endPointLocalIdRef) throws InterfaceConfigurationException {
        for (IfaceEndPoint ifaceEndPoint : getIfaceEndPoints()) {
            if (ifaceEndPoint.getEndPointLocalId().equals(endPointLocalIdRef)) {
                return ifaceEndPoint;
            }
        }
        throw new InterfaceConfigurationException("Endpoint identified by '"
                + endPointLocalIdRef
                + "' was not found in configuration file '"
                + this.INTERFACE_CONFIG_FILE
                + "'.");
    }

    /**
     *
     * @param ifaceExecBlock
     * @param execOn
     * @return
     * @throws com.ibm.soatf.config.InterfaceConfigurationException
     */
    public List<IfaceEndPoint> getIfaceEndPoint(IfaceExecBlock ifaceExecBlock, ExecuteOn execOn) throws InterfaceConfigurationException {

        final List<IfaceEndPoint> ifaceEndPoints = new ArrayList<>();
        switch (execOn) {
            case SOURCE:
                for (Source source : ifaceExecBlock.getSource()) {
                    ifaceEndPoints.add(getIfaceEndPoint(source.getEndPointLocalIdRef()));
                }
                break;
            case TARGET:
                for (Target target : ifaceExecBlock.getTarget()) {
                    ifaceEndPoints.add(getIfaceEndPoint(target.getEndPointLocalIdRef()));
                }
                break;
            case NA:
                break;
            default:
                break;
        }

        if (execOn != ExecuteOn.NA && ifaceEndPoints.isEmpty()) {
            throw new InterfaceConfigurationException("The lookup for relevant endpoints for execution block='"
                    + ifaceExecBlock.getRefId() + "' resulted in empty list. Please review the configuration file: "
                    + this.INTERFACE_CONFIG_FILE + ".");
        }
        return ifaceEndPoints;

    }

    /**
     *
     * @param envName
     * @param ifaceExecBlock
     * @param execOn
     * @return
     * @throws InterfaceConfigurationException
     */
    public List<DbObject> getIfaceDbObjectList(String envName, IfaceExecBlock ifaceExecBlock, ExecuteOn execOn) throws InterfaceConfigurationException {
        // Variables init
        List<IfaceEndPoint> ifaceEndPoints = getIfaceEndPoint(ifaceExecBlock, execOn);
        DBConfig dbConfig = null;
        DbObjects dbObjects = null;
        String[] envRefNames = envName.split("|");

        // Return object
        List<DbObject> dbObjectList = new ArrayList<>();

        // Get the database endpoint object
        for (IfaceEndPoint ifep : ifaceEndPoints) {
            if (ifep.getDatabase() != null) {
                dbConfig = ifep.getDatabase();
            }
        }

        // Error raised if endpoint object null
        if (dbConfig == null) {
            String msg = "Cannot find instance of dbConfig object for following input variables:" +
                    "\nenvironment name: " +envName +
                    ",\ninterface exectuion block: " + ifaceExecBlock.getRefId() +
                    ",\ntargeting(execute on): " + execOn.value();
            throw new InterfaceConfigurationException(msg);
        }

        // Get default db objects
        DefaultDbObjects defaultDbObjects = dbConfig.getDefaultDbObjects();
        // Get environment db objects list
        List<DBConfig.DbObjects> dbObjectsList = dbConfig.getDbObjects();

        // Seek db objects for selected environment
        boolean dbObjectsExists = false;
        for (DbObjects dbos : dbObjectsList) {
            for (String envRefName : envRefNames) {
                if (dbos.getEnvRefName().equals(envRefName)) {
                    // ATTENTION: This will seek only first occurency, if multiple environment data source exists due to wrong 
                    // iface configuration, those will not be picked up. Should be added to configuration sanity check process.
                    dbObjectsExists = true;
                    for (DbObject dbo : dbos.getDbObject()) {
                        // Return object
                        return dbos.getDbObject();
                    }
                    break;
                }
            }
        }

        // Execption if no dbObjects found for selected environment and no defaultDbObject exists
        if (!dbObjectsExists && defaultDbObjects == null) {
            String msg = "TODO";
            throw new InterfaceConfigurationException(msg);
        } else {
            // Return default object
            return defaultDbObjects.getDbObject();
        }
    }

    /**
     *
     * @param envName
     * @param ifaceExecBlock
     * @param execOn
     * @return
     * @throws com.ibm.soatf.config.InterfaceConfigurationException
     */
    public FTPConfig.File getIfaceFtpFile(String envName, IfaceExecBlock ifaceExecBlock, ExecuteOn execOn) throws InterfaceConfigurationException {
        // Variables init
        List<IfaceEndPoint> ifaceEndPoints = getIfaceEndPoint(ifaceExecBlock, execOn);
        FTPConfig ftpConfig = null;

        String[] envRefNames = envName.split("|");

        // Return object
        FTPConfig.File File = null;

        // Get the database endpoint object
        for (IfaceEndPoint ifep : ifaceEndPoints) {
            if (ifep.getFtpServer() != null) {
                ftpConfig = ifep.getFtpServer();
            }
        }

        // Error raised if endpoint object null
        if (ftpConfig == null) {
            String msg = "TODO";
            throw new InterfaceConfigurationException(msg);
        }

        // Get default db objects
        DefaultFile defaultFile = ftpConfig.getDefaultFile();
        // Get environment db objects list
        List<FTPConfig.File> ftpFileList = ftpConfig.getFile();

        // Seek db objects for selected environment
        boolean ftpFileExists = false;
        for (FTPConfig.File ftpFile : ftpFileList) {
            for (String envRefName : envRefNames) {
                if (ftpFile.getEnvRefName().equals(envRefName)) {
                    // ATTENTION: This will seek only first occurency, if multiple environment data source exists due to wrong 
                    // iface configuration, those will not be picked up. Should be added to configuration sanity check process.
                    ftpFileExists = true;
                    return ftpFile;
                }
                break;
            }
        }

        // Execption if no dbObjects found for selected environment and no defaultDbObject exists
        if (!ftpFileExists && defaultFile
                == null) {
            String msg = "TODO";
            throw new InterfaceConfigurationException(msg);
        } else {
            // Return default object
            FTPConfig.File file = new FTPConfig.File();
            file.setFileName(defaultFile.getFileName());
            file.setFileContent(defaultFile.getFileContent());
            return file;
        }
    }
    
    /**
     *
     * @param interfaceExecutionBlock
     * @param execOn
     * @return
     * @throws com.ibm.soatf.config.InterfaceConfigurationException
     */
    public SOAPConfig getSoapConfig(IfaceExecBlock interfaceExecutionBlock, ExecuteOn execOn) throws InterfaceConfigurationException {
        for (IfaceEndPoint ifaceEndPoint : getIfaceEndPoint(interfaceExecutionBlock, execOn)) {
            if (ifaceEndPoint.getSoap() != null) {
                return ifaceEndPoint.getSoap();
            }
        }
        throw new InterfaceConfigurationException("No SOAP configuration found for execution block: " + interfaceExecutionBlock.getRefId());
    }
    
    /**
     *
     * @param envName
     * @param ifaceExecBlock
     * @param execOn
     * @return
     * @throws com.ibm.soatf.config.InterfaceConfigurationException
     */
    public List<EnvelopeConfig.Element> getSoapEnvelopeElements(String envName, IfaceExecBlock ifaceExecBlock, ExecuteOn execOn) throws InterfaceConfigurationException {
        List<EnvelopeConfig.Element> list = null;
        SOAPConfig soapCfg = getSoapConfig(ifaceExecBlock, execOn);
        
        List<SOAPConfig.EnvelopeConfig> envelopeConfigs = soapCfg.getEnvelopeConfig(); //specific for environments
        if (!Utils.isEmpty(envelopeConfigs)) {
            all: for (SOAPConfig.EnvelopeConfig envelopeConfig : envelopeConfigs) {
                String[] envRefNames = envelopeConfig.getEnvRefName().split("\\|");
                for (String envRefName : envRefNames) {
                    if (envRefName.equalsIgnoreCase(envName)) {
                        list = envelopeConfig.getElement();
                        break all;
                    }
                }
            }
        } else {
            SOAPConfig.DefaultEnvelopeConfig defaultEnvelopeConfig = soapCfg.getDefaultEnvelopeConfig();
            if (defaultEnvelopeConfig == null) {
                String msg = "Either 'envelopeConfig' or 'defaultEnvelopeConfig' element must be defined.";
                throw new InterfaceConfigurationException(msg);
            }
            list = defaultEnvelopeConfig.getElement();
        }
        
        if (Utils.isEmpty(list)) {
            String msg = "No elements defined in either 'envelopeConfig' or 'defaultEnvelopeConfig' elements.";
            throw new InterfaceConfigurationException(msg);
        }
        
        return list;
    }
    
    /**
     *
     * @param interfaceExecutionBlock
     * @param execOn
     * @return
     * @throws com.ibm.soatf.config.InterfaceConfigurationException
     */
    public FileConfig getFileConfig(IfaceExecBlock interfaceExecutionBlock, ExecuteOn execOn) throws InterfaceConfigurationException {
        FileConfig fileConfig = null;
        for (IfaceEndPoint ifaceEndPoint : getIfaceEndPoint(interfaceExecutionBlock, execOn)) {
            if (ifaceEndPoint.getFileConfig() != null) {
                return ifaceEndPoint.getFileConfig();
            }
        }
        throw new InterfaceConfigurationException("No FILE configuration found for execution block: " + interfaceExecutionBlock.getRefId());
    }
    
    /**
     *
     * @param envName
     * @param ifaceExecBlock
     * @param execOn
     * @return
     * @throws com.ibm.soatf.config.InterfaceConfigurationException
     */
    public com.ibm.soatf.config.iface.file.File getFile(String envName, IfaceExecBlock ifaceExecBlock, ExecuteOn execOn) throws InterfaceConfigurationException {
        com.ibm.soatf.config.iface.file.File file = null;
        FileConfig fileCfg = getFileConfig(ifaceExecBlock, execOn);
        
        List<EnvSpecificFile> files = fileCfg.getEnvSpecificFile(); //specific for environments
        if (!Utils.isEmpty(files)) {
            all: for (EnvSpecificFile envSpecificFile : files) {
                String[] envRefNames = envSpecificFile.getEnvRefName().split("\\|");
                for (String envRefName : envRefNames) {
                    if (envRefName.equalsIgnoreCase(envName)) {
                        file = envSpecificFile.getFile();
                        break all;
                    }
                }
            }
        } else {
            com.ibm.soatf.config.iface.file.DefaultFile defaultFile = fileCfg.getDefaultFile();
            if (defaultFile == null) {
                String msg = "Either 'defaultFile' or 'envSpecificFile' element must be defined.";
                throw new InterfaceConfigurationException(msg);
            }
            file = defaultFile.getFile();
        }
        
        if (Utils.isEmpty(file)) {
            String msg = "No elements defined in either 'defaultFile' or 'envSpecificFile' elements.";
            throw new InterfaceConfigurationException(msg);
        }
        
        return file;
    }

    /**
     *
     * @return
     */
    public UTILConfig getUtilConfig() {
        return XML_CONFIG.getUtilConfig();
    }

    /**
     *
     * @param interfaceId
     * @param interfaceFlowPattern
     * @param ifaceTestScenarioId
     * @param componentDirName
     * @return
     * @throws com.ibm.soatf.config.InterfaceConfigurationException
     */
    public File getComponentWorkingDir(String interfaceId, IfaceFlowPattern interfaceFlowPattern, String ifaceTestScenarioId, String componentDirName) throws InterfaceConfigurationException {
        File dir = getTestScenarioWorkingDir(interfaceId, interfaceFlowPattern, ifaceTestScenarioId);
        if (componentDirName != null) {
            dir = new File(dir, componentDirName);
        }
        return dir;
    }

    /**
     *
     * @param interfaceId
     * @param interfaceFlowPattern
     * @param ifaceTestScenarioId
     * @return
     * @throws com.ibm.soatf.config.InterfaceConfigurationException
     */
    public File getTestScenarioWorkingDir(String interfaceId, IfaceFlowPattern interfaceFlowPattern, String ifaceTestScenarioId) throws InterfaceConfigurationException {
        try {
            File dir = new File(FCFG.getSoaTestHome(), interfaceId + "_" + FCFG.getValidFileSystemObjectName(MCFG.getInterface(interfaceId).getDescription()));
            dir = new File(dir, MasterFrameworkConfig.FLOW_PATTERN_DIR_NAME_PREFIX + FCFG.getValidFileSystemObjectName(interfaceFlowPattern.getRefId()));
            dir = new File(dir, FCFG.getValidFileSystemObjectName(interfaceFlowPattern.getInstanceMetadata().getTestName()));
            dir = new File(dir, FCFG.getValidFileSystemObjectName(ifaceTestScenarioId));
            return dir;
        } catch (FrameworkConfigurationException ex) {
            final String msg = "TODO";
            throw new InterfaceConfigurationException(msg, ex);
        }
    }
    
    /**
     *
     * @param <T>
     * @param jaxbNode
     * @param c
     * @return
     */
    public <T> T getParent(Object jaxbNode, Class<T> c) {
        Node xmlNode = binder.getXMLNode(jaxbNode);
        if (xmlNode == null) {
            return null;
        }
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        Object retVal = null;
        try {
            Node parent = (Node) xpath.evaluate("..", xmlNode, XPathConstants.NODE);
            retVal = binder.getJAXBNode(parent);
            return c.cast(retVal);
        } catch (XPathExpressionException ex) {
            String msg = "Error during XPath query for the JAXB node's parent";
            System.out.println(msg);
        } catch (ClassCastException ex) {
            String type = retVal == null ? "?" : retVal.getClass().toString();
            String msg = "Cannot cast parent of type " + type + " to the expected type of " + c.getClass();
            System.out.println(msg);
        }
        return null;
    }
}

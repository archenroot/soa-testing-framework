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

import static com.ibm.soatf.config.MasterFrameworkConfig.IFACE_CONFIG_FILENAME;
import static com.ibm.soatf.config.MasterFrameworkConfig.SOATF_MASTER_CONFIG_FILENAME;
import com.ibm.soatf.config.iface.IfaceExecBlock;
import com.ibm.soatf.config.iface.IfaceFlowPattern;
import com.ibm.soatf.config.iface.IfaceTestScenario;
import com.ibm.soatf.config.iface.SOATFIfaceConfig;
import com.ibm.soatf.config.iface.db.DBConfig;
import com.ibm.soatf.config.iface.file.EnvSpecificFile;
import com.ibm.soatf.config.iface.file.FileConfig;
import com.ibm.soatf.config.iface.ftp.FTPConfig;
import com.ibm.soatf.config.master.Databases;
import com.ibm.soatf.config.master.Environments;
import com.ibm.soatf.config.master.ExecBlockOperation;
import com.ibm.soatf.config.master.FTPServers;
import com.ibm.soatf.config.master.FlowPattern;
import com.ibm.soatf.config.master.Interface;
import com.ibm.soatf.config.master.OSBReporting;
import com.ibm.soatf.config.master.OracleFusionMiddleware;
import com.ibm.soatf.config.master.SOATestingFrameworkMasterConfiguration;
import com.ibm.soatf.config.master.TestScenario;
import com.ibm.soatf.tool.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Responsible for configuration initialization process.
 *
 * @author Ladislav Jech <archenroot@gmail.com>
 */
final public class ConfigurationManager {

    private static final Logger logger = LogManager.getLogger(ConfigurationManager.class.getName());
    private static final ConfigurationManager instance = new ConfigurationManager();

    private boolean initialized = false;

    private MasterFrameworkConfig frameworkConfig;
    private MasterConfiguration mcfg;

    private ConfigurationManager() {
    }

    /**
     * Returns instance of class.
     *
     * @return instance of ConfigurationManager class.
     */
    public static ConfigurationManager getInstance() {
        return instance;
    }

    /**
     * Instantiate <code>MasterFrameworkConfig.java</code> and
     * called <code>init()</code> method of <code>MasterFrameworkConfig.java</code> 
     * and pass <code>MasterFrameworkConfig.java</code> reference to
     * <code>MasterConfiguration.java</code> and calls <code>MasterConfiguration.init()</code>
     * for more reference check individual <code>init()</code> method
     * @throws FrameworkConfigurationException
     */
    public void init() throws FrameworkConfigurationException {
        logger.debug("Initializing configuration manager...");
        frameworkConfig = new MasterFrameworkConfig();
        frameworkConfig.init();
        mcfg = new MasterConfiguration(frameworkConfig);
        mcfg.init();        
        initialized = true;
    }

    /**
     *
     * @return
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     *
     * @return
     */
    public MasterFrameworkConfig getFrameworkConfig() {
        checkInit();
        return frameworkConfig;
    }

    /**
     *
     * @return
     */
    public MasterConfiguration getMasterConfig() {
        checkInit();
        return mcfg;
    }

    /**
     *
     * @param interfaceName
     * @return
     * @throws FrameworkConfigurationException
     */
    public InterfaceConfiguration getInterfaceConfig(String interfaceName) throws FrameworkConfigurationException {
        checkInit();
        return mcfg.getInterfaceConfig(interfaceName);
    }

    private void checkInit() {
        if (!initialized) {
            throw new IllegalStateException("ConfigurationManager not initialized. ConfigurationManager.getInstance().init() must be called before accessing any of the configurations.");
        }
    }
    
    public ConfigConsistencyResult checkConfigConsistency() throws FrameworkConfigurationException {
        if (!initialized) {
            throw new FrameworkConfigurationException("ConfigurationManager not initialized. ConfigurationManager.getInstance().init() must be called before checking configuration for consistency.");
        }
        ConfigConsistencyResult ccr = new ConfigConsistencyResult();
        //check and initialize environments stuff (for further use)
        Environments environments = mcfg.getXmlConfig().getEnvironments();
        //instances and their environments
        Map<String, Set<String>> mEnvironmentsMap = new HashMap<>();
        logger.info("Getting database environments info...");
        if(Utils.isEmpty(environments.getDatabases())) {
            ccr.addMasterError("There are no databases configured in %s", SOATF_MASTER_CONFIG_FILENAME);
        } else {
            List<Databases.Database> databases = environments.getDatabases().getDatabase();
            for (Databases.Database database : databases) {
                String identificator = database.getIdentificator(); //such es "eis/ebs/DbAdapter"
                Set<String> mDbEnvs = new HashSet<>();
                for (Databases.Database.DatabaseInstance databaseInstance : database.getDatabaseInstance()) {
                    String env = databaseInstance.getEnvironment();
                    if(mDbEnvs.contains(env)) {
                        ccr.addMasterError("Database instance \"%s\" refers to an environment \"%s\" which was already defined in one of the previous instances", identificator, env);
                    }
                    mDbEnvs.add(env);
                }
                mEnvironmentsMap.put(identificator, mDbEnvs);
            }
        }
        logger.info("Getting FTP servers environments info...");
        if(Utils.isEmpty(environments.getFtpServers())) {
            ccr.addMasterError("There are no FTP servers configured in %s", SOATF_MASTER_CONFIG_FILENAME);
        } else {
            List<FTPServers.FtpServer> ftpServers = environments.getFtpServers().getFtpServer();
            for (FTPServers.FtpServer ftpServer : ftpServers) {
                String identificator = ftpServer.getIdentificator(); //such es "SALMON to GL FTP"
                Set<String> mFtpEnvs = new HashSet<>();
                for (FTPServers.FtpServer.FtpServerInstance ftpInstance : ftpServer.getFtpServerInstance()) {
                    String env = ftpInstance.getEnvironment();
                    if(mFtpEnvs.contains(env)) {
                        ccr.addMasterError("FTP server instance \"%s\" refers to an environment \"%s\" which was already defined in one of the previous instances", identificator, env);
                    }
                    mFtpEnvs.add(env);
                }
                mEnvironmentsMap.put(identificator, mFtpEnvs);
            }
        }
        logger.info("Getting OFM environments info...");
        if(Utils.isEmpty(environments.getOracleFusionMiddleware())) {
            ccr.addMasterError("There are no fusion middleware instances configured in %s", SOATF_MASTER_CONFIG_FILENAME);
        } else {
            Set<String> mOfmEnvs = new HashSet<>();
            int count = 1;
            for (OracleFusionMiddleware.OracleFusionMiddlewareInstance ofmInstance : environments.getOracleFusionMiddleware().getOracleFusionMiddlewareInstance()) {
                String env = ofmInstance.getEnvironment();
                if(mOfmEnvs.contains(env)) {
                    ccr.addMasterWarning("Oracle Fusion Middleware instance (No. %s) refers to an environment \"%s\" which was already defined in one of the previous instances", count, env);
                }
                mOfmEnvs.add(env);
                count++;
            }
            mEnvironmentsMap.put("ofmInst", mOfmEnvs);
        }
        logger.info("Getting OSB reporting environments info...");
        if(Utils.isEmpty(environments.getOsbDatabaseReporting())) {
            ccr.addMasterError("There is no OSB reporting configured in %s", SOATF_MASTER_CONFIG_FILENAME);
        } else {
            Set<String> mOSBReportingEnvs = new HashSet<>();
            int count = 1;
            for (OSBReporting.OsbReportingInstance osbReportingInst : environments.getOsbDatabaseReporting().getOsbReportingInstance()) {
                String env = osbReportingInst.getEnvironment();
                if(mOSBReportingEnvs.contains(env)) {
                    ccr.addMasterWarning("OSB Reporting instance (No. %s) refers to an environment \"%s\" which was already defined in one of the previous instances", count, env);
                }
                mOSBReportingEnvs.add(env);
                count++;
            }
            mEnvironmentsMap.put("osbInst", mOSBReportingEnvs);
        }
        
        //stores all defined flow patterns and their subtree structure all the way down to the operation in exec block
        logger.info("Building master config flow patterns tree...");
        LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, ExecBlockOperation>>>> masterFPMap = buildFlowPatternMap(ccr);
        
        //interfaces
        List<SOATestingFrameworkMasterConfiguration.Interfaces.Interface> interfaces = mcfg.getInterfaces();
        for (SOATestingFrameworkMasterConfiguration.Interfaces.Interface iface : interfaces) {
            final String iName = iface.getName();
            final String iDesc = iface.getDescription();
            logger.info(String.format("Checking interface %s - %s...", iName, iDesc));
            //Projects
            if(Utils.isEmpty(iface.getProjects())) {
                ccr.addMasterWarning("%s - %s: No projects defined", iName, iDesc);
            }
            //referenced flow patterns
            Interface.Patterns patterns = iface.getPatterns();
            HashMap<String, Integer> masterOccurenceMap = new HashMap<>();
            if(Utils.isEmpty(patterns)) {
                ccr.addMasterWarning("%s - %s: No patterns defined", iName, iDesc);
            } else {
                for (Interface.Patterns.ReferencedFlowPattern refPattern : patterns.getReferencedFlowPattern()) {
                    final String refFlowPatternId = refPattern.getIdentificator();
                    if(!masterFPMap.containsKey(refFlowPatternId)) {
                        ccr.addMasterWarning("%s - %s: Referenced flow pattern \"%s\" was not found in the list of defined patterns", iName, iDesc, refFlowPatternId);
                    }
                    masterOccurenceMap.put(refFlowPatternId, refPattern.getOccurrence());
                }
            }
            //config.xml file exists, not directory
            boolean cfgFileExists;
            try {
                File ifaceConfigFile = mcfg.getIfaceConfigFile(iface);
                cfgFileExists = ifaceConfigFile.exists();
                if(!cfgFileExists) {
                    ccr.addInterfaceWarning("%s - %s: Missing %s", iName, iDesc, IFACE_CONFIG_FILENAME);
                    continue;
                } else if (ifaceConfigFile.isDirectory()) {
                    ccr.addInterfaceError("%s - %s: %s appears to be a directory", iName, iDesc, IFACE_CONFIG_FILENAME);
                    continue;
                }
            } catch (MasterConfigurationException e) {
                ccr.addInterfaceWarning("%s - %s: Could not construct path to the %s file. Either the interface description attribute is empty or results in an empty string after normalizing", iName, iDesc, IFACE_CONFIG_FILENAME);
                continue;
            }
            
            //from DOM/JAXB perspective, is config.xml content valid?
            InterfaceConfiguration icfg;
            try {
                icfg = mcfg.getInterfaceConfig(iface);
            } catch (MasterConfigurationException e) {
                ccr.addInterfaceWarning("%s - %s: Could not construct path to the %s file. Either the interface description attribute is empty or results in an empty string after normalizing. Normalizing here means removing any string that matches %s regex pattern", iName, iDesc, IFACE_CONFIG_FILENAME, MasterFrameworkConfig.FS_VALIDATION_PATTERN);
                continue;
            } catch (InterfaceConfigurationException e) {
                ccr.addInterfaceWarning("%s - %s: Could not load %s file. Error message: %s", iName, iDesc, IFACE_CONFIG_FILENAME, e.getMessage());
                continue;
            }
                
            //********************************//
            //     INTERFACE FLOW PATTERNS    //
            //********************************//
            List<IfaceFlowPattern> ifaceFlowPatterns = icfg.getIfaceFlowPatterns();
            
            //is occurence defined in master-config.xml correctly followed in config.xml?
            HashMap<String, Integer> occurenceMap = new HashMap<>();
            for (IfaceFlowPattern ifp : ifaceFlowPatterns) {
                final String ifpId = ifp.getRefId();
                Integer count = occurenceMap.get(ifpId);
                if(count == null) {
                    occurenceMap.put(ifpId, 1);
                } else {
                    occurenceMap.put(ifpId, count + 1);
                }
            }
            for(String mFPId : masterOccurenceMap.keySet()) {
                Integer masterCount = masterOccurenceMap.get(mFPId);
                Integer interfaceCount = occurenceMap.get(mFPId) == null ? 0 : occurenceMap.get(mFPId);
                if (!masterCount.equals(interfaceCount)) {
                    ccr.addInterfaceError("%s - %s: \"%s\" flow pattern with occurence %s in %s occurs %s time(s) in %s", iName, iDesc, mFPId, masterCount, SOATF_MASTER_CONFIG_FILENAME, interfaceCount, IFACE_CONFIG_FILENAME);
                }
            }
            
            for (IfaceFlowPattern iFP : ifaceFlowPatterns) {
                final String iFPId = iFP.getRefId();
                LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, ExecBlockOperation>>> mTestScenarios = masterFPMap.get(iFPId);
                if(mTestScenarios == null) {
                    ccr.addInterfaceError("%s - %s: \"%s\" flow pattern not defined in %s", iName, iDesc, iFPId, SOATF_MASTER_CONFIG_FILENAME);
                } else {
                    ConfigConsistencyResult tempCcr = compareFlowPatterns(iName, iDesc, iFP, mTestScenarios);
                    ccr.addAllMessages(tempCcr);
                    List<IfaceTestScenario> ifaceTestScenarios = iFP.getIfaceTestScenario();
                    for (IfaceTestScenario iTS : ifaceTestScenarios) {
                        String iTSId = iTS.getRefId();
                        LinkedHashMap<String, LinkedHashMap<String, ExecBlockOperation>> mExecBlocks = mTestScenarios.get(iTSId);
                        if(mExecBlocks == null) {
                            ccr.addInterfaceError("%s - %s: \"%s\" test scenario not defined in %s for flow pattern \"%s\"", iName, iDesc, iTSId, SOATF_MASTER_CONFIG_FILENAME, iFPId);
                        } else {
                            tempCcr = compareExecutionBlocks(iName, iDesc, iFP, iTS, mExecBlocks);
                            ccr.addAllMessages(tempCcr);
                            List<IfaceExecBlock> ifaceExecBlocks = iTS.getIfaceExecBlock();
                            for (IfaceExecBlock iEB : ifaceExecBlocks) {
                                String iEBId = iEB.getRefId();
                                LinkedHashMap<String, ExecBlockOperation> mOperations = mExecBlocks.get(iEBId);
                                if(mOperations == null) {
                                    ccr.addInterfaceError("%s - %s: \"%s\" execution block not defined in %s in flow pattern \"%s\" and test scenario \"%s\"", iName, iDesc, iEBId, SOATF_MASTER_CONFIG_FILENAME, iFPId, iTSId);
                                } else {
                                    //i don't know right now, maybe something related to the source/target and type of the operation
                                    //maybe check if endpoint of the proper type is defined for the operation
                                }
                                //check whether referenced endpoints from execution blocks are defined in ifaceEndPoints
                                ccr.addAllMessages(checkEndpointsInInterfaceConfig(iName, iDesc, iFP.getRefId(), iTS.getRefId(), iEB, icfg.getIfaceEndPoints()));
                            }
                        }
                    }
                }
                //check metadata for report
                ccr.addAllMessages(checkInstanceMetadata(iName, iDesc, iFP));
            }
            
            List<SOATFIfaceConfig.IfaceEndPoints.IfaceEndPoint> ifaceEndPoints = icfg.getIfaceEndPoints();
            for (SOATFIfaceConfig.IfaceEndPoints.IfaceEndPoint ifaceEndPoint : ifaceEndPoints) {
                ccr.addAllMessages(checkEndpointsAgainstMaster(iName, iDesc, ifaceEndPoint, mEnvironmentsMap));
            }
        }
        return ccr;
    }

    /**
     * FlowPattern
     *   |- TestScenario
     *        |- ExecBlock
     *             |- Operation
     * @param ccr
     * @return 
     */
    private LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, ExecBlockOperation>>>> buildFlowPatternMap(ConfigConsistencyResult ccr) {
        //flow patterns in master-config.xml, check for duplicity
        LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, ExecBlockOperation>>>> fpMap = new LinkedHashMap<>();
        try {
            for (FlowPattern flowPattern : mcfg.getFlowPatterns()) {
                final String fpId = flowPattern.getIdentificator();
                if(fpMap.containsKey(fpId)) {
                    ccr.addMasterError("Found multiple definitions of the \"%s\" flow pattern", fpId);
                } else {
                    final LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, ExecBlockOperation>>> tsMap = new LinkedHashMap<>();
                    fpMap.put(fpId, tsMap);
                    for (TestScenario ts : flowPattern.getTestScenario()) {
                        final String tsId = ts.getIdentificator();
                        if(tsMap.containsKey(tsId)) {
                            ccr.addMasterError("Found multiple definitions of the \"%s\" test scenario in \"%s\"", tsId, fpId);
                        } else {
                            final LinkedHashMap<String, LinkedHashMap<String, ExecBlockOperation>> ebMap = new LinkedHashMap<>();
                            tsMap.put(tsId, ebMap);
                            for (TestScenario.ExecutionBlock eb : ts.getExecutionBlock()) {
                                final String ebId = eb.getIdentificator();
                                if (ebMap.containsKey(ebId)) {
                                    ccr.addMasterError("Found multiple definitions of the \"%s\" exec block in flow pattern \"%s\" and test scenario \"%s\"", ebId, fpId, tsId);
                                } else {
                                    final LinkedHashMap<String, ExecBlockOperation> opMap = new LinkedHashMap<>();
                                    ebMap.put(ebId, opMap);
                                    for (ExecBlockOperation op : eb.getOperation()) {
                                        opMap.put(op.getName().toString(), op);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (MasterConfigurationException e) {
            ccr.addMasterWarning("Flow patterns: no flow patterns defined.");
        }
        return fpMap;
    }

    private ConfigConsistencyResult compareFlowPatterns(String iName, String iDesc, IfaceFlowPattern iFP, LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, ExecBlockOperation>>> mTestScenarios) {
        ConfigConsistencyResult ccr = new ConfigConsistencyResult();
        String iFPId = iFP.getRefId();
        List<IfaceTestScenario> iTestScenarios = iFP.getIfaceTestScenario();
        Set<String> mTSIdSet = mTestScenarios.keySet();
        LinkedHashSet<String> iTSIdSet = new LinkedHashSet<>();
        for (IfaceTestScenario iTS : iTestScenarios) {
            iTSIdSet.add(iTS.getRefId());
        }
        //check for orphans in master vs. interface
        for (String mTSId : mTSIdSet) {
            if(!iTSIdSet.contains(mTSId)) {
                ccr.addInterfaceWarning("%s - %s: \"%s\" test scenario from \"%s\" flow pattern not defined in %s", iName, iDesc, mTSId, iFPId, IFACE_CONFIG_FILENAME);
            }
        }
        for (String iTSId : iTSIdSet) {
            if(!mTSIdSet.contains(iTSId)) {
                ccr.addInterfaceError("%s - %s: \"%s\" test scenario from \"%s\" flow pattern not defined in %s", iName, iDesc, iTSId, iFPId, SOATF_MASTER_CONFIG_FILENAME);
            }
        }
        return ccr;
    }

    private ConfigConsistencyResult compareExecutionBlocks(String iName, String iDesc, IfaceFlowPattern iFP, IfaceTestScenario iTS, LinkedHashMap<String, LinkedHashMap<String, ExecBlockOperation>> mExecBlocks) {
        ConfigConsistencyResult ccr = new ConfigConsistencyResult();
        String iFPId = iFP.getRefId();
        String iTSId = iTS.getRefId();
        List<IfaceExecBlock> iExecBlocks = iTS.getIfaceExecBlock();
        Set<String> mEBIdSet = mExecBlocks.keySet();
        LinkedHashSet<String> iEBIdSet = new LinkedHashSet<>();
        for (IfaceExecBlock iEB : iExecBlocks) {
            iEBIdSet.add(iEB.getRefId());
        }
        //check for orphans in master vs. interface
        for (String mEBId : mEBIdSet) {
            if(!iEBIdSet.contains(mEBId)) {
                ccr.addInterfaceWarning("%s - %s: \"%s\" execution block not defined in %s in flow pattern \"%s\" and test scenario \"%s\"", iName, iDesc, mEBId, IFACE_CONFIG_FILENAME, iFPId, iTSId);
            }
        }
        //check for orphans in interface vs. master
        for (String iEBId : iEBIdSet) {
            if(!mEBIdSet.contains(iEBId)) {
                ccr.addInterfaceError("%s - %s: \"%s\" execution block not defined in %s in flow pattern \"%s\" and test scenario \"%s\"", iName, iDesc, iEBId, SOATF_MASTER_CONFIG_FILENAME, iFPId, iTSId);
            }
        }
        
        //check the correct order of the exec blocks in interface config - has to be in the same order as it appears in master-config
        List<String> mEBIdList = new ArrayList<>(mEBIdSet);
        List<String> iEBIdList = new ArrayList<>(iEBIdSet);
        for (int i = 0; i < mEBIdList.size() && i < iEBIdList.size(); i++) {
            String mId = mEBIdList.get(i);
            String iId = iEBIdList.get(i);
            if (!mId.equals(iId)) {
                ccr.addInterfaceError("%s - %s: \"%s\" execution block not found at expected position (%s) in %s in flow pattern \"%s\" and test scenario \"%s\"", iName, iDesc, mId, i, IFACE_CONFIG_FILENAME, iFPId, iTSId);
            }
        }
        return ccr;
    }

    private ConfigConsistencyResult checkInstanceMetadata(String iName, String iDesc, IfaceFlowPattern iFP) {
        IfaceFlowPattern.InstanceMetadata instanceMetadata = iFP.getInstanceMetadata();
        ConfigConsistencyResult ccr = new ConfigConsistencyResult();
        if(Utils.isEmpty(instanceMetadata)) {
            ccr.addInterfaceError("%s - %s: Flow pattern \"%s\" is missing metadata part", iName, iDesc, iFP.getRefId());
        } else if(Utils.isEmpty(instanceMetadata.getTestName())) {
            ccr.addInterfaceError("%s - %s: Flow pattern \"%s\" is missing a test name inside the metadata part", iName, iDesc, iFP.getRefId());
        }
        return ccr;
    }

    private ConfigConsistencyResult checkEndpointsInInterfaceConfig(String iName, String iDesc, String iFPId, String iTSId, IfaceExecBlock iEB, List<SOATFIfaceConfig.IfaceEndPoints.IfaceEndPoint> ifaceEndPoints) {
        ConfigConsistencyResult ccr = new ConfigConsistencyResult();
        Set<String> iEPIdSet = new HashSet<>();
        for (SOATFIfaceConfig.IfaceEndPoints.IfaceEndPoint iEP : ifaceEndPoints) {
            iEPIdSet.add(iEP.getEndPointLocalId());
        }
        String iEBId = iEB.getRefId();
        List<IfaceExecBlock.Source> sources = iEB.getSource();
        for (IfaceExecBlock.Source source : sources) {
            String iEBEndpointId = source.getEndPointLocalIdRef();
            if(!iEPIdSet.contains(iEBEndpointId)) {
                ccr.addInterfaceError("%s - %s: Non-existing endpoint \"%s\" referenced in \"%s\" / \"%s\" / \"%s\" execution block", iName, iDesc, iEBEndpointId, iFPId, iTSId, iEBId);
            }
        }
        List<IfaceExecBlock.Target> targets = iEB.getTarget();
        for (IfaceExecBlock.Target target : targets) {
            String iEBEndpointId = target.getEndPointLocalIdRef();
            if(!iEPIdSet.contains(iEBEndpointId)) {
                ccr.addInterfaceError("%s - %s: Non-existing endpoint \"%s\" referenced in \"%s\" / \"%s\" / \"%s\" execution block", iName, iDesc, iEBEndpointId, iFPId, iTSId, iEBId);
            }
        }
        return ccr;
    }

    private ConfigConsistencyResult checkEndpointsAgainstMaster(String iName, String iDesc, SOATFIfaceConfig.IfaceEndPoints.IfaceEndPoint iEP, Map<String, Set<String>> mEnvironmentsMap) {
        //1. if particular endpoint is missing default definition of its object, check if environment-specific
        //   objects cover all environments defined in master-config
        //2. check whether endpoints in ifaceEndPoints exist in master config
        String delimiter = Utils.escapeRegexChars(mcfg.getXmlConfig().getFrameworkGlobalConfiguration().getConstants().getCommonListDelimiter());
        ConfigConsistencyResult ccr = new ConfigConsistencyResult();
        Set<String> iEnvsSet = new HashSet<>();
        final String endPointLocalId = iEP.getEndPointLocalId();
        if (!Utils.isEmpty(iEP.getDatabase())) {
            DBConfig database = iEP.getDatabase();
            String refId = database.getRefId();
            Set<String> mDbEnvs = mEnvironmentsMap.get(refId) == null ? new HashSet<String>() : mEnvironmentsMap.get(refId);
            if(Utils.isEmpty(mDbEnvs)) {
                ccr.addInterfaceError("%s - %s: Referencing \"%s\" database, but it is not defined in %s", iName, iDesc, refId, SOATF_MASTER_CONFIG_FILENAME);
            }
            if(!Utils.isEmpty(database.getDefaultDbObjects())) { 
                //we have all envs covered
            } else if (!Utils.isEmpty(database.getDbObjects())) {
                List<DBConfig.DbObjects> dbObjects = database.getDbObjects();
                for (DBConfig.DbObjects dbObject : dbObjects) {
                    String[] envs = dbObject.getEnvRefName().split(delimiter);
                    for (String env : envs) {
                        if(iEnvsSet.contains(env)) {
                            ccr.addInterfaceWarning("%s - %s: Multiple references of \"%s\" environment in database endpoint \"%s\"", iName, iDesc, env, endPointLocalId);
                        }
                        iEnvsSet.add(env);
                    }
                }
                for (String iEnv : iEnvsSet) {
                    if(!mDbEnvs.contains(iEnv)) {
                        ccr.addInterfaceWarning("%s - %s: Referencing \"%s\" environment in database endpoint \"%s\", but it is note defined in %s", iName, iDesc, iEnv, endPointLocalId, SOATF_MASTER_CONFIG_FILENAME);
                    }
                }
                for (String mEnv : mDbEnvs) {
                    if(!iEnvsSet.contains(mEnv)) {
                        ccr.addInterfaceError("%s - %s: Missing object for environment \"%s\" in database endpoint \"%s\"", iName, iDesc, mEnv, endPointLocalId);
                    }
                }
            } else {
                ccr.addInterfaceError("%s - %s: \"%s\" endpoint definition is empty", iName, iDesc, endPointLocalId);
            }
        } else if (!Utils.isEmpty(iEP.getFileConfig())) {
            FileConfig fileConfig = iEP.getFileConfig();
            Set<String> mOFMEnvs = mEnvironmentsMap.get("ofmInst") == null ? new HashSet<String>() : mEnvironmentsMap.get("ofmInst");
            if(!Utils.isEmpty(fileConfig.getDefaultFile())) { 
                //we have all envs covered
            } else if (!Utils.isEmpty(fileConfig.getEnvSpecificFile())) {
                List<EnvSpecificFile> fileObjects = fileConfig.getEnvSpecificFile();
                for (EnvSpecificFile fileObject : fileObjects) {
                    String[] envs = fileObject.getEnvRefName().split(delimiter);
                    for (String env : envs) {
                        if(iEnvsSet.contains(env)) {
                            ccr.addInterfaceWarning("%s - %s: Multiple references of \"%s\" environment in FILE endpoint \"%s\"", iName, iDesc, env, endPointLocalId);
                        }
                        iEnvsSet.add(env);
                    }
                }
                for (String iEnv : iEnvsSet) {
                    if(!mOFMEnvs.contains(iEnv)) {
                        ccr.addInterfaceWarning("%s - %s: Referencing \"%s\" environment in FILE endpoint \"%s\", but it is note defined in %s", iName, iDesc, iEnv, endPointLocalId, SOATF_MASTER_CONFIG_FILENAME);
                    }
                }
                for (String mEnv : mOFMEnvs) {
                    if(!iEnvsSet.contains(mEnv)) {
                        ccr.addInterfaceError("%s - %s: Missing file definition for environment \"%s\" in FILE endpoint \"%s\"", iName, iDesc, mEnv, endPointLocalId);
                    }
                }
            } else {
                ccr.addInterfaceError("%s - %s: \"%s\" endpoint definition is empty", iName, iDesc, iEP.getEndPointLocalId());
            }
        } else if (!Utils.isEmpty(iEP.getFtpServer())) {
            FTPConfig ftpServer = iEP.getFtpServer();
            String refId = ftpServer.getRefId();
            Set<String> mFTPEnvs = mEnvironmentsMap.get(refId) == null ? new HashSet<String>() : mEnvironmentsMap.get("ofmInst");
            if(!Utils.isEmpty(ftpServer.getDefaultFile())) { 
                //we have all envs covered
            } else if (!Utils.isEmpty(ftpServer.getFile())) {
                List<FTPConfig.File> fileObjects = ftpServer.getFile();
                for (FTPConfig.File fileObject : fileObjects) {
                    String[] envs = fileObject.getEnvRefName().split(delimiter);
                    for (String env : envs) {
                        if(iEnvsSet.contains(env)) {
                            ccr.addInterfaceWarning("%s - %s: Multiple references of \"%s\" environment in FILE endpoint \"%s\"", iName, iDesc, env, endPointLocalId);
                        }
                        iEnvsSet.add(env);
                    }
                }
                for (String iEnv : iEnvsSet) {
                    if(!mFTPEnvs.contains(iEnv)) {
                        ccr.addInterfaceWarning("%s - %s: Referencing \"%s\" environment in FTP endpoint \"%s\", but it is note defined in %s", iName, iDesc, iEnv, endPointLocalId, SOATF_MASTER_CONFIG_FILENAME);
                    }
                }
                for (String mEnv : mFTPEnvs) {
                    if(!iEnvsSet.contains(mEnv)) {
                        ccr.addInterfaceError("%s - %s: Missing file definition for environment \"%s\" in FTP endpoint \"%s\"", iName, iDesc, mEnv, endPointLocalId);
                    }
                }
            } else {
                ccr.addInterfaceError("%s - %s: \"%s\" endpoint definition is empty", iName, iDesc, iEP.getEndPointLocalId());
            }
        } else if (!Utils.isEmpty(iEP.getJmsSubsystem())) {
            //not much to check here
        } else if (!Utils.isEmpty(iEP.getSoap())) {
            //not much to check here
        } else {
            ccr.addInterfaceError("%s - %s: \"%s\" enpoint is unknown", iName, iDesc, iEP.getEndPointLocalId());
            return ccr;
        }
        
        return ccr;
    }
}

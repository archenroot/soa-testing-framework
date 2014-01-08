/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ibm.soatf.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author user
 */
public class ConfigurationManager {
    private static final Logger logger = LogManager.getLogger(ConfigurationManager.class.getName());
    private static final ConfigurationManager instance = new ConfigurationManager();
    private boolean initialized = false;
    
    private FrameworkConfiguration frameworkConfig;
    private MasterConfiguration masterConfig;
    
    private ConfigurationManager() {
    }
    public static ConfigurationManager getInstance() {
        return instance;
    }
    
    public void init() throws FrameworkConfigurationException {
        if (!initialized) {
            logger.debug("Initializing configuration manager...");
            frameworkConfig = new FrameworkConfiguration();
            frameworkConfig.init();
            masterConfig = new MasterConfiguration(frameworkConfig);
            masterConfig.init();
            initialized = true;
        } else {
            logger.debug("Attempt to initialize configuration manager, but it was already initialized, ignoring...");
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public FrameworkConfiguration getFrameworkConfig() {
        checkInit();
        return frameworkConfig;
    }

    public MasterConfiguration getMasterConfig() {
        checkInit();
        return masterConfig;
    }

    public InterfaceConfiguration getInterfaceConfig(String interfaceName) throws FrameworkConfigurationException {
        checkInit();
        return masterConfig.getInterfaceConfig(interfaceName);
    }

    private void checkInit() {
        if (!initialized) {
            throw new IllegalStateException("ConfigurationManager not initialized. ConfigurationManager.getInstance().init() must be called before accessing any of the configurations.");
        }
    }
}

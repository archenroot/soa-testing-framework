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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Responsible for configuration initialisation process.
 *
 * @author Ladislav Jech <archenroot@gmail.com>
 */
final public class ConfigurationManager {

    private static final Logger logger = LogManager.getLogger(ConfigurationManager.class.getName());
    private static final ConfigurationManager instance = new ConfigurationManager();
    private boolean initialized = false;

    private MasterFrameworkConfig frameworkConfig;
    private MasterConfiguration masterConfig;

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
     *
     * @throws FrameworkConfigurationException
     */
    public void init() throws FrameworkConfigurationException {
        if (!initialized) {
            logger.debug("Initializing configuration manager...");
            frameworkConfig = new MasterFrameworkConfig();
            frameworkConfig.init();
            masterConfig = new MasterConfiguration(frameworkConfig);
            masterConfig.init();
            initialized = true;
        } else {
            logger.debug("Attempt to initialize configuration manager, but it was already initialized, ignoring...");
        }
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
        return masterConfig;
    }

    /**
     *
     * @param interfaceName
     * @return
     * @throws FrameworkConfigurationException
     */
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

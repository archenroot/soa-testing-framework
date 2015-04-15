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

import java.io.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class responsible for SOA Testing Framework initial configuration and
 * validation. Defined as singleton with on-demand initialisation.
 *
 * @author zANGETSu
 */
public class FrameworkConfiguration {
    private final Logger logger = LogManager.getLogger(FrameworkConfiguration.class.getName());

    public static final String SOA_TEST_HOME_ENV_VAR = "SOA_TEST_HOME";
    public static final String SOATF_HOME_ENV_VAR = "SOATF_HOME";
    
    public static final String JAXB_CONTEXT_PACKAGE = "com.ibm.soatf.config.master";

    public static final String SOATF_MASTER_CONFIG_FILENAME = "master-config.xml";
    public static final String IFACE_CONFIG_FILENAME = "config.xml";

    public static final String OSB_REFERENCE_PROJECT_DIR_NAME_PREFIX = "OSB_Reference_Project_-_";
    public static final String FLOW_PATTERN_DIR_NAME_PREFIX = "FlowPattern_-_";

    private File soaTestHome;
    private File soaTfHome;
    private File masterConfigFile;

    private final String fsValidationPattern = "^[.\\\\/:*?\"<>|]?[\\\\/:*?\"<>|]*";
    
    FrameworkConfiguration() {
        
    }

    void init() throws FrameworkConfigurationException {
        String var = System.getenv().get(SOATF_HOME_ENV_VAR);
        if(var == null || var.isEmpty()) {
            throw new FrameworkConfigurationException(SOATF_HOME_ENV_VAR + " environment variable not set.");
        }
        soaTfHome = new File(var);
        
        var = System.getenv().get(SOA_TEST_HOME_ENV_VAR);
        if(var == null || var.isEmpty()) {
            throw new FrameworkConfigurationException(SOA_TEST_HOME_ENV_VAR + " environment variable not set.");
        }
        soaTestHome = new File(var);
        
        masterConfigFile = new File(soaTestHome, SOATF_MASTER_CONFIG_FILENAME);
        if (!masterConfigFile.exists() || masterConfigFile.isDirectory()) {
            throw new FrameworkConfigurationException("There is something wrong with framework master configuration file configured as: "
                    + masterConfigFile.getAbsolutePath()
                    + ". File doesn't exist or is not a file."
            );
        }
    }

    private File getSoaTfHome() throws FrameworkConfigurationException {
        return soaTfHome;
    }

    public File getSoaTestHome() {
        return soaTestHome;
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
                    break;
                case '(':
                    wordDelimiterFound = true;
                    break;
                case ')':
                    wordDelimiterFound = true;
                    break;
                case ',':
                    wordDelimiterFound = true;
                    break;
                case '-':
                    wordDelimiterFound = true;
                    break;
                default:
                    if (!wordDelimiterFound) {
                        preFormatedName.append(c);
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

    public File getMasterConfigFile() {
        return masterConfigFile;
    }
}

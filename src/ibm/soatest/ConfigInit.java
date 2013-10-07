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
package ibm.soatest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public class ConfigInit {
    
    private static final Logger logger = LogManager.getLogger(ConfigInit.class.getName());
    public static String configurationDefaultFile = "../conf/soa-testing-framework-config-init.properties";
    
    
    public static Properties getCoinfigurationInit(){
        
        Properties configurationInit = new Properties();
        
        try {
            if (!new File(configurationDefaultFile).exists()){
                logger.error("Initialization properties file cannot be found on default location");
                throw new IOException();
            }
            configurationInit.load(new FileInputStream(configurationDefaultFile));
            logger.info("Initial confgiruation sucessfully loaded.");
            
        } catch (IOException ex){
            
        }
        return configurationInit;
    }

    public static String getConfigurationDefaultFile() {
        return configurationDefaultFile;
    }

    public static void setConfigurationDefaultFile(String configurationDefaultFile) {
        ConfigInit.configurationDefaultFile = configurationDefaultFile;
    }
    
    
}

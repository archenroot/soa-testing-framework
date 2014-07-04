/*
 * Copyright (C) 2014 user
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author user
 */
public class UserProperties {
    private static final Logger logger = LogManager.getLogger(UserProperties.class.getName());
    private static final String FILENAME = "user.properties";
    private static final File FILE = new File(FILENAME);
    private static final Properties PROPS = new Properties();
    
    public static final String LAST_ENV = "last.env";
    public static final String GENERIC_VIEWER = "generic.viewer";
    
    static {
        boolean exists = FILE.exists();
        if(!exists) {
            setProperty(LAST_ENV, "");
            setProperty(GENERIC_VIEWER, "");
            save();
        }
        if(exists) {
            try {
                PROPS.load(new FileInputStream(FILE));
            } catch (IOException ex) {
                logger.warn("Unable to load properties from file: " + FILE.getAbsolutePath(), ex);
            }
        } else {
            logger.warn("Unable to access the " + FILE.getAbsolutePath());
        }
    }
    
    public static String getExecutablePath(String executableName) {
        return PROPS.getProperty(executableName);
    }
    
    public static String getLastEnv() {
        return PROPS.getProperty(LAST_ENV);
    }
    
    public static void save() {
        try {
            PROPS.store(new FileOutputStream(FILE), FILENAME);
        } catch (IOException ex) {
            logger.warn("Unable to store user properties in file: " + FILE.getAbsolutePath());
        }
    }

    public static void setLastEnv(String env) {
        PROPS.setProperty(LAST_ENV, env);
    }

    public static void setProperty(String propName, String executablePath) {
        PROPS.setProperty(propName, executablePath);
    }
}

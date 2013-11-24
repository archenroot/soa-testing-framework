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
package com.ibm.soatf.tool;

import com.ibm.soatf.FrameworkConfigurationException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public class JavaEnvironment {

    private static final Logger logger = LogManager.getLogger(FrameworkConfigurationException.class.getName());

    public static final String JAVA_MAJOR_VERSION = getJVMMajorVersion();
    public static final String JAVA_MINOR_VERSION = getJVMMinorVersion();
    public static final String JAVA_UPDATE_VERSION = getJVMUpdateVersion();
    public static final Hashtable<String, String> JVM_VERSIONS = new Hashtable<String, String>() {
        {
            put("Major version", getJVMMajorVersion());
            put("Minor version", getJVMMinorVersion());
            put("Update version", getJVMUpdateVersion());
        }
    };

    public static final Hashtable<String, String> JAVA_VARIABLES = new Hashtable<String, String>() {
        {
            put("java.version", "Java Runtime Environment version");
            put("java.vendor", "Java Runtime Environment vendor");
            put("java.vendor.url", "Java vendor URL");
            put("java.home", "Java installation directory");
            put("java.vm.specification.version", "Java Virtual Machine specification version");
            put("java.vm.specification.vendor", "Java Virtual Machine specification vendor");
            put("java.vm.specification.name", "Java Virtual Machine specification name");
            put("java.vm.version", "Java Virtual Machine implementation version");
            put("java.vm.vendor", "Java Virtual Machine implementation vendor");
            put("java.vm.name", "Java Virtual Machine implementation name");
            put("java.specification.version", "Java Runtime Environment specification version");
            put("java.specification.vendor", "Java Runtime Environment specification vendor");
            put("java.specification.name", "Java Runtime Environment specification name");
            put("java.class.version", "Java class format version number");
            put("java.class.path", "Java class path");
            put("java.library.path", "List of paths to search when loading libraries");
            put("java.io.tmpdir", "Default temp file path");
            put("java.compiler", "Name of JIT compiler to use");
            put("java.ext.dirs", "Path of extension directory or directories");
            put("os.name", "Operating system name");
            put("os.arch", "Operating system architecture");
            put("os.version", "Operating system version");
            put("file.separator", "File separator (\"/\" on UNIX)");
            put("path.separator", "Path separator (\":\" on UNIX)");
            put("line.separator", "Line separator (\"\\n\" on UNIX)");
            put("user.name", "User's account name");
            put("user.home", "User's home directory");
            put("user.dir", "User's current working directory");

        }
    };

    public static void printJavaEnvironment() {
        logger.debug("Print JVM runtime environment.");
        for (String variable : JAVA_VARIABLES.keySet()) {

            logger.debug(JAVA_VARIABLES.get(variable) + " (" + variable + "): " + System.getProperty(variable) + ".");
        }
    }

    private static String getJVMMajorVersion() {
        String majorVersion = System.getProperty("java.version");
        majorVersion = majorVersion.substring(0, majorVersion.indexOf("."));
        logger.debug("Java major version: " + majorVersion);
        return majorVersion;
    }

    private static String getJVMMinorVersion() {
        String updateVersion = System.getProperty("java.version");
        updateVersion = updateVersion.substring(updateVersion.indexOf(".") + 1, updateVersion.indexOf(".") + 1);
        logger.debug("Java minor version: " + updateVersion);
        return updateVersion;
    }

    private static String getJVMUpdateVersion() {
        String updateVersion = System.getProperty("java.version");
        updateVersion = updateVersion.substring(updateVersion.lastIndexOf(".") + 1, updateVersion.length() - updateVersion.lastIndexOf("."));
        logger.debug("Java update version: " + updateVersion);
        return updateVersion;

    }
}

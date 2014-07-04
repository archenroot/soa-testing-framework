/*
 * Copyright (C) 2013 user
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author user
 */
public final class Utils {
    private static final Logger logger = LogManager.getLogger(Utils.class);
    private static final Set<String> regexChars = new HashSet<>(Arrays.asList(
            "|","\\","[","]","." //and lot's of others, I'm OK with these now
    ));
    
    /**
     * Construct the file path from the given <code>pathElements</code>
     * 
     * @param pathElements individual parts of the path
     * @return string representation of the path constructed from the <code>pathElements</code>
     */
    public static String getFullFilePathStr(String ...pathElements) {
        if (pathElements == null || pathElements.length == 0) {
            throw new IllegalArgumentException("At least 1 path element must be specified");
        }
        File path = new File(pathElements[0]);
        for (int i = 1; i < pathElements.length; i++) {
            path = new File(path, pathElements[i]);
        }
        return path.getAbsolutePath();
    }
    
    public static String getSQLExceptionMessage(SQLException ex) {
        return String.format("SQL Code: %s Message: %s", ex.getErrorCode(), ex.getMessage());
    }
    
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if(obj instanceof String) {
            return ((String) obj).isEmpty();
        }
        if(obj instanceof Collection) {
            return ((Collection) obj).isEmpty();
        }
        return false;
    }

    public static String escapeRegexChars(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            final String c2Str = Character.toString(c);
            if (regexChars.contains(c2Str)) {
                sb.append("\\");
            }
            sb.append(c2Str);
        }
        return sb.toString();
    }
    
    public static String insertTimestampToFilename(String fileName, Date date) {
        final int suffixPos = fileName.lastIndexOf(".");
        final String suffix = new SimpleDateFormat("_yyyyMMdd_HHmmss").format(date);
        StringBuilder sb = new StringBuilder(fileName);
        if(suffixPos == -1) {
            sb.append(suffix);
        } else {
            sb.insert(suffixPos, suffix);
        }
        return sb.toString();
    }

    /**
     * Deletes the directory contents recursively, deletes directories if <code>filesOnly</code> is <code>false</code>
     * @param rootDir the directory which contents you want to remove
     * @param filesOnly if true, keeps directories, deletes only files
     * @throws IOException 
     */
    public static void deleteDirContent(File rootDir, boolean filesOnly) throws IOException {
        if (!rootDir.isDirectory()) {
            return;
        }
        if(filesOnly) {
            Collection<File> files = FileUtils.listFiles(rootDir, FileFilterUtils.fileFileFilter(), FileFilterUtils.directoryFileFilter());
            for(File file : files) {
                if (file.isDirectory()) {
                    deleteDirContent(file, filesOnly);
                } else {
                    boolean success = file.delete();
                    logger.trace("Deleting file " + file.getName() + ": " + (success ? "Success" : "Failure"));
                }
            }
        } else {
            for(File file : rootDir.listFiles()) {
                if(file.isDirectory()) {
                    FileUtils.deleteDirectory(file);
                } else {
                    boolean success = file.delete();
                    logger.trace("Deleting file " + file.getName() + ": " + (success ? "Success" : "Failure"));
                }
            }
        }
    }
    
    /**
     * magic to get the shortened version of the path (8.3 MS-DOS names) on Windows, otherwise, no change
     * <pre></pre>
     * Example: full path to the <code>file</code> is C:\WORK\Irish Water\projects\trunk\soa_test\IW.600.EBS_M_PCM_SupplierDataVendors\FlowPattern_-_DatabaseToQueueToQueueToDatabase\TestScenarioForVendorSitesOfTypeEBSPCM002\PositiveScenario1SourceToDestination\db\eis_ebs_DatabaseAdapter_XXXIW_MW_PO_VENDOR_SITES_EXT_insert.sql
     * <pre></pre>
     * Returned value: C:\WORK\IRISHW~1\projects\trunk\soa_test\IW600~1.EBS\FLOWPA~1\TESTSC~1\POSITI~1\db\eis_ebs_DatabaseAdapter_XXXIW_MW_PO_VENDOR_SITES_EXT_insert.sql
     * @param file
     * @return shortened path of the <code>file</code> parent and original file name
     */
    public static String getOSSafeParentPath(File file) {
        if (!SystemUtils.IS_OS_WINDOWS) {
            return file.getAbsolutePath();
        }
        String shortDir = null;
        try {
            String command = "cmd /c for %I in (\"" + file.getParent() + "\") do %~sI";
            logger.trace("Running cmdline: " + command);
            BufferedReader br = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(command).getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                int idx = line.indexOf(">");
                if (idx != -1) {
                    logger.trace("Got this from the cmdline process: " + line);
                    shortDir = line.substring(idx + 1);
                    break;
                }
            }
            return shortDir + File.separator + file.getName();
        } catch (IOException e) {
            logger.warn("Could not get the short version of the " + file.getParent() + " directory.", e);
        }
        return null;
    }
}

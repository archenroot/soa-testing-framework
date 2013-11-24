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

import java.io.File;
import java.io.IOException;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FileUtils.forceMkdir;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public class FileSystem {
    

    public static final String PATH_DELIMITER ="/";
    
    public static final String ROOT_DIR = PATH_DELIMITER + "test";
    public static final String CURRENT_PATH = new File(".").toString();
     
     
    public static final String DATABASE_DIR = ROOT_DIR + PATH_DELIMITER + "database";
    public static final String DATABASE_SCRIPT_DIR = DATABASE_DIR + PATH_DELIMITER + "scripts";
    
    public static final String JMS_DIR = ROOT_DIR + PATH_DELIMITER + "jms";
    public static final String JMS_SCHEMA_DIR = JMS_DIR + PATH_DELIMITER + "schema";
    public static final String JMS_MESSAGE_DIR = JMS_DIR + PATH_DELIMITER + "message";
    
    public static final String REPORT_DIR = ROOT_DIR + PATH_DELIMITER + "report";
    public static final String REPORT_JUNIT_DIR = REPORT_DIR + PATH_DELIMITER + "junit";
    public static final String REPORT_HTML_DIR = REPORT_DIR + PATH_DELIMITER + "html";
    public static final String REPORT_PDF_DIR = REPORT_DIR + PATH_DELIMITER + "pdf";
    public static final String REPORT_XML_DIR = REPORT_DIR + PATH_DELIMITER + "xml";
    
   

    private static final Logger logger = LogManager.getLogger(FileSystem.class.getName());

    public static boolean writeContentToFile(String content, String fileName) {
        return false;
    }

    public static String readContentFromFile(String fileName) {
        String content = null;
        try {
            content = readFileToString(new File(fileName));
        } catch (IOException ex) {
            logger.error("Error while trying read content of file: " + ex.getLocalizedMessage());
        }
        return content;
    }

    public static void initializeFileSystemStructure(String path) {
        try {

            // Create root test directory
            forceMkdir(new File(path + ROOT_DIR));
            // Create database test directory
            forceMkdir(new File(path + DATABASE_DIR));
            forceMkdir(new File(path + DATABASE_SCRIPT_DIR));
            // Create jms test directory
            forceMkdir(new File(path + JMS_DIR));
            forceMkdir(new File(path + JMS_SCHEMA_DIR));
            forceMkdir(new File(path + JMS_MESSAGE_DIR));
            // Create report test directory
            forceMkdir(new File(path + REPORT_DIR));
            forceMkdir(new File(path + REPORT_HTML_DIR));
            forceMkdir(new File(path + REPORT_JUNIT_DIR));
            forceMkdir(new File(path + REPORT_PDF_DIR));
            forceMkdir(new File(path + REPORT_XML_DIR));

        } catch (IOException ex) {
            logger.error("Error when trying to initialize file system structure for testing framework: " + ex.getLocalizedMessage());
        }
    }
    
}

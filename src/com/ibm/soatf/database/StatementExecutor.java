/*
 * Copyright (C) 2013 Ladislav Jech <archenroot@gmail.com>
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
package com.ibm.soatf.database;

import com.ibm.soatf.ComponentResult;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Ladislav Jech <archenroot@gmail.com>
 */


/**
 * Tool to run database scripts
 */
public class StatementExecutor {

    private static final Logger logger = LogManager.getLogger(StatementExecutor.class);

    /**
     * Runs an SQL script from the file specified by the <code>inputScriptFile</code> parameter
     * 
     * @param conn SQL connection that on which to run this script
     * @param cor object used to store success/failure messages
     * @param inputScriptFile path to the script file
     * @throws StatementExecutorException if SQL or IO exception occurs
     */
    public static void runScript(Connection conn, ComponentResult cor, String inputScriptFile) throws StatementExecutorException {
        try {
            String sql = FileUtils.readFileToString(new File(inputScriptFile));
            if(sql.endsWith(";")) sql = sql.substring(0, sql.length()-1);
            String msg = "Successfuly loaded script file: " + inputScriptFile;
            logger.debug(msg);
            cor.addMsg(msg);
            
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            boolean hasResults = stmt.execute(sql);
            conn.commit();
            int updateCount = -1;
            if(!hasResults) {
                updateCount = stmt.getUpdateCount();
            }
            msg = "Script run successful, update count: " + updateCount;
            logger.debug(msg);
            cor.addMsg(msg);
        } catch (IOException ex) {
            String msg = "IOException: " + ex.getMessage();
            logger.error(msg, ex);
            throw new StatementExecutorException("Failed to open statement file (" + inputScriptFile + "): " + msg, ex);
        } catch (SQLException ex) {
            String msg = "SQLException " + ex.getErrorCode() + ": " + ex.getMessage();
            logger.error(msg, ex);
            throw new StatementExecutorException("Failed to generate INSERT statement: " + msg, ex);
        }
    }
}
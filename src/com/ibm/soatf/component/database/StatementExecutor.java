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
package com.ibm.soatf.component.database;

import com.ibm.soatf.flow.OperationResult;
import com.ibm.soatf.gui.ProgressMonitor;
import com.ibm.soatf.tool.FileSystem;
import com.ibm.soatf.tool.Utils;
import java.io.File;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
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
     * @param conn SQL connection on which you want to run this script
     * @param file script file
     * @return rowid of the inserted row
     * @throws com.ibm.soatf.component.database.DatabaseComponentException
     */
    public String runScript(Connection conn, File file) throws DatabaseComponentException {
        OperationResult cor = OperationResult.getInstance();
        String inputScriptFilePath = "";
        String inputScriptRelativePath = "";
        Statement stmt = null;
        try {
            ProgressMonitor.increment("Loading SQL script...");
            inputScriptFilePath = file.getAbsolutePath();
            inputScriptRelativePath = FileSystem.getRelativePath(file);
            String sql = FileUtils.readFileToString(file);
            if(sql.endsWith(";")) sql = sql.substring(0, sql.length()-1);
            String msg = "Successfuly loaded script [FILE: %s]"; 
            logger.debug(String.format(msg, inputScriptFilePath));
            cor.addMsg(msg, "<a href='file://"+inputScriptFilePath+"'>"+inputScriptFilePath+"</a>", inputScriptRelativePath);
            
            conn.setAutoCommit(false);
            
            
            /*stmt = conn.createStatement();
            ProgressMonitor.increment("Executing SQL script...");
            boolean hasResults = stmt.execute(sql);
            conn.commit();
            int updateCount = -1;
            if(!hasResults) {
                updateCount = stmt.getUpdateCount();
            }*/
            if (sql.endsWith(";")) {
                sql=sql.substring(0, sql.length()-1);
            }
            CallableStatement statement = conn.prepareCall("{call "+ sql + " RETURNING ROWID INTO ? }");
            statement.registerOutParameter( 1, Types.VARCHAR );
            ProgressMonitor.increment("Executing SQL script...");
            int updateCount = statement.executeUpdate();
            conn.commit();
            
            String rowId = null;
            if (updateCount > 0) rowId = statement.getString(1);

            msg = "Script run successful, update count: " + updateCount;
            logger.debug(msg);
            cor.addMsg(msg); 
            if (rowId != null) {
                msg = "rowId: " + rowId;
                logger.debug(msg);
                cor.addMsg(msg); 
            }
            
            final String logMsg = "Record has been inserted into source database '" + conn.getMetaData().getURL() + "'.\n"
                        + "Insert statement executed:\n%s";
            cor.addMsg(logMsg, sql, "[FILE: "+FileSystem.getRelativePath(file)+"]");
            cor.markSuccessful();
            return rowId;
        } catch (IOException ex) {
            final String msg = "Failed to open statement [FILE: %s].";
            cor.addMsg(msg, "<a href='file://"+inputScriptFilePath+"'>"+inputScriptFilePath+"</a>", inputScriptRelativePath);
            throw new DatabaseComponentException(String.format(msg,inputScriptFilePath), ex);            
        } catch (SQLException ex) {
            final String msg = String.format("Failed to execute INSERT statement: %s", Utils.getSQLExceptionMessage(ex));
            cor.addMsg(msg);
            throw new DatabaseComponentException(msg, ex);
        } finally {
            DatabaseComponent.closeStatement(stmt);
        }
    }
}
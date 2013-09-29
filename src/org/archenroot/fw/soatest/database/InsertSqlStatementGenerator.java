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
package org.archenroot.fw.soatest.database;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author zANGETSu
 */
public final class InsertSqlStatementGenerator {
    
    private Connection conn = null;
    private String objectName = null;
    private String outputSqlScriptFileName = null;
    
    public InsertSqlStatementGenerator(Connection conn, String objectName, String outputSqlScriptFileName){
        this.conn = conn;
        this.objectName = objectName;
        this.outputSqlScriptFileName = outputSqlScriptFileName;
    }
    public void generateInsertStatementsFromObject() {
        try {
            SqlStatementGenerator sqlStGen
                    = new SqlStatementGenerator(
                    this.conn, this.objectName, this.outputSqlScriptFileName);
            sqlStGen.generateInsertStatementsFromObject();
        } catch (Exception ex) {
            Logger.getLogger(DatabaseComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void generateSingleRowInsertStatement() {
        try {
            SqlStatementGenerator sqlStGen
                    = new SqlStatementGenerator(
                    this.conn, this.objectName, this.outputSqlScriptFileName);
            sqlStGen.generateOneRowSampleInsertStatement();
        } catch (Exception ex) {
            Logger.getLogger(DatabaseComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

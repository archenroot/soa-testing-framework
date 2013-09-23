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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.archenroot.fw.soatest.configuration.DatabaseType;

/**
 *
 * @author zANGETSu
 */
public class DatabaseTestComponent {

    private String databaseType;
    private String jdbcDriverClass;
    private String hostName;
    private BigInteger port;
    private String userName;
    private String password;
    private String serviceId;
    private String connectAs;
    private String objectName;
    private String jdbcUrl;
    private Connection conn;
    private String outputSQLScriptFileName;
    private DatabaseType dt;

    public enum CRUDType {

        INSERT, SELECT, UPDATE, DELETE
    }

    private DatabaseTestComponent() {
        // DUMMY constructor not for usage
    }

    public DatabaseTestComponent(DatabaseType dt) {
        this.dt = dt;
        init(dt);
    }

    private void init(DatabaseType dt) {
        try {
            this.databaseType = dt.getDatabaseType().value();
            this.jdbcDriverClass = dt.getDatabaseDriverClassName();
            this.hostName = dt.getHostName();
            this.port = dt.getPort();
            this.userName = dt.getUserName();
            this.password = dt.getPassword();
            this.serviceId = dt.getServiceId();
            this.connectAs = dt.getConnectAs();
            this.jdbcUrl = constructJdbcUrl(hostName, port, serviceId);
            this.objectName = dt.getObjectName();

            Class.forName(this.jdbcDriverClass);
            this.conn = DriverManager.getConnection(
                    this.jdbcUrl, this.userName, this.password);

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DatabaseTestComponent.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseTestComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String constructJdbcUrl(String hostName, BigInteger port, String serviceId) {
        return "jdbc:oracle:thin:@"
                + hostName + ":"
                + port.toString() + ":"
                + serviceId;
    }

    public void generateInsertStatementsFromObject() {
        try {
            SQLStatementGenerator sqlStGen
                    = new SQLStatementGenerator(
                    this.conn, dt.getObjectName(), dt.getOutputSQLScriptFileName());
            sqlStGen.generateInsertStatementsFromObject();
        } catch (Exception ex) {
            Logger.getLogger(DatabaseTestComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void generateSingleRowInsertStatement() {
        try {
            SQLStatementGenerator sqlStGen
                    = new SQLStatementGenerator(
                    this.conn, dt.getObjectName(), dt.getOutputSQLScriptFileName());
            sqlStGen.generateOneRowSampleInsertStatement();
        } catch (Exception ex) {
            Logger.getLogger(DatabaseTestComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String generateSelectStatement() {
        return null;

    }

    public String generateUpdateStatement() {
        return null;

    }

    public String generateDeleteStatement() {
        return null;

    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setPort(BigInteger port) {
        this.port = port;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setConnectAs(String connectAs) {
        this.connectAs = connectAs;
    }

    public void generateSQLStatement(CRUDType crudType) throws UnknownCRUDTypeException {
        switch (crudType) {
            case INSERT:
                this.generateSingleRowInsertStatement();
                break;
            case SELECT:
                this.generateSelectStatement();
                break;
            case UPDATE:
                this.generateUpdateStatement();
                break;
            case DELETE:
                this.generateDeleteStatement();
                break;
            default:
                throw new UnknownCRUDTypeException("Unknown CRUDType value: supported values are "
                        + CRUDType.INSERT.toString() + ", "
                        + CRUDType.SELECT.toString() + ", "
                        + CRUDType.UPDATE.toString() + ", "
                        + CRUDType.DELETE.toString() + ", ");
        }
    }

    public void writeStatementToFile(String statement, String pathToFile) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(new File(pathToFile));
            fw.write(statement);
            fw.flush();
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(DatabaseTestComponent.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(DatabaseTestComponent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void insertRowIntoDatabase(String sqlScriptFileName){
        
    }
    
    public void insertDynamicRowIntoDatabase(){
        try {
            Statement stmt = conn.createStatement();
            
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseTestComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

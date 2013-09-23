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
    private String tableName ="MESSAGE_STORE";

    protected enum CRUDType {INSERT, SELECT, UPDATE, DELETE}
    
    public final class JDBCDriverClass {
        public static final String Oracle = "oracle.jdbc.driver.OracleDriver";
        public static final String Microsoft = "NOT IMPLEMENTED YET";
    }
            
    private class UnknownCRUDTypeException extends Exception {

        public UnknownCRUDTypeException() {}
        
        public UnknownCRUDTypeException(String message) {
            super(message);
        }
        
        public UnknownCRUDTypeException(Throwable cause){
            super(cause);
        }
        
        public UnknownCRUDTypeException(String message, Throwable cause)
        {
            super(message, cause);
        }
    }
  
    DatabaseTestComponent(){
        // DUMMY constructor not for usage
    }
    
    public DatabaseTestComponent(DatabaseType dt){
        init(dt);
    }
    
    private void init(DatabaseType dt){
        this.databaseType = dt.getDatabaseType().value();
        this.jdbcDriverClass = JDBCDriverClass.Oracle;
        this.hostName = dt.getHostName();
        this.port = dt.getPort();
        this.userName = dt.getUserName();
        this.password = dt.getPassword();
        this.serviceId = dt.getServiceId();
        this.connectAs = dt.getConnectAs();
    }
    
     public String generateInsertStatement(){
        try {
            SQLStatementGenerator sqlStGen = new SQLStatementGenerator(
                    this.jdbcDriverClass
                    , this.hostName
                    , this.port
                    , this.userName
                    , this.password
                    , this.serviceId
                    , this.connectAs
                    , this.tableName);
            sqlStGen.createInsertStatement();
        } catch (Exception ex) {
            Logger.getLogger(DatabaseTestComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
        //return null;
        return null;
    }
    
    private String generateSelectStatement(){
        return null;
        
    }
    
    private String generateUpdateStatement(){
        return null;
        
    }
    
    private String generateDeleteStatement(){
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
    
    public String generateSQLStatement(CRUDType crudType ) throws UnknownCRUDTypeException{
        switch (crudType) {
            case INSERT:
                return this.generateInsertStatement();
            case SELECT:
                return this.generateSelectStatement();
            case UPDATE:
                return this.generateUpdateStatement();
            case DELETE:
                return this.generateDeleteStatement();
            default:
                throw new UnknownCRUDTypeException("Unknown CRUDType value: supported values are " 
                        + CRUDType.INSERT.toString() + ", "
                        + CRUDType.SELECT.toString() + ", "
                        + CRUDType.UPDATE.toString() + ", "
                        + CRUDType.DELETE.toString() + ", ");
        }
    }
    
   public void writeStatementToFile(String statement, String pathToFile){
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
    
   
    
}

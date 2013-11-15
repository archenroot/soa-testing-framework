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
package com.ibm.soatf.database;

import com.ibm.soatf.ComponentResult;
import com.ibm.soatf.SOATFComponent;
import com.ibm.soatf.SOATFCompType;
import com.ibm.soatf.CompOperType;
import static com.ibm.soatf.CompOperType.DATABASE_OPERATIONS;
import static com.ibm.soatf.CompOperType.DB_EXECUTE_INSERT_FROM_FILE;
import static com.ibm.soatf.CompOperType.DB_GENERATE_INSERT_ONE_ROW_RANDOM;
import com.ibm.soatf.config._interface.database.DatabaseConfiguration;
import com.ibm.soatf.config._interface.database.DbObject;
import com.ibm.soatf.config.master.Databases.Database.DatabaseInstance;
import com.ibm.soatf.mapping.IMappingEndpoint;
import com.ibm.soatf.tool.FileSystem;
import com.ibm.soatf.util.Utils;
import java.io.File;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Database component server every stuff related to databases. Basically
 * it provides common CRUD statement generation and execution.
 * @author zANGETSu
 */
public class DatabaseComponent extends SOATFComponent implements IMappingEndpoint {

    private static final Logger logger = LogManager.getLogger(DatabaseComponent.class);
    
    /**
     * Date format definition when working with DATE, DATETIME and TIMESTAMP JDBC data types.
     */
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    /**
     * Time format definition when working with DATE, DATETIME and TIMESTAMP JDBC data types.
     */
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");    

    /**
     * Common suffix for generated SQL statements of type INSERT operation.
     */
    public static final String INSERT_FILE_SUFFIX = "_insert.sql";

    /**
     * Common delimiter for file names.
     */
    public static final String NAME_DELIMITER = "_";
    
    private final DatabaseConfiguration databaseInterfaceConfiguration;
    // Only oracle database is supported now
    private final String driverClassName = "oracle.jdbc.driver.OracleDriver";
    private String hostName;
    private BigInteger port;
    private String userName;
    private String password;
    private String serviceId;
    // Not implemented yet
    //private String connectAs
    private final List<DbObject> dbObjects = new ArrayList<>();
    private String jdbcUrl;
    private final DatabaseInstance databaseMasterConfig;
    
    public DatabaseComponent(
            DatabaseInstance databaseMasterConfig,
            DatabaseConfiguration databaseInterfaceConfiguration, 
            ComponentResult componentOperationResult) {
        super(SOATFCompType.DATABASE, componentOperationResult);
        this.databaseMasterConfig = databaseMasterConfig;
        this.databaseInterfaceConfiguration = databaseInterfaceConfiguration;
        constructComponent();
    }

    /**
     * Method for database component construction.
     */
    @Override
    protected final void constructComponent() {
        try {
            logger.debug("Constructing DatabaseComponent object.");
            
            hostName = databaseMasterConfig.getHostName();
            port = databaseMasterConfig.getPort();
            userName = databaseMasterConfig.getUserName();
            password = databaseMasterConfig.getPassword();
            serviceId = databaseMasterConfig.getServiceId();
            for (DbObject object : databaseInterfaceConfiguration.getDbObjects().getDbObject()) {
                dbObjects.add(object);
            }

            jdbcUrl = constructJdbcUrl(hostName, port, serviceId);

            Class.forName(driverClassName);
        } catch (ClassNotFoundException ex) {
            logger.error("Database driver class cannot be found: " + ex.getMessage());
        }
        logger.debug("Constructing DatabaseComponent finished.");
    }

    /**
     * 
     * @param objectName
     * @return
     */
    @Override
    public Iterator<File> getGeneratedFiles(String objectName) {
        String pattern = "*";
        if (objectName != null) pattern = objectName;
        String filemask = new StringBuilder(identificator).append(NAME_DELIMITER).append(pattern).append(INSERT_FILE_SUFFIX).toString();
        return FileUtils.iterateFiles(new File(Utils.getFullFilePathStr(FileSystem.CURRENT_PATH, FileSystem.DATABASE_SCRIPT_DIR)), new WildcardFileFilter(filemask), TrueFileFilter.INSTANCE);
    }

    /**
     * Common method which executes specific atomic integration operation
     * supported within database component.
     * @param componentOperation   operation to execute
     */
    @Override
    protected void executeOperation(CompOperType componentOperation) {
        if (!DATABASE_OPERATIONS.contains(componentOperation)) {
            final String msg = "Unsupported operation: " + componentOperation + ". Valid operations are: " + DATABASE_OPERATIONS;
            logger.error(msg);
            componentOperationResult.setResultMessage(msg);
            componentOperationResult.setOverallResultSuccess(false);
        } else {
            for (DbObject object : dbObjects) {
                String filename = new StringBuilder(identificator).append(NAME_DELIMITER).append(object.getName()).append(INSERT_FILE_SUFFIX).toString();
                String path = Utils.getFullFilePathStr(FileSystem.CURRENT_PATH, FileSystem.DATABASE_SCRIPT_DIR, filename);
                switch (componentOperation) {
                    case DB_GENERATE_INSERT_ONE_ROW_RANDOM:
                        generateInsertStatement(object, path);
                        break;
                    case DB_EXECUTE_INSERT_FROM_FILE:
                        executeInsertFromFile(path);
                        break;
                    default:
                        logger.info("Operation execution not yet implemented: " + componentOperation);
                        componentOperationResult.setResultMessage("Operation: " + componentOperation + " is valid, but not yet implemented");
                        componentOperationResult.setOverallResultSuccess(false);
                }
            }
        }
    }

    private void generateInsertStatement(DbObject object, String insertSqlScriptFileName) {
        Connection conn = null;
        try {
            conn = getConnection();
            StatementGenerator.generateInsertStatement(conn, object, insertSqlScriptFileName, componentOperationResult);
            componentOperationResult.setOverallResultSuccess(true);
        } catch (SQLException e) {
            String sqlExMsg = e.getErrorCode() + ": " + e.getMessage();
            String msg = String.format("Could not get database connection: %s, %s/%s SQLException is: %s", jdbcUrl, userName, "********", sqlExMsg);
            logger.error(msg);
            componentOperationResult.addMsg(msg);
            componentOperationResult.setOverallResultSuccess(false);
        } catch (StatementGeneratorException e) {
            String msg = String.format("Statement generator failure: %s", e.getMessage());
            logger.error(msg);
            componentOperationResult.addMsg(msg);
            componentOperationResult.setOverallResultSuccess(false);
        } finally {
            closeConnection(conn);
        }
    }

    private void executeInsertFromFile(String insertSQLScriptFile) {
        Connection conn = null;
        try {
            conn = getConnection();
            StatementExecutor.runScript(conn, componentOperationResult, insertSQLScriptFile);
            componentOperationResult.setOverallResultSuccess(true);
        } catch (SQLException e) {
            String sqlExMsg = e.getErrorCode() + ": " + e.getMessage();
            String msg = String.format("Could not get database connection: %s, %s/%s SQLException is: %s", jdbcUrl, userName, "********", sqlExMsg);
            logger.error(msg);
            componentOperationResult.addMsg(msg);
            componentOperationResult.setOverallResultSuccess(false);
        } catch (StatementExecutorException e) {
            String msg = String.format("Statement executor failure: %s", e.getMessage());
            logger.error(msg);
            componentOperationResult.addMsg(msg);
            componentOperationResult.setOverallResultSuccess(false);
        } finally {
            closeConnection(conn);
        }
    }

    private static String constructJdbcUrl(String hostName, BigInteger port, String serviceId) {
        return String.format("jdbc:oracle:thin:@%s:%s:%s", hostName, port.toString(), serviceId);
    }

    private void closeConnection(Connection conn) {
        try {
            if(conn != null) {
                conn.close();
            }
        } catch (SQLException ex) {
            logger.error("The database connection cannot be closed due to: " + ex.getLocalizedMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, userName, password);
    }
}

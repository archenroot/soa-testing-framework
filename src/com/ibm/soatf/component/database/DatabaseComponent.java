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
package com.ibm.soatf.component.database;

import com.ibm.soatf.component.ComponentResult;
import com.ibm.soatf.component.SOATFComponent;
import com.ibm.soatf.component.SOATFCompType;
import com.ibm.soatf.flow.FlowPatternCompositeKey;
import com.ibm.soatf.FrameworkConfiguration;
import com.ibm.soatf.config.iface.db.DBConfig;
import com.ibm.soatf.config.iface.db.DbObject;
import com.ibm.soatf.config.master.Databases.Database.DatabaseInstance;
import com.ibm.soatf.config.master.Operation;
import com.ibm.soatf.tool.FileSystem;
import com.ibm.soatf.component.util.Utils;
import java.io.File;
import static java.lang.Boolean.TRUE;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
 * Database component server every stuff related to databases. Basically it
 * provides common CRUD statement generation and execution.
 *
 * @author zANGETSu
 */
public class DatabaseComponent extends SOATFComponent {

    private static final Logger logger = LogManager.getLogger(DatabaseComponent.class);
    private static final FrameworkConfiguration FCFG = FrameworkConfiguration.getInstance();

    /**
     * Date format definition when working with DATE, DATETIME and TIMESTAMP
     * JDBC data types.
     */
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    /**
     * Time format definition when working with DATE, DATETIME and TIMESTAMP
     * JDBC data types.
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

    // Consider to change in next live
    public static final Boolean POOLING_USE_POOLED_VALUE = true;
    public static final Boolean POOLING_USE_CUSTOM_VALUE = false;
    public static final Boolean POOLING_DONT_USE = null;

    private final DBConfig dbIfaceConfig;
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
    private String workingDirectoryPath;
    private FlowPatternCompositeKey fpck;

    private String messageRefColName;
    private String messageRefColValue;
    private String entityRefColName;
    private String entityRefColValue;
    private String poolingColName;
    private String poolingColValue;
    private String poolingPooledValue;
    private String preparedStatement;

    public DatabaseComponent(
            DatabaseInstance databaseMasterConfig,
            DBConfig dbIfaceConfig,
            ComponentResult componentOperationResult,
            FlowPatternCompositeKey ifaceFlowPatternCompositeKey) {
        super(SOATFCompType.DATABASE, componentOperationResult);
        this.databaseMasterConfig = databaseMasterConfig;
        this.dbIfaceConfig = dbIfaceConfig;
        this.fpck = ifaceFlowPatternCompositeKey;

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
            dbObjects.add(dbIfaceConfig.getDbObjects().getDbObject());
            /*
             * Need to be refactorized!!!
             */
            workingDirectoryPath = FCFG.SOA_TEST_HOME + "\\"
                    + fpck.getIfaceName() + "_" + FCFG.getValidFileSystemObjectName(fpck.getIfaceDesc()) + "\\"
                    + FCFG.FLOW_PATTERN_DIR_NAME_PREFIX + FCFG.getValidFileSystemObjectName(fpck.getFlowPatternId()) + "\\"
                    + FCFG.getValidFileSystemObjectName(fpck.getTestName()) + "\\"
                    + FCFG.getValidFileSystemObjectName(fpck.getTestScenarioId()) + "\\db\\";

            jdbcUrl = constructJdbcUrl(hostName, port, serviceId);

            Class.forName(driverClassName);
        } catch (ClassNotFoundException ex) {
            logger.fatal("Database driver class cannot be found: " + ex.getMessage());
        }
        logger.debug("Constructing DatabaseComponent finished.");
    }

    /**
     *
     * @param objectName
     * @return
     */
    public Iterator<File> getGeneratedFiles(String objectName) {
        String pattern = "*";
        if (objectName != null) {
            pattern = objectName;
        }
        String filemask = new StringBuilder(identificator).append(NAME_DELIMITER).append(pattern).append(INSERT_FILE_SUFFIX).toString();
        return FileUtils.iterateFiles(new File(Utils.getFullFilePathStr(FileSystem.CURRENT_PATH, FileSystem.DATABASE_SCRIPT_DIR)), new WildcardFileFilter(filemask), TrueFileFilter.INSTANCE);
    }

    /**
     * Common method which executes specific atomic integration operation
     * supported within database component.
     *
     * @param operation
     * @param componentOperation operation to execute
     */
    @Override
    protected void executeOperation(Operation operation) {
        /* 
         if (!DATABASE_OPERATIONS.contains(operation)) {
         final String msg = "Unsupported operation: " + componentOperation + ". Valid operations are: " + DATABASE_OPERATIONS;
         logger.error(msg);
         compOperResult.setResultMessage(msg);
         compOperResult.setOverallResultSuccess(false);
         } else {
         */
        for (DbObject object : dbObjects) {
            final String filename = new StringBuilder(dbIfaceConfig.getRefId().replace("/", "_")).append(NAME_DELIMITER).append(object.getName()).append(INSERT_FILE_SUFFIX).toString();
            final String path = Utils.getFullFilePathStr(workingDirectoryPath, filename);
            switch (operation.getName()) {

                case DB_INSERT_RECORD:
                    generateInsertStatement(object, path);
                    executeInsertFromFile(path);
                    break;
                case DB_DELETE_RECORD:
                    deleteRecord();
                    break;
                case DB_CHECK_RECORD_POOLED:
                    checkIfRecordIsPooled();
                    break;
                case DB_CHECK_RECORD_NOT_POOLED:
                    checkIfRecordIsNotPooled();
                    break;
                default:
                    logger.info("Operation execution not yet implemented: " + operation.getName());
                    compOperResult.setResultMessage("Operation: " + operation.getName() + " is valid, but not yet implemented");
                    compOperResult.setOverallResultSuccess(false);
            }
        }
    }

    private void generateInsertStatement(DbObject object, String insertSqlScriptFileName) {
        Connection conn = null;
        try {
            conn = getConnection();
            StatementGenerator.generateInsertStatement(conn, object, insertSqlScriptFileName, compOperResult);
            compOperResult.setOverallResultSuccess(true);
        } catch (SQLException e) {
            String sqlExMsg = e.getErrorCode() + ": " + e.getMessage();
            String msg = String.format("Could not get database connection: %s, %s/%s SQLException is: %s", jdbcUrl, userName, "********", sqlExMsg);
            logger.error(msg);
            compOperResult.addMsg(msg);
            compOperResult.setOverallResultSuccess(false);
        } catch (StatementGeneratorException e) {
            String msg = String.format("Statement generator failure: %s", e.getMessage());
            logger.error(msg);
            compOperResult.addMsg(msg);
            compOperResult.setOverallResultSuccess(false);
        } finally {
            closeConnection(conn);
        }
    }

    private void executeInsertFromFile(String insertSQLScriptFile) {
        Connection conn = null;
        try {
            conn = getConnection();
            StatementExecutor.runScript(conn, compOperResult, insertSQLScriptFile);
            compOperResult.setResultMessage("Record has been inserted into source database.");
            compOperResult.setOverallResultSuccess(true);
        } catch (SQLException e) {
            String sqlExMsg = e.getErrorCode() + ": " + e.getMessage();
            String msg = String.format("Could not get database connection: %s, %s/%s SQLException is: %s", jdbcUrl, userName, "********", sqlExMsg);
            logger.error(msg);
            compOperResult.addMsg(msg);
            compOperResult.setOverallResultSuccess(false);
        } catch (StatementExecutorException e) {
            String msg = String.format("Statement executor failure: %s", e.getMessage());
            logger.error(msg);
            compOperResult.addMsg(msg);
            compOperResult.setOverallResultSuccess(false);
        } finally {
            closeConnection(conn);
        }
    }

    public static String constructJdbcUrl(String hostName, BigInteger port, String serviceId) {
        return String.format("jdbc:oracle:thin:@%s:%s:%s", hostName, port.toString(), serviceId);
    }

    private void closeConnection(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ex) {
            logger.error("The database connection cannot be closed due to: " + ex.getLocalizedMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, userName, password);
    }

    private void deleteRecord() {
        Connection conn = null;
        try {
            conn = getConnection();
            StringBuilder sb = new StringBuilder();
            sb.append("DELETE ");
            sb.append(getPreparedStatement(POOLING_DONT_USE));
            String command = sb.toString();
            Statement statement = conn.createStatement();
            boolean rs = statement.execute(sb.toString());
            compOperResult.setResultMessage("Relevant records deleted by issuing: \n" + command);
            compOperResult.setOverallResultSuccess(true);

        } catch (SQLException ex) {
            logger.fatal("Wrong", ex);
            compOperResult.setResultMessage("There occured error while trying to check if record is still pooled: " + ex.getMessage());
            compOperResult.setOverallResultSuccess(false);

        }

    }

    private void checkIfRecordIsPooled() {
        Connection conn = null;
        try {
            conn = getConnection();
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * ");
            sb.append(getPreparedStatement(POOLING_USE_POOLED_VALUE));
            String command = sb.toString();
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(sb.toString());
            int cnt = 0;
            while (rs.next()) {
                compOperResult.setResultMessage("Record is pooled, the test has been done by issuing: \n" + command);
                compOperResult.setOverallResultSuccess(true);
                logger.debug("Record: " + rs.getString("CITY"));
                return;
            }
            if (cnt==0){
                compOperResult.setOverallResultSuccess(false);
                compOperResult.setResultMessage("Database record has not been pooled, check was done by issuing: \n" + command);
            }
            
            logger.trace("Check for pooled record in source database resulted in: " + compOperResult.isOverallResultSuccess());

        } catch (SQLException ex) {
            logger.fatal("Wrong", ex);
            compOperResult.setResultMessage("There occured error while trying to check if record is still pooled: " + ex.getMessage());
            compOperResult.setOverallResultSuccess(false);

        }
    }

    private void checkIfRecordIsNotPooled() {
        Connection conn = null;
        try {
            conn = getConnection();
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * ");
            sb.append(getPreparedStatement(POOLING_USE_CUSTOM_VALUE));
            String command = sb.toString();
            Statement statement = conn.createStatement();

            ResultSet rs = statement.executeQuery(sb.toString());
            int cnt = 0;
            while (rs.next()) {
                compOperResult.setResultMessage("There is still not pooled record in the source database for, the test has been done by issuing: \n" + command);

                compOperResult.setOverallResultSuccess(true);
                logger.debug("Record: " + rs.getString("CITY"));
            }

            logger.trace("Check for pooled record in source database resulted in: " + compOperResult.isOverallResultSuccess());

        } catch (SQLException ex) {
            logger.fatal("Wrong", ex);

        }

    }

    private String getPreparedStatement(Boolean usePooled) {
        StringBuilder sb = new StringBuilder();

        for (DbObject.CustomValue cusVal : dbIfaceConfig.getDbObjects().getDbObject().getCustomValue()) {

            Boolean bSEId = cusVal.isSourceEntityId();
            if (bSEId != null) {
                if (cusVal.isSourceEntityId().equals(TRUE)) {
                    entityRefColName = cusVal.getColumnName();
                    entityRefColValue = cusVal.getColumnValue();
                }
            }
            Boolean bSMId = cusVal.isSourceMessageId();
            if (bSMId != null) {
                if (cusVal.isSourceMessageId().equals(TRUE)) {
                    messageRefColName = cusVal.getColumnName();
                    messageRefColValue = cusVal.getColumnValue();
                }
            }

            String pooledValue = cusVal.getPooledValue();
            if (pooledValue != null) {
                poolingColName = cusVal.getColumnName();
                poolingPooledValue = pooledValue;
                poolingColValue = cusVal.getColumnValue();
            }
        }

        sb.append(" FROM " + dbIfaceConfig.getDbObjects().getDbObject().getName());
        sb.append(" WHERE "
                + entityRefColName + "= '" + entityRefColValue + "'"
                + " AND "
                + messageRefColName + " = '" + messageRefColValue + "'");
        if (usePooled != null) {
            if (usePooled == false) {
                sb.append(" AND " + poolingColName + " = '" + poolingColValue + "'");
            } else if (usePooled == true) {
                sb.append(" AND " + poolingColName + " = '" + poolingPooledValue + "'");
            }
        }
        return sb.toString();
    }
}

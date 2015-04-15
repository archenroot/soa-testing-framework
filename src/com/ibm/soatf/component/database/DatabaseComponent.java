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

<<<<<<< HEAD

=======
/**
 * Component used for task related to databases. Basically CRUD operation, but 
 * not restricted to. Currently only Oracle Database is supported, but extending for 
 * any JDBC compatible database is scheduled.
 * @author Ladislav Jech <archenroot@gmail.com>
 */
>>>>>>> 7c2802d5d20e30d5191a0f8f327cacd09e189422
import com.ibm.soatf.component.AbstractSoaTFComponent;
import com.ibm.soatf.component.SOATFCompType;
import com.ibm.soatf.config.iface.db.DbObject;
import com.ibm.soatf.config.master.Databases.Database.DatabaseInstance;
import com.ibm.soatf.config.master.Operation;
import com.ibm.soatf.flow.FrameworkExecutionException;
import com.ibm.soatf.flow.OperationResult;
import com.ibm.soatf.gui.ProgressMonitor;
import com.ibm.soatf.tool.Utils;
import java.io.File;
import static java.lang.Boolean.TRUE;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
<<<<<<< HEAD
 * Component used for task related to databases. Provides common CRUD statement generation and execution.
 * Currently only Oracle Database is supported, but extending for 
 * any JDBC compatible database is scheduled.
=======
 * Database component server every stuff related to databases. Basically it
 * provides common CRUD statement generation and execution.
 *
>>>>>>> 7c2802d5d20e30d5191a0f8f327cacd09e189422
 * @author Ladislav Jech <archenroot@gmail.com>
 */
public class DatabaseComponent extends AbstractSoaTFComponent {

    private static final Logger logger = LogManager.getLogger(DatabaseComponent.class);

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
    public static final Boolean POLLING_USE_POLLED_VALUE = true;
    public static final Boolean POLLING_USE_CUSTOM_VALUE = false;
    public static final Boolean POLLING_DONT_USE = null;

    // Only oracle database is supported now
    private final String driverClassName = "oracle.jdbc.driver.OracleDriver";
    private String hostName;
    private int port;
    private String userName;
    private String password;

    private List<DbObject> dbObjects = new ArrayList<>();
    private String jdbcUrl;

    private final DatabaseInstance databaseMasterConfig;
    
    private static OperationResult cor;
    
    private Map<DbObject, DbObjectConfig> dbObjectConfigs;
    private DbObject parentDbObject;
    private String refId;
<<<<<<< HEAD
    
    private static Map<DbObject, String> dbObjectInsertRowIds = new HashMap<>();
=======
>>>>>>> 7c2802d5d20e30d5191a0f8f327cacd09e189422

    private static enum SQL_COMMAND {
        SELECT,
        DELETE
    }

    /**
     * Construct DB component. 
<<<<<<< HEAD
     * @param databaseMasterConfig configuration of the specific database (host, port, credentials, etc...) from master-config.xml
     * @param dbObjects list of the tables that will be affected by executing operations on this instance of database component
=======
     * @param databaseMasterConfig configuration of the concrete database (host, port, credentials, etc...) from master-config.xml
     * @param dbObjects tables that this component will work with
>>>>>>> 7c2802d5d20e30d5191a0f8f327cacd09e189422
     * @param workingDir working dir for storing generated inserts (i.e. interface/flowpatter/testscenario/db)
     * @param refId used in the file name construction
     * @throws DatabaseComponentException 
     */
    public DatabaseComponent(
            DatabaseInstance databaseMasterConfig,
            List<DbObject> dbObjects,
            File workingDir,
            String refId) throws DatabaseComponentException {
        super(SOATFCompType.DATABASE);
        this.databaseMasterConfig = databaseMasterConfig;
        this.dbObjects = dbObjects;
        this.workingDir = workingDir;
        this.refId = refId;
        cor = OperationResult.getInstance();
        constructComponent();
    }

    /**
     * Method for database component construction. Pulls out the connection information from the DB instance xml object,
     * constructs JDBC URL, and tries to load the database driver
     * @throws com.ibm.soatf.component.database.DatabaseComponentException if there's error during DB driver loading
     */
    @Override
    protected final void constructComponent() throws DatabaseComponentException {
        try {
            logger.trace("Constructing DatabaseComponent object.");
            hostName = databaseMasterConfig.getHostName();
            port = databaseMasterConfig.getPort();
            userName = databaseMasterConfig.getUserName();
            password = databaseMasterConfig.getPassword();
            parentDbObject = dbObjects.get(0);
            dbObjectConfigs = createConfigMap(dbObjects);
            
            final boolean isSID = databaseMasterConfig.getServiceId() != null;
            if (isSID) {
                jdbcUrl = constructJdbcUrl(hostName, port, isSID, databaseMasterConfig.getServiceId());
            } else {
                jdbcUrl = constructJdbcUrl(hostName, port, isSID, databaseMasterConfig.getServiceName());
            }
            
            Class.forName(driverClassName);
        } catch (ClassNotFoundException ex) {
            final String msg = String.format("Cannot load jdbc driver class: %s", ex.getMessage());
            cor.addMsg(msg);
            throw new DatabaseComponentException(msg, ex);
        }
        logger.trace("Constructing DatabaseComponent finished.");
    }
    
    /**
     * Utility method for creating map of DB object/DB Object configuration pairs
     * @param dbObjects
     * @return
     * @throws DatabaseComponentException 
     */
    private Map<DbObject, DbObjectConfig> createConfigMap(List<DbObject> dbObjects) throws DatabaseComponentException {
        Map<DbObject, DbObjectConfig> map = new HashMap<>();
        boolean isParent = true;
        for(DbObject object : dbObjects) {
            map.put(object, new DbObjectConfig(object, isParent));
            isParent = false; //only the very first DbObject is considered parent
        }
        return map;
    }

    /**
     * Common method which executes specific atomic integration operation
     * supported within database component.
     *
     * @param operation operation to execute
     * @throws com.ibm.soatf.flow.FrameworkExecutionException
     */
    @Override
    protected void executeOperation(Operation operation) throws FrameworkExecutionException {
        switch (operation.getName()) {
            case DB_INSERT_RECORD:
                for (DbObject object : dbObjects) {

                    final String filename = new StringBuilder(refId.replace("/", "_")).append(NAME_DELIMITER).append(object.getName()).append(INSERT_FILE_SUFFIX).toString();
                    final File file = new File(workingDir, filename);
                    generateInsertStatement(object, file);
<<<<<<< HEAD
                    executeInsertFromFile(object, file);
=======
                    executeInsertFromFile(file);
>>>>>>> 7c2802d5d20e30d5191a0f8f327cacd09e189422
                }
                break;
            case DB_DELETE_RECORD:
                //due to possible foreign keys we need to delete the objects in reversed order
                //we also presume that the "parent" table is the first object defined in the config
                //and the rest are "child" objects
                ProgressMonitor.init(dbObjects.size() * 3 + 1);
                for (int i = dbObjects.size() - 1; i > -1; i--) {
                    deleteRecord(dbObjects.get(i));
                }
                break;
            case DB_CHECK_RECORD_POOLED:
                checkIfRecordIsPolled(dbObjects.get(0)); //we check only the parent object (i.e. the first one)
                break;
            case DB_CHECK_RECORD_NOT_POOLED:
                checkIfRecordIsNotPolled(dbObjects.get(0)); //we check only the parent object (i.e. the first one)
                break;
            default:
                String msg = "Invalid operation name: " + operation.getName();
                logger.error(msg);
                cor.addMsg(msg);
                throw new DatabaseComponentException(msg);
        }
    }

    /**
     * Based on the configuration found for the <code>DbObject</code> in config.xml generates an insert statement.
     * It will either contain random values based on the column types (take directly from the database), 
     * custom values from config.xml or combination of both.
     * 
     * @param object the object (table) to generate INSERT for
     * @param file file, where the statement will be stored on disk
     * @throws DatabaseComponentException if error occurs during the statement generation
     */
    private void generateInsertStatement(DbObject object, File file) throws DatabaseComponentException {
        ProgressMonitor.init(7, "Getting connection...");
        Connection conn = getConnection();
        try {
            StatementGenerator.generateInsertStatement(conn, dbObjectConfigs.get(object), file);
        } finally {
            logger.trace("Generation of insert statement finished with result: " + OperationResult.getInstance().isSuccessful());
            closeConnection(conn);
        }
    }

    /**
     * Executes the INSERT statement found in the supplied <code>file</code>
     * @param file file containing the INSERT statement
     * @throws DatabaseComponentException if the INSERT execution fails
     */
<<<<<<< HEAD
    private void executeInsertFromFile(DbObject object, File file) throws DatabaseComponentException {
=======
    private void executeInsertFromFile(File file) throws DatabaseComponentException {
>>>>>>> 7c2802d5d20e30d5191a0f8f327cacd09e189422
        ProgressMonitor.increment("Getting connection...");
        Connection conn = getConnection();
        try {
            StatementExecutor se = new StatementExecutor();
<<<<<<< HEAD
            String rowId = se.runScript(conn, file);
            
            dbObjectInsertRowIds.put(object, rowId);
=======
            se.runScript(conn, file);
>>>>>>> 7c2802d5d20e30d5191a0f8f327cacd09e189422
        } finally {
            logger.trace("Insert finished with result: " + OperationResult.getInstance().isSuccessful());
            closeConnection(conn);
        }
    }

    /**
     * Constructs the standard Oracle JDBC URL: jdbc:oracle:thin:@<code>hostName</code>:<code>port</code>&lt;delimiter&gt;<code>service</code>
     * The delimiter is determined from the <code>isSID</code> value
     * @param hostName name or IP address of the database server
     * @param port database port (e.g. 1521)
     * @param isSID if service is database SID, ':' will be used as a delimiter between port and service,
     *              otherwise '/' will be used (expecting the <code>service</code> is a database service name
     * @param service Oracle SID / Oracle Service Name
     * @return Oracle JDBC URL as <code>String</code>
     */
    public static String constructJdbcUrl(String hostName, int port, boolean isSID, String service) {
        logger.trace("Constructing JDBC URL...");
        final String jdbcUrl = String.format("jdbc:oracle:thin:@%s:%s%s%s", hostName, port, isSID ? ":" : "/", service);
        logger.trace("JDBC URL constructed: " + jdbcUrl);
        return jdbcUrl;
    }

    /**
     * Closes the SQL connection <code>conn</code>. This method does not throw any exception.
     * If the closing fails, the error message will be logged in both logging framework
     * and in the <code>OperationResult</code> object.
     * 
     * @param conn the connection to close 
     */
    public static void closeConnection(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
                logger.trace("SQL connection closed.");
            } else {
                logger.trace("SQL connection is null. Attempt to close it ignored.");
            }
        } catch (SQLException ex) {
            final String msg = "The SQL connection connection cannot be closed due to: " + Utils.getSQLExceptionMessage(ex);
            logger.warn(msg, ex);
            cor.addMsg(msg);
        }
    }

    /**
     * Tries to obtain the database connection from the <code>DriverManager</code> and returns it.
     * @return the database connection
     * @throws DatabaseComponentException if there was an error while getting the database connection
     */
    private Connection getConnection() throws DatabaseComponentException {
        try {
            final Connection conn = DriverManager.getConnection(jdbcUrl, userName, password);
            final String msg = "SQL connection obtained.";
            logger.debug(msg);
            cor.addMsg(msg, null);
            return conn;
        } catch (SQLException ex) {
            String msg = String.format("Could not get connection for %s/%s using URL: %s. %s", userName, "********", jdbcUrl, Utils.getSQLExceptionMessage(ex));
            cor.addMsg(msg);
            throw new DatabaseComponentException(msg, ex);
        }
    }

    /**
     * Deletes the entry from the database table represented by the <code>object</code>.
     *  
     * @param object xml representation of te table from the config.xml
     * @throws DatabaseComponentException if the deletion fails
     */
    private void deleteRecord(DbObject object) throws DatabaseComponentException {
        ProgressMonitor.increment("Getting connection...");
        Connection conn = getConnection();
        ProgressMonitor.increment("Preparing statement...");
        PreparedStatement stmt = prepareStatement(conn, SQL_COMMAND.DELETE, POLLING_DONT_USE, object);
        try {
            ProgressMonitor.increment("Executing statement...");
            logger.debug("Trying to delete records in source table ");
            int updateCount = stmt.executeUpdate();
            final String msg = "Records deleted in database '" + jdbcUrl + "'. Number of affected records: " + updateCount;
            logger.info(msg);
            cor.addMsg(msg);
            cor.markSuccessful();
        } catch (SQLException ex) {
            String msg = String.format("Could not execute SQL delete statement due to: %s", ex.getMessage());
            cor.addMsg(msg);
            throw new DatabaseComponentException(msg, ex);
        } finally {
            logger.trace("Deletion of record resulted in: " + cor.isSuccessful());
            close(null, stmt, conn);
        }
    }

    /**
     * Checks whether the record from the table represented by <code>object</code> was polled.
     * If exactly one record is found, the operation is marked as successful (<code>OperationResult</code> object is set to successful)
     * @param object table taken from the config.xml
     * @throws DatabaseComponentException If either 0 or more than 1 records are found
     */
    private void checkIfRecordIsPolled(DbObject object) throws DatabaseComponentException {
        ProgressMonitor.init(3, "Getting connection...");
        Connection conn = getConnection();
        ProgressMonitor.increment("Preparing statement...");
        PreparedStatement stmt = prepareStatement(conn, SQL_COMMAND.SELECT, POLLING_USE_POLLED_VALUE, object);
        
        ResultSet rs = null;
        try {
            ProgressMonitor.increment("Executing statement...");
            rs = stmt.executeQuery();
            int cnt = 0;
            while (rs.next()) {
                cnt++;
            }
            String msg;
            if (cnt == 1) {
<<<<<<< HEAD
                msg = "One record looks to be polled.";
=======
                msg = "One record looks to be polled." + stmt.toString();
>>>>>>> 7c2802d5d20e30d5191a0f8f327cacd09e189422
                logger.info(msg);
                cor.addMsg(msg);
                cor.markSuccessful();
            } else {
                if (cnt == 0) {
<<<<<<< HEAD
                    msg = "Database record has not been polled.";
=======
                    msg = "Database record has not been polled";
>>>>>>> 7c2802d5d20e30d5191a0f8f327cacd09e189422
                } else {
                    msg = "It looks like more than one polled record has been found.\n"
                            + "It can be caused by wrong configuration of the insert statement custom values, polling column or its value, etc. look into config.xml.";
                }
                cor.addMsg(msg);
                throw new DatabaseComponentException(msg);
            }
        } catch (SQLException ex) {
            String msg = String.format("Could not execute SQL statement in database '" + jdbcUrl + "' due to: %s", ex.getMessage());
            cor.addMsg(msg);
            throw new DatabaseComponentException(msg, ex);
        } finally {
            logger.trace("Check for polled record in source database '" + jdbcUrl + "'resulted in: " + cor.isSuccessful());
            close(rs, stmt, conn);
        }
    }

    /**
     * Tries to close any of the non-null parameters. Null parameters are ignored. No exception is 
     * @param rs result set to close
     * @param statement statement to close
     * @param conn sql connection to close
     */
    public static void close(ResultSet rs, Statement statement, Connection conn) {
        closeResultSet(rs);
        closeStatement(statement);
        closeConnection(conn);
    }

    /**
     * Tries to confirm that the record was not polled. If the record was not polled, it is considered as success (i.e. negative testing)
     * @param object database table from interface config.xml
     * @throws DatabaseComponentException if there is some error when getting connection or executing the statement
     */
    private void checkIfRecordIsNotPolled(DbObject object) throws DatabaseComponentException {
        ProgressMonitor.init(3, "Getting connection...");
        Connection conn = getConnection();
        ProgressMonitor.increment("Preparing statement...");
        PreparedStatement stmt = prepareStatement(conn, SQL_COMMAND.SELECT, POLLING_USE_CUSTOM_VALUE, object);
        ResultSet rs = null;
        try {
            ProgressMonitor.increment("Executing statement...");
            rs = stmt.executeQuery();
            if (rs.next()) {
                final String msg = "Unpolled record in the source database found - that was expected";
                cor.addMsg(msg);
                logger.info(msg);
                cor.markSuccessful();
            }
        } catch (SQLException ex) {
            final String msg = String.format("Could not execute SQL statement due to: %s", ex.getMessage());
            cor.addMsg(msg);
            throw new DatabaseComponentException(msg, ex);
        } finally {
            logger.trace("Check for polled record in source database resulted in: " + cor.isSuccessful());
            close(rs, stmt, conn);
        }
    }
    
    /**
     * Closes the <code>statement</code> object silently, without any exception
     * @param statement SQL statement
     */
    public static void closeStatement(Statement statement)  {
        try {
            if (statement != null) {
                statement.close();
                logger.trace("SQL statement closed.");
            } else {
                logger.trace("SQL statement is null. Attempt to close it ignored.");
            }
        } catch (SQLException ex) {
            final String msg = "The SQL statement cannot be closed due to: " + Utils.getSQLExceptionMessage(ex);
            logger.warn(msg, ex);
            cor.addMsg(msg);
        }
    }

    /**
     * Returns either SELECT or DELETE statement based on the <code>sqlCommand</code>.
     * If <code>usePolled</code> is true, then the WHERE clause will contain the polled column as well, otherwise it will
     * contain only the columns representing the sourceEntityId and columns representing the sourceMessageId
     * 
     * @param conn database connection used for creating the statement
     * @param sqlCommand enum value, can contain either SELECT or DELETE
     * @param usePolled whether to use the polled column in the WHERE clause
     * @param object the object (table) to generate the statement for
     * @return the <code>PreparedStatement</code> object
     * @throws DatabaseComponentException if error occurs during the preparation of the statement
     */
    private PreparedStatement prepareStatement(Connection conn, SQL_COMMAND sqlCommand, Boolean usePolled, DbObject object) throws DatabaseComponentException {
        StringBuilder sb = new StringBuilder();
        switch(sqlCommand) {
            case SELECT:
                sb.append("SELECT * \n");
                break;
            case DELETE: 
                sb.append("DELETE \n");
                break;
            default:
                final String msg = "Don't know how to handle " + sqlCommand + " SQL command";
                cor.addMsg(msg);
                throw new DatabaseComponentException(msg);
        }
        DbObjectConfig dbConfig = dbObjectConfigs.get(object);
        DbObjectConfig parentDbConfig = dbObjectConfigs.get(parentDbObject);
        sb.append(" FROM ").append(object.getName()).append("\n");
        sb.append(" WHERE ");
<<<<<<< HEAD
        String rowId = dbObjectInsertRowIds.get(object);
        if (rowId != null) {
           sb.append("rowid = ?");
        } else {
            String[] cols = parentDbConfig.getSourceEntityIdColumns();
            sb.append(cols[0]).append(" = ?").append("\n");
            for (int i = 1; i < cols.length; i++) {
                sb.append(" AND ").append(cols[i]).append(" = ?").append("\n");
            }
            cols = parentDbConfig.getSourceMessageIdColumns();
            for (String col : cols) {
                sb.append(" AND ").append(col).append(" = ?").append("\n");
            }
=======
        String[] cols = parentDbConfig.getSourceEntityIdColumns();
        sb.append(cols[0]).append(" = ?").append("\n");
        for (int i = 1; i < cols.length; i++) {
            sb.append(" AND ").append(cols[i]).append(" = ?").append("\n");
        }
        cols = parentDbConfig.getSourceMessageIdColumns();
        for (String col : cols) {
            sb.append(" AND ").append(col).append(" = ?").append("\n");
>>>>>>> 7c2802d5d20e30d5191a0f8f327cacd09e189422
        }
        if (usePolled != null) {
            sb.append(" AND ").append(dbConfig.getPolledColumnName()).append(" = ?");
        }
        String sql = sb.toString();
                
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
            int i = 1;
<<<<<<< HEAD
            if (rowId != null) {
               stmt.setString(i++, rowId);
               int idx = sb.indexOf("?");
               sb.replace(idx, idx + 1, "'" + rowId.replace("'", "''") + "'");
            } else {
                for (String col : parentDbConfig.getSourceEntityIdColumns()) {
                    final String val = dbConfig.getColumnValue(col);
                    stmt.setString(i++, val);
                    int idx = sb.indexOf("?");
                    sb.replace(idx, idx + 1, "'" + val.replace("'", "''") + "'");
                }

                for (String col : parentDbConfig.getSourceMessageIdColumns()) {
                    final String val = dbConfig.getColumnValue(col);
                    stmt.setString(i++, val);
                    int idx = sb.indexOf("?");
                    sb.replace(idx, idx + 1, "'" + val.replace("'", "''") + "'");
                }
=======
            for (String col : parentDbConfig.getSourceEntityIdColumns()) {
                final String val = dbConfig.getColumnValue(col);
                stmt.setString(i++, val);
                int idx = sb.indexOf("?");
                sb.replace(idx, idx + 1, "'" + val.replace("'", "''") + "'");
            }
            
            for (String col : parentDbConfig.getSourceMessageIdColumns()) {
                final String val = dbConfig.getColumnValue(col);
                stmt.setString(i++, val);
                int idx = sb.indexOf("?");
                sb.replace(idx, idx + 1, "'" + val.replace("'", "''") + "'");
>>>>>>> 7c2802d5d20e30d5191a0f8f327cacd09e189422
            }
            
            if (usePolled != null) {
                final String val = usePolled ? dbConfig.getPolledColumnPolledValue() : dbConfig.getPolledColumnCustomValue();
                stmt.setString(i, val);
                int idx = sb.indexOf("?");
                sb.replace(idx, idx + 1, "'" + val.replace("'", "''") + "'");
            }
            String msg = "Created prepared statement.";
            cor.addMsg(msg);
            logger.info(msg);
            msg = "The underlying SQL is:\n"
                + sb.toString();
            cor.addMsg(msg);
            logger.trace(msg);
            return stmt;
        } catch (SQLException ex) {
            closeStatement(stmt);
            final String msg = String.format("Failed to prepare statement due to: %s", ex.getMessage());
            cor.addMsg(msg);
            throw new DatabaseComponentException(msg, ex);
        }
    }

    @Override
    protected void destructComponent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Closes the <code>rs</code> object silently, without any exception
     * @param rs SQL result set
     */
    public static void closeResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
                logger.trace("SQL result set closed.");
            } else {
                logger.trace("SQL result set is null. Attempt to close it ignored.");
            }
        } catch (SQLException ex) {
            final String msg = "The SQL result set cannot be closed." + Utils.getSQLExceptionMessage(ex);
            logger.warn(msg , ex);
            cor.addMsg(msg);
        }
    }
    
    /**
     * Helper class that constructs all of the DbObject metadata required for running statements on this DbObject.
     * It is further used in StatementGenerator class.
     */
    public static class DbObjectConfig {
        private Map<String, DbObject.CustomValue > customValuesMap = new HashMap<>();
        private String polledColumnName;
        private String polledColumnPolledValue;
        private final String dbObjectName;
        private final String[] sourceEntityIdColumns;
        private final String[] sourceMessageIdColumns;
        
        /**
         * Determines all metadata for this <code>DbObject</code> like polled column, what are the polled/unpolled values
         * sourceEntityId columns and sourceMessageId columns and holds a map of column names and their respective column objects
         * taken from interface config.xml
         * @param object DB table
         * @param isParent whether this DbObject is parent (in a DB relationship sense)
         * @throws DatabaseComponentException 
         */
        public DbObjectConfig(DbObject object, boolean isParent) throws DatabaseComponentException {
            OperationResult cor = OperationResult.getInstance();
            dbObjectName = object.getName();
            List<DbObject.CustomValue> customValues = object.getCustomValue();
            List<String> sourceEntityIdColumnList = new ArrayList<>();
            List<String> sourceMessageIdColumnList = new ArrayList<>();
            if(customValues != null) {
                for (DbObject.CustomValue cusVal : customValues) {
                    final String name = cusVal.getColumnName().toUpperCase();
<<<<<<< HEAD
                    String value = cusVal.getColumnValue();
                    cusVal.setColumnValue("null".equalsIgnoreCase(value) ? "null" : value);
=======
                    final String value = cusVal.getColumnValue();
                    cusVal.setColumnValue(value.equalsIgnoreCase("null") ? "null" : value);
>>>>>>> 7c2802d5d20e30d5191a0f8f327cacd09e189422
                    customValuesMap.put(name, cusVal);
                    
                    Boolean bSEId = cusVal.isSourceEntityId();
                    if (TRUE.equals(bSEId)) {
                        sourceEntityIdColumnList.add(name);
                    }

                    Boolean bSMId = cusVal.isSourceMessageId();
                    if (TRUE.equals(bSMId)) {
                        sourceMessageIdColumnList.add(name);
                    }

                    String polledValue = cusVal.getPolledValue();
                    if (polledValue != null) {
                        if (polledColumnName != null) {
                            final String msg = "There is already a column " + polledColumnName + " defined for polling, yet another was found: " + name;
                            cor.addMsg(msg);
                            throw new DatabaseComponentException(msg);
                        }
                        polledColumnName = name;
                        polledColumnPolledValue = polledValue;
                    }
                }
            }
            if(isParent) {
                if (sourceEntityIdColumnList.isEmpty()) {
                    final String msg = "sourceEntityId attribute not configured in any of the custom values for the table " + object.getName();
                    cor.addMsg(msg);
                    throw new DatabaseComponentException(msg);
                }
                if (sourceMessageIdColumnList.isEmpty()) {
                    final String msg = "sourceMessageId attribute not configured in any of the custom values for the table " + object.getName();
                    cor.addMsg(msg);
                    throw new DatabaseComponentException(msg);
                }
            }
            sourceEntityIdColumns = sourceEntityIdColumnList.toArray(new String[sourceEntityIdColumnList.size()]);
            sourceMessageIdColumns = sourceMessageIdColumnList.toArray(new String[sourceMessageIdColumnList.size()]);
            customValuesMap = Collections.unmodifiableMap(customValuesMap);
        }

        /**
         * 
         * @return Map of column names and XML CustomValue objects
         */
        public Map<String, DbObject.CustomValue> getCustomValuesMap() {
            return customValuesMap;
        }

        /**
         * 
         * @return name of the column that is used in polling 
         */
        public String getPolledColumnName() {
            return polledColumnName;
        }
        
        /**
         * 
         * @return custom value that was set for the polled column in 
         */
        public String getPolledColumnCustomValue() {
            final DbObject.CustomValue polledColumn = getCustomValuesMap().get(getPolledColumnName());
            if(!Utils.isEmpty(polledColumn)) {
                return polledColumn.getColumnValue();
            }
            return null;
        }

        /**
         *
         * @return value that marks the DB record as already polled
         */
        public String getPolledColumnPolledValue() {
            return polledColumnPolledValue;
        }

        /**
         * 
         * @return DB table name as defined in interface config.xml
         */
        public String getDbObjectName() {
            return dbObjectName;
        }

        /**
         * 
         * @return an array of columns that are used as sourceEntityId
         */
        public String[] getSourceEntityIdColumns() {
            return sourceEntityIdColumns;
        }

        /**
         * 
         * @return an array of columns that are used as sourceMessageId
         */
        public String[] getSourceMessageIdColumns() {
            return sourceMessageIdColumns;
        }
        
        /**
         * 
         * @param colName column we want to get the custom value for
         * @return the custom value for the <code>colName</code>
         */
        public String getColumnValue(String colName) {
            return getCustomValuesMap().get(colName).getColumnValue();
        }
    }
}

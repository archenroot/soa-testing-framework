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

/**
 * Component used for task related to databases. Basically CRUD operation, but 
 * not restricted to. Currently only Oracle Database is supported, but extending for 
 * any JDBC compatible database is scheduled.
 * @author Ladislav Jech <archenroot@gmail.com>
 */
import com.ibm.soatf.FrameworkException;
import com.ibm.soatf.FrameworkExecutionException;
import com.ibm.soatf.component.SOATFCompType;
import com.ibm.soatf.component.AbstractSOATFComponent;
import com.ibm.soatf.config.iface.db.DBConfig;
import com.ibm.soatf.config.iface.db.DbObject;
import com.ibm.soatf.config.master.Databases.Database.DatabaseInstance;
import com.ibm.soatf.config.master.Operation;
import com.ibm.soatf.flow.OperationResult;
import com.ibm.soatf.tool.FileSystem;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
public class DatabaseComponent extends AbstractSOATFComponent {

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

    private final DBConfig dbIfaceConfig;

    // Only oracle database is supported now
    private final String driverClassName = "oracle.jdbc.driver.OracleDriver";
    private String hostName;
    private int port;
    private String userName;
    private String password;
    private String serviceId;

    private List<DbObject> dbObjects = new ArrayList<>();
    private String jdbcUrl;

    private final DatabaseInstance databaseMasterConfig;
    
    private final OperationResult cor;
    
    private Map<DbObject, DbObjectConfig> dbObjectConfigs;
    private DbObject parentDbObject;

    private static enum SQL_COMMAND {
        SELECT,
        DELETE
    }

    public DatabaseComponent(
            DatabaseInstance databaseMasterConfig,
            DBConfig dbIfaceConfig,
            File workingDir) throws FrameworkExecutionException {
        super(SOATFCompType.DATABASE);
        this.databaseMasterConfig = databaseMasterConfig;
        this.dbIfaceConfig = dbIfaceConfig;
        this.workingDir = workingDir;
        cor = OperationResult.getInstance();
        constructComponent();
    }

    /**
     * Method for database component construction.
     * @throws com.ibm.soatf.FrameworkExecutionException
     */
    @Override
    protected final void constructComponent() throws FrameworkExecutionException {
        try {
            logger.trace("Constructing DatabaseComponent object.");
            hostName = databaseMasterConfig.getHostName();
            port = databaseMasterConfig.getPort();
            userName = databaseMasterConfig.getUserName();
            password = databaseMasterConfig.getPassword();
            serviceId = databaseMasterConfig.getServiceId();
            dbObjects = dbIfaceConfig.getDbObjects().getDbObject();
            parentDbObject = dbObjects.get(0);
            dbObjectConfigs = createConfigMap(dbObjects);
            jdbcUrl = constructJdbcUrl(hostName, port, serviceId);
            Class.forName(driverClassName);
        } catch (ClassNotFoundException ex) {
            String msg = String.format("Cannot load jdbc driver class: %s", ex.getMessage());
            cor.addMsg(msg);
            throw new FrameworkExecutionException(msg, ex);
        }
        logger.trace("Constructing DatabaseComponent finished.");
    }
    
    private Map<DbObject, DbObjectConfig> createConfigMap(List<DbObject> dbObjects) throws FrameworkExecutionException {
        Map<DbObject, DbObjectConfig> map = new HashMap<>();
        boolean isParent = true;
        for(DbObject object : dbObjects) {
            map.put(object, new DbObjectConfig(object, isParent));
            isParent = false; //only the very first DbObject is considered parent
        }
        return map;
    }

    /**
     * Gets list of statement files generated for specific endpoint.
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
     * @param operation operation to execute
     * @throws com.ibm.soatf.FrameworkExecutionException
     */
    @Override
    protected void executeOperation(Operation operation) throws FrameworkException {
        /* 
         // This block will be reimplemented after there will be created subclasses on XSD level.
         if (!DATABASE_OPERATIONS.contains(operation)) {
         final String msg = "Unsupported operation: " + componentOperation + ". Valid operations are: " + DATABASE_OPERATIONS;
         logger.error(msg);
         cor.addMsg(msg);
         cor.setOverallResultSuccess(false);
         } else {
         */
        switch (operation.getName()) {
            case DB_INSERT_RECORD:
                for (DbObject object : dbObjects) {
                    final String filename = new StringBuilder(dbIfaceConfig.getRefId().replace("/", "_")).append(NAME_DELIMITER).append(object.getName()).append(INSERT_FILE_SUFFIX).toString();
                    final File file = new File(workingDir, filename);
                    generateInsertStatement(object, file);
                    executeInsertFromFile(file);
                }
                break;
            case DB_DELETE_RECORD:
                //due to possible foreign keys we need to delete the objects in reversed order
                //we also presume that the "parent" table is the first object defined in the config
                //and the rest are "child" objects
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
                throw new FrameworkExecutionException(msg);
        }
    }

    private void generateInsertStatement(DbObject object, File file) throws FrameworkException {
        Connection conn = getConnection();
        try {
            StatementGenerator.generateInsertStatement(conn, dbObjectConfigs.get(object), file);
        } finally {
            logger.trace("Generation of insert statement finished with result: " + OperationResult.getInstance().isSuccessful());
            closeConnection(conn);
        }
    }

    private void executeInsertFromFile(File file) throws FrameworkException {
        Connection conn = getConnection();
        try {
            StatementExecutor.runScript(conn, file);
        } finally {
            logger.trace("Insert finished with result: " + OperationResult.getInstance().isSuccessful());
            closeConnection(conn);
        }
    }

    public static String constructJdbcUrl(String hostName, int port, String serviceId) {
        logger.trace("Constructing JDBC URL...");
        return String.format("jdbc:oracle:thin:@%s:%s:%s", hostName, port, serviceId);
    }

    public static void closeConnection(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
            logger.trace("SQL connection closed.");
        } catch (SQLException ex) {
            logger.error("The database connection cannot be closed due to: " + Utils.getSQLExceptionMessage(ex));
        }
    }

    private Connection getConnection() throws FrameworkExecutionException {
        try {
            final Connection conn = DriverManager.getConnection(jdbcUrl, userName, password);
            final String msg = "SQL connection obtained.";
            logger.debug(msg);
            cor.addMsg(msg);
            return conn;
        } catch (SQLException ex) {
            String msg = String.format("Could not get connection for %s/%s using URL: %s. %s", userName, "********", jdbcUrl, Utils.getSQLExceptionMessage(ex));
            cor.addMsg(msg);
            throw new FrameworkExecutionException(msg, ex);
        }
    }

    private void deleteRecord(DbObject object) throws FrameworkException {
        Connection conn = getConnection();
        PreparedStatement stmt = prepareStatement(conn, SQL_COMMAND.DELETE, POLLING_DONT_USE, object);
        try {
            int updateCount = stmt.executeUpdate();
            final String msg = "Records deleted in database '" + jdbcUrl + "'. Number of affected records: " + updateCount;
            logger.info(msg);
            cor.addMsg(msg);
            cor.markSuccessful();
        } catch (SQLException ex) {
            String msg = String.format("Could not execute SQL delete statement due to: %s", ex.getMessage());
            cor.addMsg(msg);
            throw new FrameworkExecutionException(msg, ex);
        } finally {
            logger.trace("Deletion of record resulted in: " + cor.isSuccessful());
            release(null, stmt, conn);
        }
    }

    private void checkIfRecordIsPolled(DbObject object) throws FrameworkExecutionException {
        Connection conn = getConnection();
        PreparedStatement stmt = prepareStatement(conn, SQL_COMMAND.SELECT, POLLING_USE_POLLED_VALUE, object);
        
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery();
            int cnt = 0;
            while (rs.next()) {
                cnt++;
            }
            String msg;
            if (cnt == 1) {
                msg = "One record looks to be polled." + stmt.toString();
                logger.info(msg);
                cor.markSuccessful();
            } else {
                if (cnt == 0) {
                    msg = "Database record has not been polled";
                } else {
                    msg = "It looks like more than one polled record has been found.\n"
                            + "It can be caused by wrong configuration of the insert statement custom values, polling column or its value, etc. look into config.xml.";
                }
                logger.error(msg);
            }
            cor.addMsg(msg);
        } catch (SQLException ex) {
            String msg = String.format("Could not execute SQL statement in database '" + jdbcUrl + "' due to: %s", ex.getMessage());
            cor.addMsg(msg);
            throw new FrameworkExecutionException(msg, ex);
        } finally {
            logger.trace("Check for polled record in source database '" + jdbcUrl + "'resulted in: " + cor.isSuccessful());
            release(rs, stmt, conn);
        }
    }

    private void release(ResultSet rs, Statement statement, Connection conn) {
        closeResultSet(rs);
        closeStatement(statement);
        closeConnection(conn);
    }

    private void checkIfRecordIsNotPolled(DbObject object) throws FrameworkExecutionException {
        Connection conn = getConnection();
        PreparedStatement stmt = prepareStatement(conn, SQL_COMMAND.SELECT, POLLING_USE_CUSTOM_VALUE, object);
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery();
            if (rs.next()) {
                final String msg = "Unpolled record in the source database found - that was expected";
                cor.addMsg(msg);
                logger.info(msg);
                cor.markSuccessful();
            }
        } catch (SQLException ex) {
            String msg = String.format("Could not execute SQL statement due to: %s", ex.getMessage());
            cor.addMsg(msg);
            throw new FrameworkExecutionException(msg, ex);
        } finally {
            logger.trace("Check for polled record in source database resulted in: " + cor.isSuccessful());
            release(rs, stmt, conn);
        }
    }
    
    public static void closeStatement(Statement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
            logger.trace("SQL statement closed.");
        } catch (SQLException ex) {
            logger.error("The statement cannot be closed due to: " + Utils.getSQLExceptionMessage(ex));
        }
    }

    private PreparedStatement prepareStatement(Connection conn, SQL_COMMAND sqlCommand, Boolean usePolled, DbObject object) throws FrameworkExecutionException {
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
                throw new FrameworkExecutionException(msg);
        }
        DbObjectConfig dbConfig = dbObjectConfigs.get(object);
        DbObjectConfig parentDbConfig = dbObjectConfigs.get(parentDbObject);
        sb.append(" FROM ").append(object.getName()).append("\n");
        sb.append(" WHERE ");
        String[] cols = parentDbConfig.getSourceEntityIdColumns();
        sb.append(cols[0]).append(" = ?").append("\n");
        for (int i = 1; i < cols.length; i++) {
            sb.append(" AND ").append(cols[i]).append(" = ?").append("\n");
        }
        cols = parentDbConfig.getSourceMessageIdColumns();
        for (String col : cols) {
            sb.append(" AND ").append(col).append(" = ?").append("\n");
        }
        if (usePolled != null) {
            sb.append(" AND ").append(dbConfig.getPolledColumnName()).append(" = ?");
        }
        String sql = sb.toString();
                
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
            int i = 1;
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
            String msg = String.format("Failed to prepare statement due to: %s", ex.getMessage());
            cor.addMsg(msg);
            throw new FrameworkExecutionException(msg, ex);
        }
    }

    @Override
    protected void destructComponent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static void closeResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            logger.trace("SQL result set closed.");
        } catch (SQLException ex) {
            logger.error("The result set cannot be closed due to: " + Utils.getSQLExceptionMessage(ex));
        }
    }
    
    public static class DbObjectConfig {
        private Map<String, String> customValuesMap = new HashMap<>();
        private String polledColumnName;
        private String polledColumnPolledValue;
        private final String dbObjectName;
        private final String[] sourceEntityIdColumns;
        private final String[] sourceMessageIdColumns;
        
        public DbObjectConfig(DbObject object, boolean isParent) throws FrameworkExecutionException {
            OperationResult cor = OperationResult.getInstance();
            dbObjectName = object.getName();
            List<DbObject.CustomValue> customValues = object.getCustomValue();
            List<String> sourceEntityIdColumnList = new ArrayList<>();
            List<String> sourceMessageIdColumnList = new ArrayList<>();
            if(customValues != null) {
                for (DbObject.CustomValue cusVal : customValues) {
                    final String name = cusVal.getColumnName().toUpperCase();
                    final String value = cusVal.getColumnValue();
                    customValuesMap.put(name, value.equalsIgnoreCase("null") ? "null" : value);
                    
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
                            throw new FrameworkExecutionException(msg);
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
                    throw new FrameworkExecutionException(msg);
                }
                if (sourceMessageIdColumnList.isEmpty()) {
                    final String msg = "sourceMessageId attribute not configured in any of the custom values for the table " + object.getName();
                    cor.addMsg(msg);
                    throw new FrameworkExecutionException(msg);
                }
            }
            sourceEntityIdColumns = sourceEntityIdColumnList.toArray(new String[sourceEntityIdColumnList.size()]);
            sourceMessageIdColumns = sourceMessageIdColumnList.toArray(new String[sourceMessageIdColumnList.size()]);
            customValuesMap = Collections.unmodifiableMap(customValuesMap);
        }

        public Map<String, String> getCustomValuesMap() {
            return customValuesMap;
        }

        public String getPolledColumnName() {
            return polledColumnName;
        }
        
        public String getPolledColumnCustomValue() {
            return getCustomValuesMap().get(getPolledColumnName());
        }

        public String getPolledColumnPolledValue() {
            return polledColumnPolledValue;
        }

        public String getDbObjectName() {
            return dbObjectName;
        }

        public String[] getSourceEntityIdColumns() {
            return sourceEntityIdColumns;
        }

        public String[] getSourceMessageIdColumns() {
            return sourceMessageIdColumns;
        }
        
        public String getColumnValue(String colName) {
            return getCustomValuesMap().get(colName);
        }
    }
}

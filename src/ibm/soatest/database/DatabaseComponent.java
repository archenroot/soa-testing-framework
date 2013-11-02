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
package ibm.soatest.database;

import ibm.soatest.CompOperResult;
import ibm.soatest.SOATFComponent;
import ibm.soatest.SOATFCompType;
import ibm.soatest.CompOperType;
import static ibm.soatest.CompOperType.DATABASE_OPERATIONS;
import static ibm.soatest.CompOperType.DB_EXECUTE_INSERT_FROM_FILE;
import static ibm.soatest.CompOperType.DB_GENERATE_INSERT_ONE_ROW_RANDOM;
import ibm.soatest.config.DatabaseConfiguration;
import ibm.soatest.config.DatabaseTypeEnum;
import ibm.soatest.config.DbObject;
import ibm.soatest.mapping.IMappingEndpoint;
import ibm.soatest.tool.FileSystem;
import ibm.soatest.util.Utils;
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
 *
 * @author zANGETSu
 */
public class DatabaseComponent extends SOATFComponent implements IMappingEndpoint {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");    

    private static final Logger logger = LogManager.getLogger(DatabaseComponent.class);
    
    private DatabaseConfiguration databaseConfiguration = null;
    private DatabaseTypeEnum databaseType;
    // Only oracle database is supported now
    private final String driverClassName = "oracle.jdbc.driver.OracleDriver";
    private String hostName;
    private BigInteger port;
    private String userName;
    private String password;
    private String serviceId;
    // Not implemented yet
    //private String connectAs
    private List<DbObject> objects = new ArrayList<DbObject>();
    private String jdbcUrl;
    public static final String INSERT_FILE_SUFFIX = "_insert.sql";
    public static final String NAME_DELIMITER = "_";

    public DatabaseComponent(DatabaseConfiguration databaseConfiguration, CompOperResult componentOperationResult) {
        super(SOATFCompType.DATABASE, componentOperationResult);
        this.databaseConfiguration = databaseConfiguration;
        constructComponent();
    }

    @Override
    protected final void constructComponent() {
        try {
            logger.debug("Constructing DatabaseComponent object.");

            this.identificator = this.databaseConfiguration.getIdentificator();
            this.databaseType = DatabaseTypeEnum.ORACLE;
            this.hostName = this.databaseConfiguration.getHostName();
            this.port = this.databaseConfiguration.getPort();
            this.userName = this.databaseConfiguration.getUserName();
            this.password = this.databaseConfiguration.getPassword();
            this.serviceId = this.databaseConfiguration.getServiceId();
            for (DbObject object : this.databaseConfiguration.getDbObjects().getDbObject()) {
                this.objects.add(object);
            }

            this.jdbcUrl = constructJdbcUrl(this.hostName, this.port, this.serviceId);

            Class.forName(this.driverClassName);
        } catch (ClassNotFoundException ex) {
            logger.error("Database driver class cannot be found: " + ex.getMessage());
        }
        logger.debug("Constructing DatabaseComponent finished.");
    }
    
    @Override
    public Iterator<File> getGeneratedFiles(String objectName) {
        String pattern = "*";
        if (objectName != null) pattern = objectName;
        String filemask = new StringBuilder(identificator).append(NAME_DELIMITER).append(pattern).append(INSERT_FILE_SUFFIX).toString();
        return FileUtils.iterateFiles(new File(Utils.getFullFilePathStr(FileSystem.CURRENT_PATH, FileSystem.DATABASE_SCRIPT_DIR)), new WildcardFileFilter(filemask), TrueFileFilter.INSTANCE);
    }

    @Override
    protected void executeOperation(CompOperType componentOperation) {
        if (!DATABASE_OPERATIONS.contains(componentOperation)) {
            final String msg = "Unsupported operation: " + componentOperation + ". Valid operations are: " + DATABASE_OPERATIONS;
            logger.error(msg);
            componentOperationResult.setResultMessage(msg);
            componentOperationResult.setOverallResultSuccess(false);
        } else {
            for (DbObject object : objects) {
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
            this.logger.error(msg);
            this.componentOperationResult.addMsg(msg);
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
            //nothing to do
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, userName, password);
    }
}

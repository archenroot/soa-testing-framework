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
import ibm.soatest.config.Database;
import ibm.soatest.config.DatabaseConfiguration;
import ibm.soatest.config.DatabaseTypeEnum;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public class DatabaseComponent extends SOATFComponent {
    private static final Logger logger = LogManager.getLogger(DatabaseComponent.class);
    
    public static Set<CompOperType> supportedOperations 
           = CompOperType.databaseOperations;
        
    private CompOperType databaseOperation = null;
    private DatabaseConfiguration databaseConfiguration = null;
    private List<Database> databaseList = null;
    private Database database = null;
    
    private String connectionName;
    private String databaseType;
    private String driverClassName;
    private String hostName;
    private BigInteger port;
    private String userName;
    private String password;
    private String serviceId;
    private String connectAs;
    private String objectName;
    private String insertSqlScriptFileName;
    private String selectSqlScriptFileName;
    private String updateSqlScriptFileName;
    private String deleteSqlScriptFileName;

    private String jdbcUrl;
    private Connection conn;
    
    private CompOperResult componentOperationResult = new CompOperResult();

    public DatabaseComponent(DatabaseConfiguration databaseConfiguration) {
        super(SOATFCompType.DATABASE);
        this.databaseConfiguration = databaseConfiguration;
        databaseList = this.databaseConfiguration.getDatabase();
        database = databaseList.get(0);
        constructComponent();
    }

    @Override
    protected final void constructComponent() {
        try {
            logger.debug("Constructing DatabaseComponent object.");
            this.connectionName = database.getIdentificator();
            this.databaseType = DatabaseTypeEnum.ORACLE.value();
            this.driverClassName = database.getDriverClassName();
            this.hostName = database.getHostName();
            this.port = database.getPort();
            this.userName = database.getUserName();
            this.password = database.getPassword();
            serviceId = database.getServiceId();
            connectAs = database.getConnectAs();
            objectName = database.getObjectName();
            insertSqlScriptFileName = database.getInsertSqlScriptFileName();
            selectSqlScriptFileName = database.getSelectSqlScriptFileName();
            updateSqlScriptFileName = database.getUpdateSqlScriptFileName();
            deleteSqlScriptFileName = database.getDeleteSqlScriptFileName();
            
            jdbcUrl = constructJdbcUrl(this.hostName, this.port, this.serviceId);
            
            Class.forName(this.driverClassName);
            
            
            this.conn = DriverManager.getConnection(
                    this.jdbcUrl, this.userName, this.password);
        
            
        } catch (ClassNotFoundException ex) {
            logger.error("Database driver class cannot be found: " + ex.getMessage());
        } catch (SQLException ex) {
            logger.error("DriverManager cannot get the connection: " + ex.getMessage());
        }
        logger.debug("Constructing DatabaseComponent finished.");
        
    }

    @Override
    public CompOperResult executeOperation(CompOperType componentOperation) {
        
        Set<CompOperType> supportedOperations = CompOperType.databaseOperations;

        if (supportedOperations.contains(componentOperation)) {
            try {
                throw new UnsupportedComponentOperation();
            } catch (UnsupportedComponentOperation ex) {
                logger.error("Component operation is not supported.");
            }
        }

        switch (componentOperation) {
            case DB_GENERATE_INSERT_ONE_ROW_RANDOM:
                generateInsertDynamicallyOneRow();
                break;
            case DB_EXECUTE_INSERT_FROM_FILE:
        try {
            executeInsertFromFile();
        } catch (IOException ex) {
            logger.error(ex.getLocalizedMessage());
        } catch (SQLException ex) {
            logger.error(ex.getLocalizedMessage());
        }
                break;
            default:
        }
        this.componentOperationResult.setOverallResult(true);
        this.componentOperationResult.setResultMessage("Database operation finished sucessfully");
        return this.componentOperationResult;
    }

    private void generateInsertDynamicallyOneRow() {
        try {
            StatementGenerator sg = new StatementGenerator(conn, objectName, insertSqlScriptFileName);
            sg.generateOneRowSampleInsertStatement();
        } catch (Exception ex) {
            logger.error(ex.getLocalizedMessage());
        }
    }

    private void executeInsertFromFile() throws FileNotFoundException, IOException, SQLException {
        StatementExecutor se = new StatementExecutor(conn, false, true);
        Reader r = new FileReader(new File(this.insertSqlScriptFileName));
        se.runScript(r);
    }

    private static String constructJdbcUrl(String hostName, BigInteger port, String serviceId) {
        return "jdbc:oracle:thin:@"
                + hostName + ":"
                + port.toString() + ":"
                + serviceId;
    }

    protected static void writeStatementToFile(String statement, String pathToFile) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(new File(pathToFile));
            fw.write(statement);
            fw.flush();
            fw.close();
        } catch (IOException ex) {
            logger.error(ex.getLocalizedMessage());
        } finally {
            try {
                fw.close();
            } catch (IOException ex) {
                logger.error(ex.getLocalizedMessage());
            }
        }

    }

    

}

package com.ibm.soatf.component.util;

import com.ibm.soatf.component.ComponentResult;
import com.ibm.soatf.flow.FlowPatternCompositeKey;
import com.ibm.soatf.FrameworkConfiguration;
import com.ibm.soatf.FrameworkRuntimeException;
import com.ibm.soatf.InterfaceConfiguration;
import com.ibm.soatf.component.SOATFComponent;
import com.ibm.soatf.component.SOATFCompType;
import com.ibm.soatf.config.iface.IfaceExecBlock;
import com.ibm.soatf.config.iface.SOATFIfaceConfig;
import com.ibm.soatf.config.iface.db.DBConfig;
import com.ibm.soatf.config.iface.db.DbObject;
import com.ibm.soatf.config.iface.db.DbObject.CustomValue;
import com.ibm.soatf.config.iface.ftp.FTPConfig;
import com.ibm.soatf.config.iface.jms.JMSConfig;
import com.ibm.soatf.config.iface.soap.SOAPConfig;
import com.ibm.soatf.config.iface.util.UTILConfig;
import com.ibm.soatf.config.master.Component;
import static com.ibm.soatf.config.master.ExecuteOn.NA;
import com.ibm.soatf.config.master.OSBReporting.OsbReportingInstance;
import com.ibm.soatf.config.master.Operation;
import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance;
import com.ibm.soatf.component.database.DatabaseComponent;
import static com.ibm.soatf.component.database.DatabaseComponent.constructJdbcUrl;
import static java.lang.Boolean.TRUE;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public class UtilityComponent extends SOATFComponent {

    private static final Logger logger = LogManager.getLogger(UtilityComponent.class);

    private OsbReportingInstance osbReportingInstance;
    private UTILConfig utilIfaceConfig;
    private FlowPatternCompositeKey fpck;

    private FTPConfig ftpCfg;
    private DBConfig dbCfg;
    private SOAPConfig soapCfg;

    private long delay;
    private String jdbcUrl;
    private String userName;
    private String password;
    private final String driverClassName = "oracle.jdbc.driver.OracleDriver";

    private String messageRefColName;
    private String messageRefColValue;
    private String entityRefColName;
    private String entityRefColValue;
    private final InterfaceConfiguration ICFG;
    private static final FrameworkConfiguration FCFG = FrameworkConfiguration.getInstance();
    private IfaceExecBlock ifaceExecBlock;

    private DBConfig dbConfig = null;
    private OracleFusionMiddlewareInstance ofmInstance = null;
    private FTPConfig ftpConfig = null;
    private JMSConfig jmsConfig = null;
    private SOAPConfig soapConfig = null;
    private DatabaseComponent dbComp = null;
    private String osbReportingDatabaseDelete;
    private Component component = null;
    private String poolingColName;
    private String poolingColValue;

    public UtilityComponent(
            IfaceExecBlock ifaceExecBlock,
            OsbReportingInstance osbReportingInstance,
            UTILConfig utilInterfaceConfig,
            ComponentResult componentOperationResult,
            FlowPatternCompositeKey ifpck) {
        super(SOATFCompType.UTIL, componentOperationResult);
        this.ifaceExecBlock = ifaceExecBlock;
        this.osbReportingInstance = osbReportingInstance;
        this.utilIfaceConfig = utilInterfaceConfig;
        this.fpck = ifpck;
        ICFG = FCFG.getInterfaceConfig(ifpck.getIfaceName());
        constructComponent();
    }

    @Override
    protected void constructComponent() {

        this.jdbcUrl = constructJdbcUrl(this.osbReportingInstance.getHostName(), this.osbReportingInstance.getPort(), this.osbReportingInstance.getServiceId());
        this.userName = this.osbReportingInstance.getUserName();
        this.password = this.osbReportingInstance.getPassword();
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException ex) {
            try {
                throw new FrameworkRuntimeException("");
            } catch (FrameworkRuntimeException ex1) {
                java.util.logging.Logger.getLogger(UtilityComponent.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }

    @Override
    public void executeOperation(Operation operation) {
        try {
            if (!operation.getExecuteOn().equals(NA)) {
                for (SOATFIfaceConfig.IfaceEndPoints.IfaceEndPoint ifaceEndPoint : ICFG.getIfaceEndPoint(ifaceExecBlock, operation.getExecuteOn())) {
                    if (ifaceEndPoint.getDatabase() != null) {
                        dbConfig = ifaceEndPoint.getDatabase();
                        component = Component.DB;
                    } else if (ifaceEndPoint.getFtpServer() != null) {
                        ftpConfig = ifaceEndPoint.getFtpServer();
                        component = Component.FTP;
                    } else if (ifaceEndPoint.getSoap() != null) {
                        if (ifaceEndPoint.getSoap().getOperationName() != null) {
                            soapConfig = ifaceEndPoint.getSoap();
                            component = Component.SOAP;
                        }
                    } else if (ifaceEndPoint.getJmsSubsystem() != null) {
                        jmsConfig = ifaceEndPoint.getJmsSubsystem();
                        component = Component.JMS;
                    }
                }
            } 
            
            this.compOperResult.setOperation(operation);

            /*if (!UTIL_OPERATIONS.contains(componentOperation)) {
             final String msg = "Unsupported operation: " + componentOperation + ". Valid operations are: " + UTIL_OPERATIONS;
             logger.error(msg);
             this.compOperResult.setResultMessage(msg);
             this.compOperResult.setOverallResultSuccess(false);
             } else {
             */
            switch (operation.getName()) {
                case UTIL_DELAY:
                    switch (component) {
                        case DB:
                            delay = utilIfaceConfig.getDelays().getWaitForDbPool();
                            break;
                        case FTP:
                            delay = utilIfaceConfig.getDelays().getWaitForFTPPool();
                            break;
                        case JMS:
                            delay = utilIfaceConfig.getDelays().getWaitForQueueMsgTransfer();
                            break;
                        default:
                            delay = utilIfaceConfig.getDelays().getWaitForErrorQueue();
                    }
                    logger.debug("Pausing thread for " + delay + " miliseconds.");
                    Thread.sleep(delay);
                    logger.trace("Resuming thread.");
                    compOperResult.setResultMessage("Process dealyed for " + delay / 1000 + " seconds.");
                    compOperResult.setOverallResultSuccess(true);
                    break;
                case UTIL_CLEAR_REPORTING:
                    prepareQueryForOSBReporting();
                    clearOSBDatabaseReporting();
                    break;
                case UTIL_CHECK_REPORTING_FOR_SUCCESS:
                    prepareQueryForOSBReporting();
                    checkOSBDBReportingForSuccess();
                    break;
                case UTIL_CHECK_REPORTING_FOR_FAILURE:
                    prepareQueryForOSBReporting();
                    checkOSBDBReportingForFailure();
                    break;
                default:
                    logger.info("Operation execution not yet implemented: " + operation.getName());
                    this.compOperResult.setResultMessage("Operation: " + operation.getName() + " is valid, but not yet implemented");
                    this.compOperResult.setOverallResultSuccess(false);
            }
        } catch (InterruptedException ex) {
            logger.fatal("Exception", ex);
        }
    }

    private void checkOSBDBReportingForFailure() {

    }

    private void checkOSBDBReportingForSuccess() {
        Connection conn = null;
        CallableStatement callableStatement = null;
        ResultSet rs = null;
        Statement statement = null;
        DbObject dbObject = null;

        try {
            conn = getConnection();
            compOperResult.setOverallResultSuccess(false);
            String getDBUSERByUserIdSql = "{call ERROR_HOSPITAL.GATHER_OSB_DATA()}";
            callableStatement = conn.prepareCall(getDBUSERByUserIdSql);
            boolean result = callableStatement.execute();
            logger.debug("Tryied to update osb reporting table with result: " + result);
            conn = getConnection();
            String command = "SELECT * " + osbReportingDatabaseDelete;
            statement = conn.createStatement();
            logger.trace("Statement to execute: " + command);
            rs = statement.executeQuery(command);
            int cnt = 0;
            while (rs.next()) {
                cnt++;
                compOperResult.setResultMessage("There has been found following records in the OSB Reporting Database system by issuing command : \n "
                        + command
                        + rs.getCursorName());
                compOperResult.setOverallResultSuccess(true);
                logger.trace("Record: " + rs.getString("CITY"));
            }
            if (cnt == 0) {
                compOperResult.setResultMessage("There has not been found any success records in the OSB Reporting Database system by issuing command: " + command);
                compOperResult.setOverallResultSuccess(false);
            }

        } catch (SQLException ex) {
            logger.fatal("Cannot connect to database", ex);
        } finally {
            if (callableStatement != null) {
                try {
                    callableStatement.close();

                } catch (SQLException ex) {
                    java.util.logging.Logger.getLogger(UtilityComponent.class
                            .getName()).log(Level.SEVERE, null, ex);
                }

                if (conn != null) {
                    try {
                        conn.close();

                    } catch (SQLException ex) {
                        java.util.logging.Logger.getLogger(UtilityComponent.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    private void clearOSBDatabaseReporting() throws FrameworkRuntimeException {
        try {
            Connection conn = getConnection();
            Statement statement = null;
            String command = "DELETE" + this.osbReportingDatabaseDelete;
            statement = conn.createStatement();
            boolean result = statement.execute(command);
            logger.trace("Deleted record in OSB reporting resulted SQL execution of: " + result);
            conn.commit();
            compOperResult.setResultMessage("OSB Database Reporting has been cleaned by issuing following command: " + command);
            compOperResult.setOverallResultSuccess(true);
        } catch (SQLException ex) {
            throw new FrameworkRuntimeException("Cannot clear OSB Reporting database.", ex);
        }
    }

    private Connection getConnection() throws FrameworkRuntimeException {
        try {
            return DriverManager.getConnection(jdbcUrl, userName, password);
        } catch (SQLException ex) {
            throw new FrameworkRuntimeException("Cannot get connection to database.", ex);
        }

    }

    private void prepareQueryForOSBReporting() throws FrameworkRuntimeException {
        osbReportingDatabaseDelete = " FROM REPORTING_EVENTS WHERE ";
        entityRefColName = "ENTITY_REF";
        messageRefColName = "MESSAGE_REF";

        switch (component) {
            case DB:
                for (CustomValue cusVal : dbConfig.getDbObjects().getDbObject().getCustomValue()) {
                    Boolean bSEId = cusVal.isSourceEntityId();
                    if (bSEId != null) {
                        if (cusVal.isSourceEntityId().equals(TRUE)) {
                            //entityRefColName = cusVal.getColumnName();
                            entityRefColValue = cusVal.getColumnValue();
                        }
                    }
                    Boolean bSMId = cusVal.isSourceMessageId();
                    if (bSMId != null) {
                        if (cusVal.isSourceMessageId().equals(TRUE)) {
                            //messageRefColName = cusVal.getColumnName();
                            messageRefColValue = cusVal.getColumnValue();
                        }
                    }

                }
                break;
            case FTP:
                entityRefColValue = ftpConfig.getFileName();
                messageRefColValue = ftpConfig.getFileName();
                break;
            case SOAP:
                //TODO
                break;
            default:
                throw new IllegalArgumentException("Unknown component type on input: " + component.value() + ".");

        }
        osbReportingDatabaseDelete += entityRefColName + "= '" + entityRefColValue + "'"
                + " AND " + messageRefColName + " = '" + messageRefColValue + "'";
    }
}

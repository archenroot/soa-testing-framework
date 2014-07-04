package com.ibm.soatf.component.util;

import com.ibm.soatf.FrameworkException;
import com.ibm.soatf.component.AbstractSoaTFComponent;
import com.ibm.soatf.component.SOATFCompType;
import com.ibm.soatf.component.database.DatabaseComponent;
import static com.ibm.soatf.component.database.DatabaseComponent.constructJdbcUrl;
import com.ibm.soatf.config.ConfigurationManager;
import com.ibm.soatf.config.FrameworkConfigurationException;
import com.ibm.soatf.config.InterfaceConfiguration;
import com.ibm.soatf.config.InterfaceConfigurationException;
import com.ibm.soatf.config.MasterConfiguration;
import com.ibm.soatf.config.MasterConfigurationException;
import com.ibm.soatf.config.iface.IfaceExecBlock;
import com.ibm.soatf.config.iface.IfaceTestScenario;
import com.ibm.soatf.config.iface.SOATFIfaceConfig.IfaceEndPoints.IfaceEndPoint;
import com.ibm.soatf.config.iface.db.DbObject;
import com.ibm.soatf.config.iface.db.DbObject.CustomValue;
import com.ibm.soatf.config.iface.soap.EnvelopeConfig;
import com.ibm.soatf.config.iface.util.UTILConfig;
import com.ibm.soatf.config.master.ExecuteOn;
import com.ibm.soatf.config.master.OSBReporting;
import com.ibm.soatf.config.master.OSBReporting.OsbReportingInstance;
import com.ibm.soatf.config.master.Operation;
import com.ibm.soatf.config.master.Project;
import com.ibm.soatf.flow.FlowExecutor;
import com.ibm.soatf.flow.FrameworkExecutionException;
import com.ibm.soatf.flow.OperationResult;
import com.ibm.soatf.gui.ProgressMonitor;
import com.ibm.soatf.tool.Utils;
import com.ibm.soatf.tool.ZipUtils;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import static java.lang.Boolean.TRUE;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility component is not strictly related to any technology. So it simply do
 * operations which not related to any fusion middleware endpoint technology, or
 * it is working over multiple endpoints within one operation.
 *
 * @author Ladislav Jech <archenroot@gmail.com>
 */
public class UtilityComponent extends AbstractSoaTFComponent {

    private static final Logger logger = LogManager.getLogger(UtilityComponent.class);
    private static final MasterConfiguration MCFG = ConfigurationManager.getInstance().getMasterConfig();

    private InterfaceConfiguration ICFG;

    private final String envName;
    private final String interfaceName;
    private final OsbReportingInstance osbReportingInstance;
    private UTILConfig utilIfaceConfig;

    private String jdbcUrl;
    private String userName;
    private String password;
    private final String driverClassName = "oracle.jdbc.driver.OracleDriver";

    private final Operation operation;
    private String messageRefColName;
    private String messageRefColValue;
    private String entityRefColName;
    private String entityRefColValue;
    private String startDate;
    private String osbReportingEventsTestRunConditions;
    private SOATFCompType component = null;
    
    private final String reportingEventsTable;
    private final String gatherOSBReportsProcedure;
    private final boolean executeGatherProcedure;
    private final int gatherWaitInterval;

    private String deleteSOAWLIDataTable;
    private String deleteSOAWLIAttributeTable;
    private String deleteOSBReportingEventsTable;

    private final File rootWorkingDir;

    private String jmsMessageId;

    private StringBuilder resultMessage = new StringBuilder();
    private StringBuilder shortMessage = new StringBuilder();
    
    private IfaceTestScenario ifaceTestScenario = null;
    private IfaceExecBlock firstIfaceExecBlock =  null;

    private final OperationResult cor;

    public UtilityComponent(
            String interfaceName,
            String envName,
            InterfaceConfiguration ICFG,
            IfaceTestScenario ifaceTestScenario,
            OSBReporting.DbObjects osbDbObjects,
            OsbReportingInstance osbReportingInstance,
            UTILConfig utilInterfaceConfig,
            File rootWorkingDir,
            Operation operation) throws FrameworkConfigurationException, FrameworkExecutionException {
        super(SOATFCompType.UTIL);
        this.interfaceName = interfaceName;
        this.reportingEventsTable = osbDbObjects.getReportEventsTable().getName();
        this.gatherOSBReportsProcedure = osbDbObjects.getGatherOSBReportsProcedure().getName();
        this.gatherWaitInterval = osbDbObjects.getGatherOSBReportsProcedure().getScheduledInterval();
        this.executeGatherProcedure = osbDbObjects.getGatherOSBReportsProcedure().isForceProcedureExecution();
        this.envName = envName;
        this.ifaceTestScenario = ifaceTestScenario;
        this.osbReportingInstance = osbReportingInstance;
        this.utilIfaceConfig = utilInterfaceConfig;
        this.rootWorkingDir = rootWorkingDir;
        this.operation = operation;
        cor = OperationResult.getInstance();
        this.ICFG = ICFG;
        constructComponent();
    }
    
    
    public UtilityComponent(
            String interfaceName,
            String envName,
            OSBReporting.DbObjects osbDbObjects,
            OsbReportingInstance osbReportingInstance,
            File flowPatternInstanceDir,
            Operation operation) throws FrameworkConfigurationException, FrameworkExecutionException {
        this(interfaceName, envName, null, null, osbDbObjects, osbReportingInstance, null, flowPatternInstanceDir, operation);
    }

    @Override
    protected final void constructComponent() throws FrameworkConfigurationException, FrameworkExecutionException {
        final String hostName = osbReportingInstance.getHostName();
        final int port = osbReportingInstance.getPort();
        final boolean isSID = osbReportingInstance.getServiceId() != null;
        if (isSID) {
            jdbcUrl = constructJdbcUrl(hostName, port, isSID, osbReportingInstance.getServiceId());
        } else {
            jdbcUrl = constructJdbcUrl(hostName, port, isSID, osbReportingInstance.getServiceName());
        }
        this.userName = this.osbReportingInstance.getUserName();
        this.password = this.osbReportingInstance.getPassword();
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException ex) {
            throw new FrameworkExecutionException("The specific JDBC driver class cannot be found: " + ex);
        }

    }

    @Override
    public void executeOperation(Operation operation) throws FrameworkException {
        /**
         * Getting the component which is the test scenario first data source.
         */
        if (operation.getExecuteOn() == ExecuteOn.NA) {
            logger.debug("Current util component is not targeted to any endpoint and therefore no search for origin data source of test scenario is required. Setting component type to UTIL.");
            component = SOATFCompType.UTIL;
        } else {
            try {
                logger.debug("Getting the component type which is defined as origin data source for test scenario " + ifaceTestScenario.getRefId() + ".");
                firstIfaceExecBlock = ifaceTestScenario.getIfaceExecBlock().get(0);
                for (IfaceEndPoint ifaceEndPoint : ICFG.getIfaceEndPoint(firstIfaceExecBlock, operation.getExecuteOn())) {
                    if (ifaceEndPoint.getDatabase() != null) {
                        component = SOATFCompType.DATABASE;
                        break;
                    } else if (ifaceEndPoint.getFtpServer() != null) {
                        component = SOATFCompType.FTP;
                    } else if (ifaceEndPoint.getSoap() != null) {
                        if (ifaceEndPoint.getSoap().getOperationName() != null) {
                            component = SOATFCompType.SOAP;
                        }
                    } else if (ifaceEndPoint.getJmsSubsystem() != null) {
                        component = SOATFCompType.JMS;
                    }
                }
            } catch (InterfaceConfigurationException ex) {
                final String msg = "Error when trying to obtain the original data source component type defined for test scenario " + ifaceTestScenario.getRefId() + ".";
                cor.addMsg(msg);
                throw new UtilComponentException(msg, ex);
            }
        }
        logger.debug("Origin component is type of " +  component);

        //cor.setOperation(operation); //nastavuje konstruktor abstractoperation

        /*if (!UTIL_OPERATIONS.contains(componentOperation)) {
         final String msg = "Unsupported operation: " + componentOperation + ". Valid operations are: " + UTIL_OPERATIONS;
         logger.error(msg);
         cor.setResultMessage(msg);
         cor.setOverallResultSuccess(false);
         } else {
         */
        switch (operation.getName()) {
            case UTIL_WAIT_FOR_DB_POOLING_TRIGGER:
                logger.info("Wait for db poller for " + utilIfaceConfig.getDelays().getWaitForDbPool() / 1000 + " seconds.");
                threadSleep(utilIfaceConfig.getDelays().getWaitForDbPool());
                cor.markSuccessful();
                cor.addMsg("Test process paused for db poller.");
                break;
            case UTIL_WAIT_FOR_ENQUEUE_TO_ERROR_QUEUE:
                logger.info("Wait for error enqueue for " + utilIfaceConfig.getDelays().getWaitForErrorQueue() / 1000 + " seconds.");
                threadSleep(utilIfaceConfig.getDelays().getWaitForErrorQueue());
                cor.markSuccessful();
                cor.addMsg("Test process paused for error queue.");
                break;
            case UTIL_WAIT_FOR_FTP_POOLING_TRIGGER:
                logger.info("Wait for ftp poller for " + utilIfaceConfig.getDelays().getWaitForFTPPool() / 1000 + " seconds.");
                threadSleep(utilIfaceConfig.getDelays().getWaitForFTPPool());
                cor.markSuccessful();
                cor.addMsg("Test process paused for FTP poller.");
                break;
            case UTIL_WAIT_FOR_JMS_MESSAGE_TRANSFER:
                logger.info("Wait for jms transfer for " + utilIfaceConfig.getDelays().getWaitForQueueMsgTransfer() / 1000 + " seconds.");
                threadSleep(utilIfaceConfig.getDelays().getWaitForQueueMsgTransfer());
                cor.markSuccessful();
                cor.addMsg("Test process paused for JMS message transfer.");
                break;
            case UTIL_WAIT_FOR_FILE_POLLING_TRIGGER:
                logger.info("Wait for FILE poller for " + utilIfaceConfig.getDelays().getWaitForFilePoll() / 1000 + " seconds.");
                threadSleep(utilIfaceConfig.getDelays().getWaitForFilePoll());
                cor.markSuccessful();
                cor.addMsg("Test process paused for FILE poller.");
                break;
            case UTIL_CLEAR_REPORTING:
                initiateReporting();
                break;
            case UTIL_CHECK_REPORTING_FOR_SUCCESS:
                checkOSBDBReportingForSuccess();
                break;
            case UTIL_CHECK_REPORTING_FOR_FAILURE:
                checkOSBDBReportingForFailure();
                break;
            case UTIL_CHECK_REPORTING_FOR_NOTHING:
                checkOSBDBReportingForNothing();
                break;
            case UTIL_PRINT_OSB_REPORTING:
                loadQueryVariablesOSBReporting();
                printOSBReporting();
                break;
            case UTIL_PURGE_DUPLICATE_CONTROL:
                purgeDuplicateControlTable();
                break;
            case UTIL_ARCHIVE_RESULTS:
                archiveResults();
                break;
            case UTIL_CLEAN_RESULTS:
                cleanResults();
                break;
            default:
                logger.info("Operation execution not yet implemented: " + operation.getName());
                cor.addMsg("Operation: " + operation.getName() + " is valid, but not yet implemented");
        }

    }
    
    private void purgeDuplicateControlTable() throws UtilComponentException {
        Connection conn = null;
        Statement statement = null;
        resultMessage = new StringBuilder();
        try {
            ProgressMonitor.init(2, "Getting connection");
            conn = getConnection();
            statement = conn.createStatement();
            ProgressMonitor.increment("Truncating table");
            String command = "truncate table DUPLICATE_CONTROL";            
            logger.trace("Statement to execute: " + command);
            statement.executeUpdate(command);
            resultMessage.append("Cleaning DUPLICATE_CONTROL table successful\n");
            int updateCount = statement.getUpdateCount();
            resultMessage.append("Affected rows: ").append(updateCount);
            cor.addMsg(resultMessage.toString());
            cor.markSuccessful();
        } catch (SQLException ex) {
            final String msg = "JDBC error occured" + ex.getErrorCode() + ": " + ex.getMessage();
            cor.addMsg(msg);
            throw new UtilComponentException(msg, ex);

        } finally {
            cor.addMsg(resultMessage.toString(), shortMessage.toString());
            DatabaseComponent.close(null, statement, conn);
        }
    }

    private void checkOSBDBReportingForFailure() throws UtilComponentException {
        Connection conn = null;
        CallableStatement callableStatement = null;
        ResultSet rs;
        Statement statement;
        resultMessage = new StringBuilder();
        shortMessage = new StringBuilder();
        String projectName = null;
        
        try {
            if (executeGatherProcedure) {
                ProgressMonitor.init(3, "Getting DB connection");
                conn = getConnection();
                ProgressMonitor.increment("Invoking " + gatherOSBReportsProcedure + " procedure");
                callableStatement = conn.prepareCall("{call "+gatherOSBReportsProcedure+"()}");
                boolean result = callableStatement.execute();
                logger.debug("Tried to update osb reporting table with result: " + result);
                DatabaseComponent.closeConnection(conn);
            } else {
                logger.info("Wait for scheduled gather job for " + gatherWaitInterval + " seconds."); 
                ProgressMonitor.init(3, "Waiting for stored procedure execution: " + gatherWaitInterval + "s");
                ProgressMonitor.increment();
                for (int i = gatherWaitInterval - 1; i >= 0; i--) {
                    Thread.sleep(1000);
                    ProgressMonitor.setMsg("Waiting for stored procedure execution: " + i + "s");
                }               
            }
            loadQueryVariablesOSBReporting();            
            ProgressMonitor.increment("Invoking SELECT");
            conn = getConnection();
            String command = "SELECT * " + osbReportingEventsTestRunConditions + " ORDER BY TIME DESC";
            statement = conn.createStatement();
            logger.trace("Statement to execute: " + command);
            rs = statement.executeQuery(command);
            int cnt = 0;
            boolean entryFound = false, exitFound = false, errorFound = false;
            while (rs.next()) {
                cnt++;
                if (rs.getString("PROJECT_NAME") != null) {
                    projectName = rs.getString("PROJECT_NAME");
                }

                if (rs.getString("STATUS").equals("Entry")) {
                    entryFound = true;
                    jmsMessageId = rs.getString("JMS_MESSAGE_ID");
                }
                if (rs.getString("STATUS").equals("Exit")) {
                    if (!errorFound) exitFound = true;
                }
                if (rs.getString("STATUS").equals("Error")) {
                    if (!exitFound) errorFound = true;
                }
            }
            if ("".equals(projectName) || projectName == null) {
                projectName = "<Unknown Project - issue within OSB reporting level!!!>";
            }
            if (entryFound && !exitFound && errorFound) {
                cor.markSuccessful();
                resultMessage.append("Checking OSB reporting for FAILURE resulted in overall success by issuing command:\n");
                resultMessage.append(command).append("\n");
                resultMessage.append("Both reports of type Entry and Error found.\n");
                resultMessage.append("JMSMessageId: ").append(jmsMessageId);
                shortMessage.append("Checking OSB reporting for FAILURE resulted in overall success\n");
                shortMessage.append("Both reports of type Entry and Error found.\n");
                shortMessage.append("JMSMessageId: ").append(jmsMessageId);
                
                final String errors = getListOfErrors(conn, interfaceName, entityRefColValue, messageRefColValue, startDate);
                resultMessage.append(errors);
                shortMessage.append(errors);
                return;
            } else if (entryFound && exitFound && !errorFound) {
                resultMessage.append("Checking OSB reporting for FAILURE was not successful, check done by issuing command:\n");
                resultMessage.append(command).append("\n");
                resultMessage.append("Reports of type Entry and Exit were found.");
                shortMessage.append("Checking OSB reporting failed, the process reported error\n");
                shortMessage.append("Reports of type Entry and Exit were found.");

                final String errors = getListOfErrors(conn, interfaceName, entityRefColValue, messageRefColValue, startDate);
                resultMessage.append(errors);
                shortMessage.append(errors);
            } else {
                if (cnt == 0) {
                    resultMessage.append("There has not been found any records in the OSB Reporting Database system by issuing command:\n");
                    resultMessage.append(command);
                    shortMessage.append("There has not been found any records in the OSB Reporting Database system.");
                } else {
                    resultMessage.append("Checking OSB reporting failed, the process reports looks inconsistent, check done by issuing command:\n");
                    resultMessage.append(command).append("\n");
                    resultMessage.append("It looks like there is a problem with reporting configuration on database, osb levels, or wrong configuration on OSB proxy level.\n");
                    resultMessage.append("Check xpath and xquery implementation for project: ").append(projectName).append(".");
                    shortMessage.append("Checking OSB reporting failed, the process reports looks inconsistent\n");
                    shortMessage.append("It looks like there is a problem with reporting configuration on database, osb levels, or wrong configuration on OSB proxy level.\n");
                    shortMessage.append("Check xpath and xquery implementation for project: ").append(projectName).append(".");
                    if (errorFound) {
                        final String errors = getListOfErrors(conn, interfaceName, entityRefColValue, messageRefColValue, startDate);
                        resultMessage.append(errors);
                        shortMessage.append(errors);
                    }                    
                }
            }
            throw new UtilComponentException(shortMessage.toString());
        } catch (SQLException ex) {
            final String msg = "JDBC error occured" + ex.getErrorCode() + ": " + ex.getMessage();
            cor.addMsg(msg);
            throw new UtilComponentException(msg, ex);
        } catch (InterruptedException ex) {
            final String msg = "Delaying the process for " + gatherWaitInterval + "s failed: " + ex.getMessage();
            cor.addMsg(msg);
            throw new UtilComponentException(msg, ex);
        } finally {
            cor.addMsg(resultMessage.toString(), shortMessage.toString());
            DatabaseComponent.closeStatement(callableStatement);
            DatabaseComponent.closeConnection(conn);
        }
    }

    private void checkOSBDBReportingForSuccess() throws UtilComponentException {
        Connection conn = null;
        CallableStatement callableStatement = null;
        ResultSet rs = null;
        Statement statement = null;
        DbObject dbObject = null;
        resultMessage = new StringBuilder();
        shortMessage = new StringBuilder();
        String projectName = null;      

        try {
            if (executeGatherProcedure) {
                ProgressMonitor.init(3, "Getting DB connection");
                conn = getConnection();
                ProgressMonitor.increment("Invoking " + gatherOSBReportsProcedure + " procedure");
                callableStatement = conn.prepareCall("{call "+gatherOSBReportsProcedure+"()}");
                boolean result = callableStatement.execute();
                logger.debug("Tried to update osb reporting table with result: " + result);
                DatabaseComponent.closeConnection(conn);
            } else {
                logger.info("Wait for scheduled gather job for " + gatherWaitInterval + " seconds."); 
                ProgressMonitor.init(3, "Waiting for stored procedure execution: " + gatherWaitInterval + "s");
                ProgressMonitor.increment();
                for (int i = gatherWaitInterval - 1; i >= 0; i--) {
                    Thread.sleep(1000);
                    ProgressMonitor.setMsg("Waiting for stored procedure execution: " + i + "s");
                }               
            }
            loadQueryVariablesOSBReporting();            
            ProgressMonitor.increment("Invoking SELECT");
            conn = getConnection();
            String command = "SELECT * " + osbReportingEventsTestRunConditions + " ORDER BY TIME DESC";
            statement = conn.createStatement();
            logger.trace("Statement to execute: " + command);
            rs = statement.executeQuery(command);
            int cnt = 0;
            boolean entryFound = false, exitFound = false, errorFound = false;
            while (rs.next()) {
                cnt++;
                if (rs.getString("PROJECT_NAME") != null) {
                    projectName = rs.getString("PROJECT_NAME");
                }

                if (rs.getString("STATUS").equals("Entry")) {
                    entryFound = true;
                    jmsMessageId = rs.getString("JMS_MESSAGE_ID");
                }
                if (rs.getString("STATUS").equals("Exit")) {
                    if (!errorFound) exitFound = true;
                }
                if (rs.getString("STATUS").equals("Error")) {
                    if (!exitFound) errorFound = true;                    
                }
            }
            if ("".equals(projectName) || projectName == null) {
                projectName = "<Unknown Project - issue within OSB reporting level!!!>";
            }
            if (entryFound && exitFound && !errorFound) {
                cor.markSuccessful();
                resultMessage.append("Checking OSB reporting for SUCCESS resulted in overall success by issuing command:\n");
                resultMessage.append(command).append("\n");
                resultMessage.append("Both reports of type Entry and Exit found.\n");
                resultMessage.append("JMSMessageId: ").append(jmsMessageId);
                shortMessage.append("Checking OSB reporting for SUCCESS resulted in overall success\n");
                shortMessage.append("Both reports of type Entry and Exit found.\n");
                shortMessage.append("JMSMessageId: ").append(jmsMessageId);
                return;
            } else if (entryFound && !exitFound && errorFound) {
                resultMessage.append("Checking OSB reporting failed, the process reported error, check done by issuing command:\n");
                resultMessage.append(command).append("\n");
                resultMessage.append("Reports of type Entry and Error were found.");
                shortMessage.append("Checking OSB reporting failed, the process reported error\n");
                shortMessage.append("Reports of type Entry and Error were found.");

                final String errors = getListOfErrors(conn, interfaceName, entityRefColValue, messageRefColValue, startDate);
                resultMessage.append(errors);
                shortMessage.append(errors);
            } else {
                if (cnt == 0) {
                    resultMessage.append("There has not been found any records in the OSB Reporting Database system by issuing command:\n ");
                    resultMessage.append(command);
                    shortMessage.append("There has not been found any records in the OSB Reporting Database system.");
                } else {
                    resultMessage.append("Checking OSB reporting failed, the process reports looks inconsistent, check done by issuing command:\n");
                    resultMessage.append(command).append("\n");
                    resultMessage.append("It looks like there is a problem with reporting configuration on database, osb levels, or wrong configuration on OSB proxy level.\n");
                    resultMessage.append("Check xpath and xquery implementation for project: ").append(projectName).append(".");
                    shortMessage.append("Checking OSB reporting resulted failed, the process reports looks inconsistent\n");
                    shortMessage.append("It looks like there is a problem with reporting configuration on database, osb levels, or wrong configuration on OSB proxy level.\n");
                    shortMessage.append("Check xpath and xquery implementation for project: ").append(projectName).append(".");
                    if (errorFound) {
                        final String errors = getListOfErrors(conn,interfaceName, entityRefColValue, messageRefColValue, startDate);
                        resultMessage.append(errors);
                        shortMessage.append(errors);
                    }
                }
            }
            throw new UtilComponentException(shortMessage.toString());
        } catch (SQLException ex) {
            final String msg = "JDBC error occured" + ex.getErrorCode() + ": " + ex.getMessage();
            cor.addMsg(msg);
            throw new UtilComponentException(msg, ex);
        } catch (InterruptedException ex) {
            final String msg = "Delaying the process for " + gatherWaitInterval + "s failed: " + ex.getMessage();
            cor.addMsg(msg);
            throw new UtilComponentException(msg, ex);
        } finally {
            cor.addMsg(resultMessage.toString(), shortMessage.toString());
            DatabaseComponent.closeResultSet(rs);
            DatabaseComponent.closeStatement(callableStatement);
            DatabaseComponent.closeStatement(statement);
            DatabaseComponent.closeConnection(conn);
        }
    }

    private Connection getConnection() throws UtilComponentException {
        try {
            final String msg = "Trying to get connection to " + jdbcUrl;
            cor.addMsg(msg, null);
            return DriverManager.getConnection(jdbcUrl, userName, password);
        } catch (SQLException ex) {
            final String msg = "Cannot get connection to database." + Utils.getSQLExceptionMessage(ex);
            cor.addMsg(msg);
            throw new UtilComponentException(msg, ex);

        }

    }

    private void loadQueryVariablesOSBReporting() throws UtilComponentException {
        Connection conn = null;
        try {
            osbReportingEventsTestRunConditions = " FROM "+reportingEventsTable+" WHERE ";
            entityRefColName = "ENTITY_REF";
            messageRefColName = "MESSAGE_REF";
            //component = Component.DB;
            File path;
            StringBuilder entitySb = new StringBuilder();
            StringBuilder messageSb = new StringBuilder();
            int bSEIdCount = 0,
            bSMIdCount = 0;
            switch (component) {
                case DATABASE:

                    List<DbObject> dbObjects = ICFG.getIfaceDbObjectList(this.envName, firstIfaceExecBlock, operation.getExecuteOn());
                    if (dbObjects.isEmpty()) {
                        String msg = "There exists no Database endpoint within config.xml file "
                                + " execution block " + firstIfaceExecBlock.getRefId()
                                + " targeting " + operation.getExecuteOn().value() + ".";
                        logger.error(msg);
                        throw new UtilComponentException(msg);
                    }
                    DbObject dbObject = dbObjects.get(0);

                    for (CustomValue cusVal : dbObject.getCustomValue()) {

                        Boolean bSEId = cusVal.isSourceEntityId();
                        if (bSEId != null) {
                            bSEIdCount++;
                            if (cusVal.isSourceEntityId().equals(TRUE)) {
                                if (bSEIdCount == 1) {
                                    entitySb.append(cusVal.getColumnValue());
                                } else {
                                    entitySb.append("-").append(cusVal.getColumnValue());
                                }

                            }
                        }
                        Boolean bSMId = cusVal.isSourceMessageId();
                        if (bSMId != null) {
                            bSMIdCount++;
                            if (cusVal.isSourceMessageId().equals(TRUE)) {
                                if (bSMIdCount == 1) {

                                    messageSb.append(cusVal.getColumnValue());
                                } else {
                                    messageSb.append("-").append(cusVal.getColumnValue());
                                }

                            }
                        }

                    }

                    // Should be removed if new implementation works in all cases
                    //entityRefColValue = entitySb.deleteCharAt(entitySb.length() - 1).toString();
                    //messageRefColValue = messageSb.deleteCharAt(entitySb.length() - 1).toString();
                    entityRefColValue = entitySb.toString();
                    messageRefColValue = messageSb.toString();
                    break;
                case FTP:
                    com.ibm.soatf.config.iface.ftp.File ftpfile = ICFG.getFTPFile(this.envName, firstIfaceExecBlock, operation.getExecuteOn());
                    if (ftpfile == null) {
                        String msg = "There exists no FTP endpoint within config.xml file "
                                + " execution block " + firstIfaceExecBlock.getRefId()
                                + " targeting " + operation.getExecuteOn().value() + ".";
                        logger.error(msg);
                        throw new UtilComponentException(msg);
                    }
                    String fileName = Utils.insertTimestampToFilename(ftpfile.getFileName(), FlowExecutor.getActualRunDate());
                    int suffixPos = fileName.lastIndexOf(".");
                    entityRefColValue =  fileName.substring(0,suffixPos);
                    messageRefColValue = null;//file.getSourceMessageId();
                    /*
                     TODO
                     String fileName = ftpConfig.getFileName();
                     path = new File(rootWorkingDir, "ftp");
                     String fileContent = ftpConfig.getFileContent();
                    
                     String actualFile = FTPComponent.getFile(path, fileName, fileContent).getName();
                     if (actualFile != null && actualFile.lastIndexOf(".") > 0) {
                     actualFile = actualFile.substring(0, actualFile.lastIndexOf("."));
                     }
                     entityRefColValue = actualFile;
                     messageRefColValue = null;
                     */
                    break;
                case FILE:
                    com.ibm.soatf.config.iface.file.File file = ICFG.getFile(this.envName, firstIfaceExecBlock, operation.getExecuteOn());
                    if (file == null) {
                        String msg = "There exists no File endpoint within config.xml file "
                                + " execution block " + firstIfaceExecBlock.getRefId()
                                + " targeting " + operation.getExecuteOn().value() + ".";
                        logger.error(msg);
                        throw new UtilComponentException(msg);
                    }
                    //exclude suffix                    
                    fileName = Utils.insertTimestampToFilename(file.getFileName(), FlowExecutor.getActualRunDate());
                    suffixPos = fileName.lastIndexOf(".");
                    entityRefColValue =  fileName.substring(0,suffixPos);
                    messageRefColValue = null;//file.getSourceMessageId();
                    break;
                case SOAP:                    
                    path = new File(rootWorkingDir, "soap");
                    //SOAPConfig soapCfg = ICFG.getSoapConfig(firstIfaceExecBlock, operation.getExecuteOn());
                    EnvelopeConfig ec = ICFG.getSoapEnvelopeConfig(this.envName, firstIfaceExecBlock, operation.getExecuteOn());
                    if (ec != null) {
                        entityRefColValue = ec.getSourceEntityRef();
                        messageRefColValue = ec.getSourceMessageId();
                        if (entityRefColValue != null && messageRefColValue != null) {
                            break;
                        }
                    } 
                    List<EnvelopeConfig.Element> customVals = ICFG.getSoapEnvelopeElements(this.envName, firstIfaceExecBlock, operation.getExecuteOn());
                    for (EnvelopeConfig.Element e : customVals) {
                        Boolean bSEId = e.isSourceEntityRef();
                        if (bSEId != null && bSEId.equals(TRUE)) {
                            bSEIdCount++;
                            //final String entityRefElement = e.getElementXpath();
                            //if (entityRefElement != null) {
                                //final String entityRefColSubValue = SOAPComponent.getValueFromGeneratedEnvelope(path, soapConfig.getServiceName(), soapConfig.getOperationName(), transformXPath(entityRefElement));
                                final String entityRefColSubValue = e.getElementValue();
                                if (bSEIdCount == 1) {
                                    entitySb.append(entityRefColSubValue);
                                } else {
                                    entitySb.append("-").append(entityRefColSubValue);
                                }   
                            //}
                        } 
                        Boolean bSMId = e.isSourceMessageId();
                        if (bSMId != null && bSMId.equals(TRUE)) {
                            bSMIdCount++;                                
                            //final String messageRefElement = e.getElementXpath();
                            //if (messageRefElement != null) {
                                //final String messageRefColSubValue = SOAPComponent.getValueFromGeneratedEnvelope(path, soapConfig.getServiceName(), soapConfig.getOperationName(), transformXPath(messageRefElement));
                                final String messageRefColSubValue = e.getElementValue();
                                if (bSMIdCount == 1) {
                                    messageSb.append(messageRefColSubValue);
                                } else {
                                    messageSb.append("-").append(messageRefColSubValue);
                                }   
                            //}
                        }                        
                    }
                    if (entityRefColValue == null) {
                        entityRefColValue = entitySb.toString();
                    }
                    if (messageRefColValue == null) {
                        messageRefColValue = messageSb.toString();                    
                    }
                    break;
                case JMS:
                    //TODO
                    break;
                case UTIL:
                    break;
                default:
                    throw new IllegalArgumentException("Unknown component type on input: " + component.name() + ".");

            }
            long diff = 0;
            //if we don't have starting db timestamp, compute diff from local times
            if (FlowExecutor.getActualRunDBDate() == null) {
                Date actualEndDate =  new Date();
                diff = actualEndDate.getTime() - FlowExecutor.getActualRunDate().getTime();
            }
            startDate = FlowExecutor.getActualRunDBDateString();
            conn = getConnection();
            updateDBDate(conn);
            //apply diff to end db timestamp to compute starting db timestamp
            if (diff > 0) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(FlowExecutor.getActualRunDBDate());
                cal.add(Calendar.MILLISECOND, -(int)diff);
                startDate = String.format("TO_DATE('%s', 'YYYY/MM/DD HH24:MI:SS')", DatabaseComponent.DATE_FORMAT.format(cal.getTime()));
            }
            osbReportingEventsTestRunConditions += "time > " + startDate;
            osbReportingEventsTestRunConditions += " AND time <= " + FlowExecutor.getActualRunDBDateString();
            osbReportingEventsTestRunConditions += " AND interface = '" + interfaceName +"'";
            if (entityRefColValue != null && entityRefColValue.length() > 0) {
                osbReportingEventsTestRunConditions += " AND " + entityRefColName + "= '" + entityRefColValue + "'";
            }
            if (messageRefColValue != null && messageRefColValue.length() > 0) {
                osbReportingEventsTestRunConditions += " AND " + messageRefColName + " = '" + messageRefColValue + "'";
            }
        } catch (SQLException | InterfaceConfigurationException ex) {
            DatabaseComponent.closeConnection(conn);
            final String msg = "Error occured while trying to configure OSB reporting subsystem with origin source of type " + component.name() + ":\n" + ex.getLocalizedMessage();
            cor.addMsg(msg);
            throw new UtilComponentException(msg, ex);
        }
    }

    private void checkOSBDBReportingForNothing() throws UtilComponentException {
        Connection conn = null;
        CallableStatement callableStatement = null;
        ResultSet rs = null;
        Statement statement = null;
        DbObject dbObject = null;
        resultMessage = new StringBuilder();
        shortMessage = new StringBuilder();
        String projectName = null;
        try {
            if (executeGatherProcedure) {
                ProgressMonitor.init(3, "Getting DB connection");
                conn = getConnection();
                ProgressMonitor.increment("Invoking " + gatherOSBReportsProcedure + " procedure");
                callableStatement = conn.prepareCall("{call "+gatherOSBReportsProcedure+"()}");
                boolean result = callableStatement.execute();
                logger.debug("Tried to update osb reporting table with result: " + result);
                DatabaseComponent.closeConnection(conn);
            } else {
                logger.info("Wait for scheduled gather job for " + gatherWaitInterval + " seconds."); 
                ProgressMonitor.init(3, "Waiting for stored procedure execution: " + gatherWaitInterval + "s");
                ProgressMonitor.increment();
                for (int i = gatherWaitInterval - 1; i >= 0; i--) {
                    Thread.sleep(1000);
                    ProgressMonitor.setMsg("Waiting for stored procedure execution: " + i + "s");
                }               
            }
            loadQueryVariablesOSBReporting();            
            conn = getConnection();
            String command = "SELECT * " + osbReportingEventsTestRunConditions + " ORDER BY TIME DESC";
            statement = conn.createStatement();
            logger.trace("Statement to execute: " + command);
            rs = statement.executeQuery(command);
            int cnt = 0;
            boolean errorFound = false;
            while (rs.next()) {
                cnt++;
                if (rs.getString("STATUS").equals("Error")) {
                    errorFound = true;                    
                }
            }

            if (cnt == 0) {
                cor.markSuccessful();
                resultMessage.append("Checking OSB reporting for no reports resulted in overall success by issuing command:\n");
                resultMessage.append(command).append("\n");
                shortMessage.append("Checking OSB reporting for no reports resulted in overall success\n");
            } else {
                resultMessage.append("Checking OSB reporting failed, there were found ").append(cnt).append(" reports by issuing command:\n");
                resultMessage.append(command).append("\n");
                shortMessage.append("Checking OSB reporting failed, there were found ").append(cnt).append(" reports\n");
                if (errorFound) {
                    final String errors = getListOfErrors(conn, interfaceName, entityRefColValue, messageRefColValue, startDate);
                    resultMessage.append(errors);
                    shortMessage.append(errors);
                }                
                throw new UtilComponentException(shortMessage.toString());                
            }
        } catch (SQLException ex) {
            final String msg = "JDBC error occured" + ex.getErrorCode() + ": " + ex.getMessage();
            cor.addMsg(msg);
            throw new UtilComponentException(msg, ex);

        } catch (InterruptedException ex) {
            final String msg = "Delaying the process for " + gatherWaitInterval + "s failed: " + ex.getMessage();
            cor.addMsg(msg);
            throw new UtilComponentException(msg, ex);
        } finally {
            cor.addMsg(resultMessage.toString(), shortMessage.toString());
            if (callableStatement != null) {
                try {
                    callableStatement.close();
                } catch (SQLException ex) {
                    throw new UtilComponentException(ex);
                }
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException ex) {
                        throw new UtilComponentException(ex);
                    }
                }
            }
        }
    }
    
    private void initiateReporting() throws UtilComponentException {
        ProgressMonitor.init(3, "Getting DB connection");
        Connection connection = getConnection();
        try {
            ProgressMonitor.increment("Fetching actual DB sysdate");
            updateDBDate(connection);
            final String msg = "Actual DB sysdate is: " + DatabaseComponent.DATE_FORMAT.format(FlowExecutor.getActualRunDBDate());
            cor.addMsg(msg);
            cor.markSuccessful();
            logger.info(msg);
        } catch (SQLException ex) {
            String message = "An error occured when trying to fetch actual DB sysdate for reporting events:\n" + ex.getErrorCode() + ": " + ex.getMessage();
            cor.addMsg(message);
            throw new UtilComponentException(message);
        } finally {
            DatabaseComponent.closeConnection(connection);
        }
    }    
/*
    private void clearReporting() throws UtilComponentException {
        String command = "DELETE" + this.osbReportingEventsTestRunConditions;
        ProgressMonitor.init(6, "Getting DB connection");
        Connection connection = getConnection();
        resultMessage = new StringBuilder();
        shortMessage = new StringBuilder();
        try {
            boolean result;
            ProgressMonitor.increment("Deleting from WLI_QS_REPORT_DATA");
            StringBuilder sb = new StringBuilder();
            sb.append(" DELETE FROM WLI_QS_REPORT_DATA ");
            sb.append("WHERE MSG_GUID IN\n (\n");
            sb.append("  SELECT MSG_GUID");
            sb.append(" FROM WLI_QS_REPORT_ATTRIBUTE");
            sb.append(" WHERE MSG_LABELS LIKE '%Key=");
            sb.append(this.entityRefColValue);
            if (this.messageRefColValue != null) {
                sb.append(";SourceMessageId=");
                sb.append(this.messageRefColValue);
            }
            sb.append("%'\n )");
            deleteSOAWLIDataTable = sb.toString();
            command = deleteSOAWLIDataTable;
            Statement statement = connection.createStatement();
            result = statement.execute(deleteSOAWLIDataTable);
            logger.trace("first wli table." + result);
            resultMessage.append("Cleaning SOA infra JMS reporitng data table sucessful issuing following command:\n");
            resultMessage.append(deleteSOAWLIDataTable).append("\n");
            int updateCount = statement.getUpdateCount();
            resultMessage.append("Affected rows: ").append(updateCount);
            shortMessage.append("Cleaning SOA infra JMS reporitng data table sucessful\n");
            shortMessage.append("Affected rows: ").append(updateCount);
            connection.commit();
            //connection.close();

            // connection = getConnection();
            ProgressMonitor.increment("Deleting from WLI_QS_REPORT_ATTRIBUTE");
            sb = new StringBuilder();
            sb.append(" DELETE FROM WLI_QS_REPORT_ATTRIBUTE ");
            sb.append("WHERE MSG_LABELS LIKE '%Key=");
            sb.append(entityRefColValue);
            if (this.messageRefColValue != null) {
                sb.append(";SourceMessageId=");
                sb.append(messageRefColValue);
            }
            sb.append("%'");
            deleteSOAWLIAttributeTable = sb.toString();
            command = deleteSOAWLIAttributeTable;
            statement = connection.createStatement();
            boolean r = statement.execute(deleteSOAWLIAttributeTable);
            logger.trace("second wli table." + result);
            resultMessage.append("\nCleaning SOA infra JMS reporitng attribute table sucessful issuing following command:\n");
            resultMessage.append(deleteSOAWLIAttributeTable + "\n");
            updateCount = statement.getUpdateCount();
            resultMessage.append("Affected rows: " + updateCount);
            shortMessage.append("\nCleaning SOA infra JMS reporitng attribute table sucessful\n");
            shortMessage.append("Affected rows: " + updateCount);
            connection.commit();

            ProgressMonitor.increment("Deleting from " + reportingEventsTable);
            sb = new StringBuilder();
            sb.append(" DELETE FROM NWK_XXXIW.");
            sb.append(reportingEventsTable);
            sb.append(" WHERE ENTITY_REF = '");
            sb.append(entityRefColValue);
            if (this.messageRefColValue != null) {
                sb.append("' AND MESSAGE_REF = '");
                sb.append(messageRefColValue);
            }
            sb.append("'");
            deleteOSBReportingEventsTable = sb.toString();
            command = deleteOSBReportingEventsTable;
            result = statement.execute(deleteOSBReportingEventsTable);
            logger.trace("osb table." + result);

            resultMessage.append("\nCleaning OSB reporting table sucessful issuing following command:\n");
            resultMessage.append(deleteOSBReportingEventsTable + "\n");
            updateCount = statement.getUpdateCount();
            resultMessage.append("Affected rows: " + updateCount);
            shortMessage.append("\nCleaning OSB reporting table sucessful\n");
            shortMessage.append("Affected rows: " + updateCount);
            connection.commit();

            // Error tables clean up
            // Table XXXIW_MWP_ERROR_DETAILS
            ProgressMonitor.increment("Deleting from XXXIW_MWP_ERROR_DETAILS");
            sb = new StringBuilder();
            sb.append(" DELETE FROM XXXIW_MWP_ERROR_DETAILS WHERE error_id IN \n");
            sb.append(" (\n");
            sb.append("  SELECT ID FROM xxxiw_mwp_errors ERR WHERE err.source_ENTITY_REF = '" + entityRefColValue);
            if (this.messageRefColValue != null) {
                sb.append("' AND err.source_MESSAGE_REF = '" + messageRefColValue);
            }
            sb.append("'\n )");
            command = sb.toString();
            result = statement.execute(command);
            logger.trace("osb table." + result);
            connection.commit();

            resultMessage.append("\nCleaning OSB reporting ERROR_DETAILS table sucessful issuing following command:\n");
            resultMessage.append(command + "\n");
            updateCount = statement.getUpdateCount();
            resultMessage.append("Affected rows: " + updateCount);
            shortMessage.append("\nCleaning OSB reporting ERROR_DETAILS table sucessful\n");
            shortMessage.append("Affected rows: " + updateCount);

            // Table xxxiw_mwp_errors
            ProgressMonitor.increment("Deleting from XXXIW_MWP_ERRORS");
            sb = new StringBuilder();
            sb.append(" DELETE FROM XXXIW_MWP_ERRORS ERR WHERE err.SOURCE_ENTITY_REF = '" + entityRefColValue);
            if (this.messageRefColValue != null) {
                sb.append("' AND err.SOURCE_MESSAGE_REF = '" + messageRefColValue);
            }
            sb.append("'");
            command = sb.toString();
            result = statement.execute(command);
            logger.trace("osb table." + result);

            resultMessage.append("\nCleaning OSB reporting ERRORS table sucessful issuing following command:\n");
            resultMessage.append(command).append("\n");
            updateCount = statement.getUpdateCount();
            resultMessage.append("Affected rows: ").append(updateCount);
            shortMessage.append("\nCleaning OSB reporting ERRORS table sucessful\n");
            shortMessage.append("Affected rows: ").append(updateCount);
            connection.commit();

            cor.markSuccessful();
            cor.addMsg(resultMessage.toString(), shortMessage.toString());
        } catch (SQLException ex) {
            String message = "An error occured when trying to clear SOA reporting subsystem:\n" + ex.getErrorCode() + ": " + ex.getMessage();
            cor.addMsg(message + "\n by issuing command:\n" + command);
        } finally {
            DatabaseComponent.closeConnection(connection);
        }
    }
*/
    private void threadSleep(long delay) throws UtilComponentException {
        int seconds = (int) (delay / 1000);
        ProgressMonitor.init(seconds, seconds + "s");
        try {
            if (delay < 1000) {
                Thread.sleep(delay);
            } else {
                for (int i = seconds - 1; i >= 0; i--) {
                    Thread.sleep(1000);
                    ProgressMonitor.increment(i + "s");
                }
            }
            cor.markSuccessful();
            cor.addMsg("Process delayed for " + seconds + " seconds.");
        } catch (InterruptedException ex) {
            final String msg = "Delaying the process for " + seconds + "s failed: " + ex.getMessage();
            cor.addMsg(msg);
            throw new UtilComponentException(msg, ex);
        }
    }
    
    private static void updateDBDate(Connection conn) throws SQLException {
        ResultSet rs = null;
        Statement statement = null;
        try {
            statement = conn.createStatement();
            rs = statement.executeQuery("select sysdate from dual");
            while (rs.next()) {
                java.sql.Timestamp sysdate = rs.getTimestamp("SYSDATE");
                FlowExecutor.setActualRunDBDate(sysdate);
            }
        } finally {
            DatabaseComponent.closeResultSet(rs);
            DatabaseComponent.closeStatement(statement);
        }
    }

    /**
     * Accesses the XXXIW_MWP_ERROR_DETAILS and WLI_QS_REPORT_ATTRIBUTE tables and fetches the error messages for selected interface, entityRef/messageId and start date
     * @param conn
     * @param interfaceName
     * @param entityRefColValue
     * @param messageRefColValue
     * @param dateFrom
     * @return 
     */
    private static String getListOfErrors(Connection conn, String interfaceName, String entityRefColValue, String messageRefColValue, String dateFrom) {
        ResultSet ers = null;
        Statement errorStatement = null;
        List<String> projects = new ArrayList<>();
        try {
            for(Project p: MCFG.getProjects(interfaceName)){
                projects.add(p.getName());
            }
        } catch(MasterConfigurationException e) {
            logger.warn(e.getLocalizedMessage());
        }
        StringBuilder sb = new StringBuilder();
        
        final StringBuilder errors = new StringBuilder("SELECT ERROR_MESSAGE FROM XXXIW_MWP_ERROR_DETAILS WHERE error_id IN \n");
        errors.append(" (\n  SELECT ID FROM xxxiw_mwp_errors ERR WHERE err.int_name like '");
        errors.append(interfaceName).append("%'");
        errors.append(" AND err.created_date > ");
        errors.append(dateFrom);        
        if (messageRefColValue != null) {
            errors.append(" AND err.source_ENTITY_REF = '");
            errors.append(entityRefColValue);
            errors.append("'");
        }
        if (messageRefColValue != null) {
            errors.append(" AND err.source_MESSAGE_REF = '");
            errors.append(messageRefColValue);
            errors.append("'");
        }
        errors.append("\n )");
        final StringBuilder reportErrors = new StringBuilder("SELECT ERROR_CODE, ERROR_REASON FROM WLI_QS_REPORT_ATTRIBUTE WHERE DB_TIMESTAMP > ");
        reportErrors.append(dateFrom);
        if (messageRefColValue != null || entityRefColValue != null) {
            reportErrors.append(" AND MSG_LABELS LIKE '%");
            if (messageRefColValue != null) {
                reportErrors.append("Key=");
                reportErrors.append(entityRefColValue);
                reportErrors.append(";");
            }
            if (messageRefColValue != null) {
                reportErrors.append("SourceMessageId=");
                reportErrors.append(messageRefColValue);
                reportErrors.append(";");
            }
            reportErrors.append("%'");
        } else {
            if (projects.size() > 0) {
                reportErrors.append(" AND (");
                String delim = "";
                for (String projectName: projects) {
                    reportErrors.append(delim);
                    reportErrors.append("INBOUND_SERVICE_NAME LIKE '%");
                    reportErrors.append(projectName);
                    reportErrors.append("%'");
                    delim = " OR ";
                }
                reportErrors.append(")");
            }
        }
        try {
            errorStatement = conn.createStatement();
            logger.debug("Gathering error data #1:\n" + errors.toString());
            ers = errorStatement.executeQuery(errors.toString());
            int ecnt=0;
            while (ers.next()) {
                if (ers.getString("ERROR_MESSAGE") != null) {
                    if (ecnt == 0) {
                        sb.append("-----------------------------------------------------\nError messages found in XXXIW_MWP_ERROR_DETAILS table:\n");
                    }
                    sb.append(++ecnt);
                    sb.append(". ");
                    sb.append(ers.getString("ERROR_MESSAGE"));
                    sb.append("\n");
                }
            }
            DatabaseComponent.closeResultSet(ers);
            DatabaseComponent.closeStatement(errorStatement);                        
            errorStatement = conn.createStatement();
            logger.debug("Gathering error data #2:\n" + reportErrors.toString());
            ers = errorStatement.executeQuery(reportErrors.toString());
            ecnt=0;
            while (ers.next()) {
                if (ers.getString("ERROR_CODE") != null) {
                    if (ecnt == 0) {
                        sb.append("\n-----------------------------------------------------\nError messages found in WLI_QS_REPORT_ATTRIBUTE table:\n");
                    }
                    sb.append(++ecnt);
                    sb.append(". ");
                    sb.append(ers.getString("ERROR_CODE"));
                    sb.append(": ");
                    sb.append(ers.getString("ERROR_REASON"));
                    sb.append("\n");
                }
            }
        } catch(SQLException e) {
            ;
        } finally {
            DatabaseComponent.closeResultSet(ers);
            DatabaseComponent.closeStatement(errorStatement);
        }
        return sb.toString();
    }

    private void archiveResults() throws UtilComponentException {
        File[] files = rootWorkingDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !name.equals(MCFG.getArchiveDirName());
            }
        });
        
        ProgressMonitor.init(files.length + 1, "Getting list of files...");
        String msg = "Getting list of files and folders to zip...";
        logger.info(msg);
        cor.addMsg(msg);
        
        File archiveDir = new File(rootWorkingDir, MCFG.getArchiveDirName());
        File archiveFile = new File(archiveDir, Utils.insertTimestampToFilename("test_results.zip", FlowExecutor.getActualRunDate()));
        
        final ZipUtils ZIP = ZipUtils.getInstance();
        try {
            msg = "Zipping files...";
            logger.info(msg);
            cor.addMsg(msg, null);
            ZIP.zip(files, archiveFile);
            msg = "Successfully stored zipped results in <a href='file://"+archiveFile.getAbsolutePath()+"'>"+archiveFile.getAbsolutePath()+"</a>";
            logger.info(msg);
            cor.addMsg(msg, null);
            cor.markSuccessful();
        } catch (IOException ex) {
            msg = "Failed to zip the contents of " + rootWorkingDir.getAbsolutePath() + " folder into <a href='file://"+archiveFile.getAbsolutePath()+"'>"+archiveFile.getAbsolutePath()+"</a>";
            cor.addMsg(msg, null);
            throw new UtilComponentException(msg, ex);
        }
    }
    
    private void cleanResults() throws UtilComponentException {
        File[] files = rootWorkingDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !name.equals(MCFG.getArchiveDirName());
            }
        });
        String msg = "Folders to delete: ";
        StringBuilder sb = new StringBuilder(msg);
        for (File file : files) {
            sb.append("\n").append(file.getAbsolutePath());
        }
        msg = sb.toString();
        logger.debug(msg);
        cor.addMsg(msg, null);
        
        msg = "Deleting files...";
        ProgressMonitor.init(files.length + 1, msg);
        logger.info(msg);
        cor.addMsg(msg, null);
        
        try {
            for (File file : files) {
                ProgressMonitor.increment(msg);
                Utils.deleteDirContent(file, true);
            }
            msg = "Successfully cleaned files from " + rootWorkingDir.getAbsolutePath() + " and its subdirectories";
            logger.info(msg);
            cor.addMsg(msg, null);
            cor.markSuccessful();
        } catch (IOException ex) {
            msg = "Failed to delete the contents of " + rootWorkingDir.getAbsolutePath();
            cor.addMsg(msg, null);
            throw new UtilComponentException(msg, ex);
        }
    }
    
    @Override
    protected void destructComponent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void printOSBReporting() throws UtilComponentException {
        try {
            Connection connection = getConnection();
            CallableStatement callableStatement = connection.prepareCall("{call "+gatherOSBReportsProcedure+"()}");
            boolean result = callableStatement.execute();
            connection.commit();
            logger.debug("Tried to update osb reporting table with result: " + result);
            ResultSet rsRepEvent, rsErrors = null;
            String repEventCommand = "SELECT DISTINCT\n"
                    + "repev.originator,\n"
                    + "repev.status,\n"
                    + "repev.jms_message_id,\n"
                    + "repev.project_name,\n"
                    + "repev.interface\n"
                    + "FROM "+reportingEventsTable+" REPEV\n"
                    + "WHERE REPEV.ENTITY_REF = '" + this.entityRefColValue + "'";
            if (this.messageRefColValue != null) {
                repEventCommand += " AND repev.message_ref = '" + this.messageRefColValue + "'";
            }

            logger.debug("Command for selecting report_events table:\n" + repEventCommand);
            String errorsCommand = "SELECT DISTINCT\n"
                    + "err.id,\n"
                    + "err.int_name,\n"
                    + "err.error_context,\n"
                    + "TO_CHAR(err.error_datetime, 'DD-MM-YYYY HH24:MI:SS') AS ERROR_DATETIME,\n"
                    + "errdet.error_code,\n"
                    + "errdet.error_message "
                    + "FROM xxxiw_mwp_errors ERR\n"
                    + "INNER JOIN xxxiw_mwp_error_details ERRDET\n"
                    + "ON err.id = errdet.error_id\n"
                    + "WHERE err.source_ENTITY_REF = '" + this.entityRefColValue + "'";
            if (this.messageRefColValue != null) {
                errorsCommand += " AND err.source_MESSAGE_REF = '" + this.messageRefColValue + "'";
            }
            logger.debug("Command for selecting error tables:\n" + errorsCommand);
            Statement statement = connection.createStatement();
            rsRepEvent = statement.executeQuery(repEventCommand);

            StringBuilder sb = new StringBuilder();
            sb.append("\n************************************************************************************************");
            sb.append("\n****************                      PRODUCTION TEST                       ********************");
            sb.append("\n************************************************************************************************");
            sb.append("\n");
            sb.append("\nQuerying OSB reporting in 2 stages withing database(").append(this.jdbcUrl).append(" under schema + '").append(this.userName).append("'): ");
            sb.append("\n");
            sb.append("\nPhase 1: checking table ").append(reportingEventsTable);
            sb.append("\n").append(repEventCommand);
            sb.append("\n");
            sb.append("\n************************************************************************************************");
            sb.append("\n****************               Phase 1 results(REPORTING EVENTS)                ****************");
            int count = 0;
            while (rsRepEvent.next()) {
                sb.append("\n");
                sb.append("\nResult number ").append(++count).append(" found with following data: ");
                sb.append("\n Interface: ").append(rsRepEvent.getString("interface"));
                sb.append("\n Originator: ").append(rsRepEvent.getString("originator"));
                sb.append("\n JMS message id: ").append(rsRepEvent.getString("jms_message_id"));
                sb.append("\n Project name: ").append(rsRepEvent.getString("project_name"));
                sb.append("\n Reporting status: ").append(rsRepEvent.getString("status"));
                sb.append("\n-----------------------------------------------------");
            }
            if (count == 0) {
                sb.append("No relevant records found.");
            }

            rsErrors = statement.executeQuery(errorsCommand);
            sb.append("\n");
            sb.append("\nPhase 2: checking tables ERRORS and ERROR_DETAILS");
            sb.append("\n").append(errorsCommand);
            sb.append("\n");
            sb.append("\n************************************************************************************************");
            sb.append("\n****************                Phase 2 results(ERRORS)                         ****************");
            count = 0;
            while (rsErrors.next()) {
                sb.append("\nResult number ").append(++count).append(" found with following data: ");
                sb.append("\n Interface: ").append(rsErrors.getString("int_name"));
                sb.append("\n Error context: ").append(rsErrors.getString("error_context"));
                sb.append("\n Error date: ").append(rsErrors.getString("ERROR_DATETIME"));
                sb.append("\n Error code: ").append(rsErrors.getString("error_code"));
                sb.append("\n Error message: ").append(rsErrors.getString("error_message"));
                sb.append("\n-----------------------------------------------------");
            }
            if (count == 0) {
                sb.append("No relevant records found.");
            }
            sb.append("\n Printout finished...");
            cor.markSuccessful();
            cor.addMsg(sb.toString());
        } catch (SQLException ex) {
            String msg = "Something happended while printing OSB report events:\n" + ExceptionUtils.getFullStackTrace(ex);
            cor.addMsg(msg);
            throw new UtilComponentException(msg, ex);
        }
    }
}

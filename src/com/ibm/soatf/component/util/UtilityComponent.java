package com.ibm.soatf.component.util;

import com.ibm.soatf.FrameworkException;
import com.ibm.soatf.FrameworkExecutionException;
import com.ibm.soatf.component.SOATFCompType;
import com.ibm.soatf.component.AbstractSOATFComponent;
import com.ibm.soatf.component.database.DatabaseComponent;
import static com.ibm.soatf.component.database.DatabaseComponent.constructJdbcUrl;
import com.ibm.soatf.component.ftp.FTPComponent;
import com.ibm.soatf.component.soap.SOAPComponent;
import com.ibm.soatf.config.ConfigurationManager;
import com.ibm.soatf.config.FrameworkConfiguration;
import com.ibm.soatf.config.InterfaceConfiguration;
import com.ibm.soatf.config.iface.IfaceExecBlock;
import com.ibm.soatf.config.iface.SOATFIfaceConfig.IfaceEndPoints.IfaceEndPoint;
import com.ibm.soatf.config.iface.db.DBConfig;
import com.ibm.soatf.config.iface.db.DbObject;
import com.ibm.soatf.config.iface.db.DbObject.CustomValue;
import com.ibm.soatf.config.iface.ftp.FTPConfig;
import com.ibm.soatf.config.iface.jms.JMSConfig;
import com.ibm.soatf.config.iface.soap.SOAPConfig;
import com.ibm.soatf.config.iface.soap.SOAPConfig.EnvelopeConfig.Element;
import com.ibm.soatf.config.iface.util.UTILConfig;
import com.ibm.soatf.config.master.Component;
import com.ibm.soatf.config.master.ExecuteOn;
import com.ibm.soatf.config.master.OSBReporting.OsbReportingInstance;
import com.ibm.soatf.config.master.Operation;
import com.ibm.soatf.config.master.OracleFusionMiddleware.OracleFusionMiddlewareInstance;
import com.ibm.soatf.flow.FlowPatternCompositeKey;
import com.ibm.soatf.flow.OperationResult;
import java.io.File;
import static java.lang.Boolean.TRUE;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author zANGETSu
 */
public class UtilityComponent extends AbstractSOATFComponent {

    private static final Logger logger = LogManager.getLogger(UtilityComponent.class);

    private OsbReportingInstance osbReportingInstance;
    private UTILConfig utilIfaceConfig;
    private FlowPatternCompositeKey fpck;

    //private FTPConfig ftpCfg;
    //private DBConfig dbCfg;
    //private SOAPConfig soapCfg;
    private long delay;
    private String jdbcUrl;
    private String userName;
    private String password;
    private final String driverClassName = "oracle.jdbc.driver.OracleDriver";

    private String messageRefColName;
    private String messageRefColValue;
    private String entityRefColName;
    private String entityRefColValue;
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

    private String deleteSOAWLIDataTable;
    private String deleteSOAWLIAttributeTable;
    private String deleteOSBReportingEventsTable;

    private File rootWorkingDir;

    private String jmsMessageId;

    StringBuilder resultMessage = new StringBuilder();

    private final OperationResult cor;

    public UtilityComponent(
            IfaceExecBlock ifaceExecBlock,
            OsbReportingInstance osbReportingInstance,
            UTILConfig utilInterfaceConfig,
            FlowPatternCompositeKey ifpck,
            File rootWorkingDir) {
        super(SOATFCompType.UTIL);
        this.ifaceExecBlock = ifaceExecBlock;
        this.osbReportingInstance = osbReportingInstance;
        this.utilIfaceConfig = utilInterfaceConfig;
        this.fpck = ifpck;
        this.rootWorkingDir = rootWorkingDir;
        cor = OperationResult.getInstance();
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
                throw new FrameworkExecutionException("");
            } catch (FrameworkExecutionException ex1) {
                java.util.logging.Logger.getLogger(UtilityComponent.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }

    }

    @Override
    public void executeOperation(Operation operation) throws FrameworkException {
        FrameworkConfiguration FCFG = ConfigurationManager.getInstance().getFrameworkConfig();
        InterfaceConfiguration ICFG = ConfigurationManager.getInstance().getInterfaceConfig(fpck.getIfaceName());
        
        
        if (operation.getExecuteOn() == ExecuteOn.NA) {
            component = Component.UTIL;
        } else {
            IfaceExecBlock firstIfaceExecBlock = ICFG.getIfaceTestScenario(this.fpck.getFlowPatternId(), this.fpck.getTestScenarioId()).getIfaceExecBlock().get(0);
            
            for (IfaceEndPoint ifaceEndPoint : ICFG.getIfaceEndPoint(firstIfaceExecBlock, operation.getExecuteOn())) {
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

        cor.setOperation(operation);
        loadQueryVariablesOSBReporting();
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
            case UTIL_CLEAR_REPORTING:
                cleanReporting();
                break;
            case UTIL_CHECK_REPORTING_FOR_SUCCESS:
                checkOSBDBReportingForSuccess();
                break;
            case UTIL_CHECK_REPORTING_FOR_FAILURE:
                checkOSBDBReportingForFailure();
                break;
            case UTIL_PRINT_OSB_REPORTING:
                printOSBReporting();
                break;
            default:
                logger.info("Operation execution not yet implemented: " + operation.getName());
                cor.addMsg("Operation: " + operation.getName() + " is valid, but not yet implemented");
        }

    }

    private void checkOSBDBReportingForFailure() throws FrameworkExecutionException {
        Connection conn = null;
        CallableStatement callableStatement = null;
        ResultSet rs = null;
        Statement statement = null;
        DbObject dbObject = null;
        resultMessage = new StringBuilder();
        String projectName = null;
        try {
            conn = getConnection();
            String getDBUSERByUserIdSql = "{call ERROR_HOSPITAL.GATHER_OSB_DATA()}";
            callableStatement = conn.prepareCall(getDBUSERByUserIdSql);
            boolean result = callableStatement.execute();
            logger.debug("Tryied to update osb reporting table with result: " + result);
            conn = getConnection();
            String command = "SELECT * " + osbReportingDatabaseDelete + " ORDER BY TIME ASC";
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
                    exitFound = true;
                }
                if (rs.getString("STATUS").equals("Error")) {
                    errorFound = true;
                }
            }
            if ("".equals(projectName) || projectName == null) {
                projectName = "Unknown Project - issue within OSB reporting level!!!";
            }
            if (entryFound && !exitFound && errorFound) {
                cor.markSuccessful();
                resultMessage.append("Checking OSB reporting for FAILURE resulted in overall success by issuing command:\n");
                resultMessage.append(command + "\n");
                resultMessage.append("Both reports of type Entry and Error found.\n");
                resultMessage.append("JMSMessageId: " + jmsMessageId);

            } else {
                resultMessage.append("Checking OSB reporting resulted failed, the process reports looks inconsistent, check done by issuing command:\n");
                resultMessage.append(command + "\n");
                resultMessage.append("It looks like there is problem with reporting configuration on database, osb levels, or wrong configuration on OSB proxy level.\n"
                        + "Check xpath and xquery implementation for project: " + projectName);
            }
            if (cnt == 0) {
                resultMessage.append("There has not been found any records in the OSB Reporting Database system by issuing command:\n ");
                resultMessage.append(command);
                resultMessage.append("\n for project " + projectName);
            }

        } catch (SQLException ex) {
            resultMessage.append("\n JDBC error occured" + ex.getErrorCode() + ": " + ex.getMessage());
            logger.fatal("JDBC error", ex);
        } finally {
            cor.addMsg(resultMessage.toString());
            if (callableStatement != null) {
                try {
                    callableStatement.close();
                } catch (SQLException ex) {
                    throw new FrameworkExecutionException(ex);
                }
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException ex) {
                        throw new FrameworkExecutionException(ex);
                    }
                }
            }
        }
    }

    private void checkOSBDBReportingForSuccess() throws FrameworkExecutionException {
        Connection conn = null;
        CallableStatement callableStatement = null;
        ResultSet rs = null;
        Statement statement = null;
        DbObject dbObject = null;
        resultMessage = new StringBuilder();
        String projectName = null;
        try {
            conn = getConnection();
            String getDBUSERByUserIdSql = "{call ERROR_HOSPITAL.GATHER_OSB_DATA()}";
            callableStatement = conn.prepareCall(getDBUSERByUserIdSql);
            boolean result = callableStatement.execute();
            logger.debug("Tryied to update osb reporting table with result: " + result);
            conn = getConnection();
            String command = "SELECT * " + osbReportingDatabaseDelete + " ORDER BY TIME ASC";
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
                    exitFound = true;
                }
                if (rs.getString("STATUS").equals("Error")) {
                    errorFound = true;
                }
            }
            if (projectName == "" || projectName == null) {
                projectName = "Unknown Project - issue within OSB reporting level!!!";
            }
            if (entryFound && exitFound && !errorFound) {
                cor.markSuccessful();
                resultMessage.append("Checking OSB reporting for SUCCESS resulted in overall success by issuing command:\n");
                resultMessage.append(command + "\n");
                resultMessage.append("Both reports of type Entry and Exit found.\n");
                resultMessage.append("JMSMessageId: " + jmsMessageId);

            } else if (entryFound && !exitFound && errorFound) {
                resultMessage.append("Checking OSB reporting resulted failed, the process reported error, check done by issuing command:\n");
                resultMessage.append(command + "\n");
            } else {
                resultMessage.append("Checking OSB reporting resulted failed, the process reports looks inconsistent, check done by issuing command:\n");
                resultMessage.append(command + "\n");
                resultMessage.append("It looks like there is problem with reporting configuration on database, osb levels, or wrong configuration on OSB proxy level.\n"
                        + "Check xpath and xquery implementation for project: " + projectName);
            }

            if (cnt == 0) {
                resultMessage.append("There has not been found any records in the OSB Reporting Database system by issuing command:\n ");
                resultMessage.append(command);
                resultMessage.append("\n for project " + projectName);
            }

        } catch (SQLException ex) {
            resultMessage.append("\n JDBC error occured" + ex.getErrorCode() + ": " + ex.getMessage());
            logger.fatal("JDBC error", ex);
        } finally {
            cor.addMsg(resultMessage.toString());
            if (callableStatement != null) {
                try {
                    callableStatement.close();

                } catch (SQLException ex) {
                    throw new FrameworkExecutionException(ex);
                }

                if (conn != null) {
                    try {
                        conn.close();

                    } catch (SQLException ex) {
                        throw new FrameworkExecutionException(ex);
                    }
                }
            }
        }
    }

    private Connection getConnection() throws FrameworkExecutionException {
        try {
            return DriverManager.getConnection(jdbcUrl, userName, password);
        } catch (SQLException ex) {
            throw new FrameworkExecutionException("Cannot get connection to database.", ex);
        }

    }

    private void loadQueryVariablesOSBReporting() throws FrameworkExecutionException {
        osbReportingDatabaseDelete = " FROM REPORTING_EVENTS WHERE ";
        entityRefColName = "ENTITY_REF";
        messageRefColName = "MESSAGE_REF";
        //component = Component.DB;
        File path;
        switch (component) {
            case DB:
                StringBuilder entitySb = new StringBuilder();
                StringBuilder messageSb = new StringBuilder();
                int bSEIdCount = 0, bSMIdCount = 0;
                for (CustomValue cusVal : dbConfig.getDbObjects().getDbObject().get(0).getCustomValue()) {
                    
                    Boolean bSEId = cusVal.isSourceEntityId();
                    if (bSEId != null) {
                        bSEIdCount++;
                        if (cusVal.isSourceEntityId().equals(TRUE)) {
                            if (bSEIdCount == 1){
                                entitySb.append(cusVal.getColumnValue());
                            } else{
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
                String fileName = ftpConfig.getFileName();
                path = new File(rootWorkingDir, "ftp");
                String fileContent = ftpConfig.getFileContent();

                String actualFile = FTPComponent.getFile(path, fileName, fileContent).getName();
                if (actualFile != null && actualFile.lastIndexOf(".") > 0) {
                    actualFile = actualFile.substring(0, actualFile.lastIndexOf("."));
                }
                entityRefColValue = actualFile;
                messageRefColValue = null;
                break;
            case SOAP:
                path = new File(rootWorkingDir, "soap");
                if (soapConfig.getEnvelopeConfig() != null) {
                    List<Element> customVals = soapConfig.getEnvelopeConfig().getElement();
                    if (customVals != null) {
                        String entityRefElement = null;
                        String messageRefElement = null;
                        for (Element e : customVals) {
                            if (e.isSourceEntityRef() != null && e.isSourceEntityRef()) {
                                entityRefElement = e.getElementXpath();
                            }
                            if (e.isSourceMessageId() != null && e.isSourceMessageId()) {
                                messageRefElement = e.getElementXpath();
                            }
                        }
                        if (entityRefElement != null) {
                            entityRefColValue = SOAPComponent.getValueFromGeneratedEnvelope(path, soapConfig.getServiceName(), soapConfig.getOperationName(), transformXPath(entityRefElement));
                            logger.trace("SOAP request entityRefId is: " + entityRefColValue);
                        }
                        if (messageRefElement != null) {
                            messageRefColValue = SOAPComponent.getValueFromGeneratedEnvelope(path, soapConfig.getServiceName(), soapConfig.getOperationName(), transformXPath(messageRefElement));
                            logger.trace("SOAP request messageRefId is: " + messageRefColValue);
                        }
                    }
                }
            case JMS:
                //TODO
                break;
            case UTIL:
                break;
            default:
                throw new IllegalArgumentException("Unknown component type on input: " + component.value() + ".");

        }
        osbReportingDatabaseDelete += entityRefColName + "= '" + entityRefColValue + "'";
        if (component != Component.FTP) { //(messageRefColValue != null)
                osbReportingDatabaseDelete += " AND " + messageRefColName + " = '" + messageRefColValue + "'";
        }
    }

    private String transformXPath(String xPath) {
        if (xPath == null) {
            return null;
        }
        String path;
        if (xPath.startsWith("$this") || xPath.startsWith("$body")) {
            path = xPath.substring(5);
        } else {
            path = xPath;
        }
        path = path.replaceAll("\\*:([^/]+)", "*[local-name()=\"$1\"]");
        String s = "$this";
        if (path.startsWith("/")) {
            s += path;
        } else {
            s = s + "//" + path;
        }
        return s;
    }

    private void cleanReporting() throws FrameworkExecutionException {
        String command = "DELETE" + this.osbReportingDatabaseDelete;
        Connection connection = getConnection();
        

        try {
            boolean result;
            Statement statement = connection.createStatement();
            StringBuilder sb = new StringBuilder();
            sb.append("DELETE FROM NWKOSB_DBUSER.WLI_QS_REPORT_DATA ");
            sb.append("WHERE MSG_GUID IN (");
            sb.append("  SELECT MSG_GUID ");
            sb.append("  FROM WLI_QS_REPORT_ATTRIBUTE");
            sb.append("  WHERE MSG_LABELS LIKE \'%Key=");
            sb.append(this.entityRefColValue);
            sb.append(";SourceMessageId=");
            sb.append(this.messageRefColValue);
            sb.append("%\')");
            deleteSOAWLIDataTable = sb.toString();
            command = deleteSOAWLIDataTable;
            statement = connection.createStatement();
            result = statement.execute(deleteSOAWLIDataTable);
            logger.trace("first wli table." + result);
            resultMessage.append("Cleaning SOA infra JMS reporitng data table sucessful issuing following command:\n");
            resultMessage.append(deleteSOAWLIDataTable + "\n");
            resultMessage.append("Affected rows: " + statement.getUpdateCount());
            connection.commit();
            //connection.close();

           // connection = getConnection();
            sb = new StringBuilder();
            sb.append("DELETE FROM NWKOSB_DBUSER.WLI_QS_REPORT_ATTRIBUTE ");
            sb.append("WHERE MSG_LABELS LIKE \'%Key=");
            sb.append(entityRefColValue);
            sb.append(";SourceMessageId=");
            sb.append(messageRefColValue);
            sb.append("%\'");
            deleteSOAWLIAttributeTable = sb.toString();
            command = deleteSOAWLIAttributeTable;
            statement = connection.createStatement();
            boolean r = statement.execute(deleteSOAWLIAttributeTable);
                logger.trace("second wli table." + result);
            resultMessage.append("\nCleaning SOA infra JMS reporitng attribute table sucessful issuing following command:\n");
            resultMessage.append(deleteSOAWLIAttributeTable + "\n");
            resultMessage.append("Affected rows: " + statement.getUpdateCount());
            connection.commit();

            sb = new StringBuilder();
            sb.append("DELETE FROM NWK_XXXIW.REPORTING_EVENTS ");
            sb.append("WHERE ENTITY_REF = \'");
            sb.append(entityRefColValue);
            sb.append("' AND MESSAGE_REF = \'");
            sb.append(messageRefColValue);
            sb.append("\'");
            deleteOSBReportingEventsTable = sb.toString();
            command = deleteOSBReportingEventsTable;
            result = statement.execute(deleteOSBReportingEventsTable);
            logger.trace("obs table." + result);
            
            resultMessage.append("\nCleaning OSB reporting table sucessful issuing following command:\n");
            resultMessage.append(deleteOSBReportingEventsTable + "\n");
            resultMessage.append("Affected rows: " + statement.getUpdateCount());
            connection.commit();
            
            // Error tables clean up
            // Table XXXIW_MWP_ERROR_DETAILS
            sb = new StringBuilder();
            sb.append("DELETE FROM XXXIW_MWP_ERROR_DETAILS WHERE error_id IN \n");
            sb.append("(\n");
            sb.append("  SELECT ID FROM xxxiw_mwp_errors ERR WHERE err.source_ENTITY_REF = '" + entityRefColValue + "' AND err.source_message_ref = '" + messageRefColValue +"'\n");
            sb.append("  )");
            sb.toString();
            command = sb.toString();
            result = statement.execute(command);
            logger.trace("obs table." + result);
            connection.commit();
            
            resultMessage.append("\nCleaning OSB reporting ERROR_DETAILS table sucessful issuing following command:\n");
            resultMessage.append(command + "\n");
            resultMessage.append("Affected rows: " + statement.getUpdateCount());
            
            // Table xxxiw_mwp_errors
            sb = new StringBuilder();
            sb.append("  DELETE FROM xxxiw_mwp_errors ERR WHERE err.source_ENTITY_REF = '" + entityRefColValue + "' AND err.source_message_ref = '" + messageRefColValue +"'\n");
            sb.toString();
            command = sb.toString();
            result = statement.execute(command);
            logger.trace("obs table." + result);
            
            resultMessage.append("\nCleaning OSB reporting ERROR_DETAILS table sucessful issuing following command:\n");
            resultMessage.append(command + "\n");
            resultMessage.append("Affected rows: " + statement.getUpdateCount());
            connection.commit();
            
            cor.markSuccessful();
            cor.addMsg(resultMessage.toString());
        } catch (SQLException ex) {
            String message = "There occured error when trying to clear SOA reporting subsystem:\n" + ex.getErrorCode() + ": " + ex.getMessage();
            cor.addMsg(message + "\n by issuing command:\n" + command);
        } finally{
            try {
                connection.close();
            } catch (SQLException ex) {
               String msg = "TODO";
               throw new FrameworkExecutionException(ex);
            }
        }
    }

    private void threadSleep(Long delay) {
        try {
            Thread.sleep(delay);
            cor.markSuccessful();
            cor.addMsg("Process dealyed for " + delay / 1000 + " seconds.");
        } catch (InterruptedException ex) {
            cor.addMsg("Process dealyed for " + delay / 1000 + " seconds failed: " + ex.getMessage());
        }
    }

    @Override
    protected void destructComponent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void printOSBReporting() throws FrameworkExecutionException {
        try {
            Connection connection = getConnection();
            String getDBUSERByUserIdSql = "{call ERROR_HOSPITAL.GATHER_OSB_DATA()}";
            CallableStatement callableStatement = connection.prepareCall(getDBUSERByUserIdSql);
            boolean result = callableStatement.execute();
            connection.commit();
            logger.debug("Tryied to update osb reporting table with result: " + result);
            Statement statement = connection.createStatement();
            ResultSet rsRepEvent, rsErrors = null;
            String repEventCommand = "SELECT DISTINCT\n" +
                    "repev.originator,\n" +
                    "repev.status,\n" +
                    "repev.jms_message_id,\n" +
                    "repev.project_name,\n" +
                    "repev.interface\n" +
                    "FROM REPORTING_EVENTS REPEV\n" +
                    "WHERE REPEV.ENTITY_REF = '" + this.entityRefColValue + "'";
            if (this.messageRefColValue != null) {
                repEventCommand += " AND repev.message_ref = '" + this.messageRefColValue + "'";
            }
       
            logger.debug("Command for selecting report_events table:\n" + repEventCommand);
            String errorsCommand = "SELECT DISTINCT\n" + 
                    "err.id,\n" +
                    "err.int_name,\n" +
                    "err.error_context,\n" +
                    "TO_CHAR(err.error_datetime, 'DD-MM-YYYY HH24:MI:SS') AS ERROR_DATETIME,\n"+
                    "errdet.error_code,\n" + 
                    "errdet.error_message " + 
                    "FROM xxxiw_mwp_errors ERR\n" +
                    "INNER JOIN xxxiw_mwp_error_details ERRDET\n" +
                    "ON err.id = errdet.error_id\n" +
                    "WHERE err.source_ENTITY_REF = '" + this.entityRefColValue + "'";
            if (this.messageRefColValue != null) {
                errorsCommand += " AND err.source_message_ref = '" + this.messageRefColValue + "'";
            }            
            logger.debug("Command for selecting error tables:\n" + errorsCommand);
            statement = connection.createStatement();
            rsRepEvent = statement.executeQuery(repEventCommand);
            
            
            StringBuilder sb = new StringBuilder();
            sb.append("\n************************************************************************************************");
            sb.append("\n****************                      PRODUCTION TEST                       ********************");
            sb.append("\n************************************************************************************************");
            sb.append("\n");
            sb.append("\nQuerying OSB reporting in 2 stages withing database(" + this.jdbcUrl+ " under schema + '" + this.userName + "'): ");
            sb.append("\n");
            sb.append("\nPhase 1: checking table REPORTING_EVENTS");
            sb.append("\n" + repEventCommand);
            sb.append("\n");
            sb.append("\n************************************************************************************************");
            sb.append("\n****************               Phase 1 results(REPORTING EVENTS)                ****************");
            int count = 0;
            while (rsRepEvent.next()){
                sb.append("\n");
                sb.append("\nResult number " + ++count + " found with following data: ");
                sb.append("\n Interface: " + rsRepEvent.getString("interface"));
                sb.append("\n Originator: " + rsRepEvent.getString("originator"));
                sb.append("\n JMS message id: " + rsRepEvent.getString("jms_message_id")); 
                sb.append("\n Project name: " + rsRepEvent.getString("project_name")); 
                sb.append("\n Reporting status: " + rsRepEvent.getString("status")); 
                sb.append("\n-----------------------------------------------------");
              }
            if (count == 0){
                sb.append("No relevant records found.");
            }
            
            
            rsErrors = statement.executeQuery(errorsCommand);
            sb.append("\n");
            sb.append("\nPhase 2: checking tables ERRORS and ERROR_DETAILS");
            sb.append("\n" + errorsCommand);
            sb.append("\n");
            sb.append("\n************************************************************************************************");
            sb.append("\n****************                Phase 2 results(ERRORS)                         ****************");
            count = 0;
            while (rsErrors.next()){
                sb.append("\nResult number " + ++count + " found with following data: ");
                sb.append("\n Interface: " + rsErrors.getString("int_name"));
                sb.append("\n Error context: " + rsErrors.getString("error_context"));
                sb.append("\n Error date: " + rsErrors.getString("ERROR_DATETIME")); 
                sb.append("\n Error code: " + rsErrors.getString("error_code")); 
                sb.append("\n Error message: " + rsErrors.getString("error_message")); 
                sb.append("\n-----------------------------------------------------");
              }
            if (count == 0){
                sb.append("No relevant records found.");
            }
            sb.append("\n Printout finished...");
            cor.markSuccessful();
            cor.addMsg(sb.toString());
        } catch (FrameworkExecutionException | SQLException ex) {
            String msg = "Something happended while printing OSB report events:\n" + ExceptionUtils.getFullStackTrace(ex);
            cor.addMsg(msg);
            throw new FrameworkExecutionException(ex);
        }
    }
}

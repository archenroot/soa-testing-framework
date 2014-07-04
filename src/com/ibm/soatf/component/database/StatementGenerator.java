package com.ibm.soatf.component.database;

import com.ibm.soatf.config.iface.db.DbObject;
import com.ibm.soatf.flow.OperationResult;
import com.ibm.soatf.gui.ProgressMonitor;
import com.ibm.soatf.tool.FileSystem;
import com.ibm.soatf.tool.RandomGenerator;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Calendar;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class that generates the insert statement based on the custom values and real metadata taken from the database table
 * @author kroky
 */
public final class StatementGenerator {

    private static final Logger logger = LogManager.getLogger(StatementGenerator.class);
    private static final int DEFAULT_NUMBER_PRECISION = 9;

    /**
     * Method that generates the insert statement based on the custom values and real metadata taken from the database table
     * 
     * @param conn SQL connection through which we connect to the desired database and query the table for its metadata
     * @param config config metadata (e.g. custom values) for the DbObject (table)
     * @param file the file you want the statement to be written to
     * @throws DatabaseComponentException 
     */
    public static void generateInsertStatement(Connection conn, DatabaseComponent.DbObjectConfig config, File file) throws DatabaseComponentException {
        OperationResult cor = OperationResult.getInstance();
        String objectName = config.getDbObjectName();
        
        Map<String, DbObject.CustomValue> customValuesMap = config.getCustomValuesMap();
        String outputScriptFilePath = "";
        String outputScriptRelativePath = "";
        try {
            logger.info("Generating INSERT statements for: " + objectName);
            Calendar cal = Calendar.getInstance();
            Statement stmt = conn.createStatement();
            ProgressMonitor.increment("Getting table metadata...");
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + objectName);
            ResultSetMetaData rsmd = rs.getMetaData();

            int numColumns = rsmd.getColumnCount();
            //printColumnNames(rsmd);

            StringBuilder columnNames = new StringBuilder();
            StringBuilder columnValues = new StringBuilder();

            int maxLength = 0;
            
            for (int i = 1; i <= numColumns; i++) { //for pretty print
                String columnName = rsmd.getColumnName(i);
                maxLength = columnName.length() > maxLength ? columnName.length() : maxLength;
            } 
            maxLength++;
            ProgressMonitor.increment("Generating INSERT statement...");
            for (int i = 1; i <= numColumns; i++) {
                String columnName = rsmd.getColumnName(i).toUpperCase();
                
                String str = new StringBuilder("%n    /*").append(columnName).append("*/%").append(maxLength - columnName.length()).append("s").toString();
                columnValues.append(String.format(str, " "));
                if (i != 1) {
                    columnValues.append(",");
                    columnNames.append(",");
                } else {
                    columnValues.append(" ");
                }
                
                columnNames.append(columnName);
                
                int type = rsmd.getColumnType(i);
                String v;
                int size = rsmd.getPrecision(i);
                switch (type) {
                    case Types.BIGINT:
                    case Types.BIT:
                    case Types.BOOLEAN:
                    case Types.DECIMAL:
                    case Types.DOUBLE:
                    case Types.FLOAT:
                    case Types.NUMERIC:
                    case Types.INTEGER:
                    case Types.SMALLINT:
                    case Types.TINYINT:
                        v = customValuesMap.containsKey(columnName) ? customValuesMap.get(columnName).getColumnValue() : String.valueOf(RandomGenerator.getNumeric(size == 0 ? DEFAULT_NUMBER_PRECISION : size));
                        if("".equals(v)) v="null";
                        break;
                    case Types.DATE:
                    case Types.TIMESTAMP:
                        if(customValuesMap.containsKey(columnName)) {
                            v = dateField(customValuesMap.get(columnName));
                        } else {
                            v = String.format("TO_DATE('%s', 'YYYY/MM/DD HH24:MI:SS')", DatabaseComponent.DATE_FORMAT.format(cal.getTime()));
                        }
                        break;
                    case Types.TIME:
                        if(customValuesMap.containsKey(columnName)) {
                            v = dateField(customValuesMap.get(columnName));
                        } else {
                            v = String.format("TO_DATE('%s', 'HH24:MI:SS')", DatabaseComponent.TIME_FORMAT.format(cal.getTime()));
                        }
                        break;
                    default:
                        if (customValuesMap.containsKey(columnName)) {
                            v = customValuesMap.get(columnName).getColumnValue().replaceAll("'", "''");
                            v = "null".equals(v) ? "null" : "'" + v + "'";
                        } else {
                            v = String.format("'%s'", RandomGenerator.getRandomAlphabetical(size));
                        }
                        break;
                }
                
                columnValues.append(v);
                
                logger.debug("Processed column name " + columnName + " with value " + v);
            }
            String msg = "Successfuly generated INSERT statement for object: " + objectName;
            logger.debug(msg);
            cor.addMsg(msg);
            ProgressMonitor.increment("Saving INSERT statement to file...");
            if (file.exists()) {
                FileUtils.forceDelete(file);
            }
            String insert = String.format("INSERT INTO %s (%s\n) VALUES (%s\n);",
                    objectName,
                    columnNames,
                    columnValues);
            outputScriptFilePath = file.getAbsolutePath();
            outputScriptRelativePath = FileSystem.getRelativePath(file);
            FileUtils.writeStringToFile(file, insert);
            msg = "Successfuly stored INSERT statement for object: " + objectName + " in [FILE: %s]";
            logger.debug(String.format(msg, outputScriptFilePath));
            cor.addMsg(msg, "<a href='file://"+outputScriptFilePath+"'>"+outputScriptFilePath+"</a>", outputScriptRelativePath);
            //cor.markSuccessful();
        } catch (SQLException ex) {
            final String msg = String.format("Failed to generate INSERT statement. Reason: %s", ex.getMessage());
            cor.addMsg(msg);
            throw new DatabaseComponentException(msg, ex);
        } catch (IOException ex) {
            final String msg = "Failed to save INSERT statement file %s. Reason: " + ex.getMessage();
            cor.addMsg(msg, "<a href='file://"+outputScriptFilePath+"'>"+outputScriptFilePath+"</a>", outputScriptRelativePath);
            throw new DatabaseComponentException(String.format(msg, outputScriptFilePath), ex);
        }
    }

    /**
     * Returns the value in to_date(arg1, arg2) format where arg1 is the date in string format taken from the column's value
     * and the arg2 is the corresponding database date format
     * @param column DB column that is of a date/timestamp type
     * @return the value in to_date(arg1, arg2) format
     */
    public static String dateField(DbObject.CustomValue column) {
        String dateStr = column.getColumnValue();
        if (dateStr == null || "".equals(dateStr)) {
            return "null";
        }
        if(dateStr.toUpperCase().startsWith("TO_DATE") || dateStr.toUpperCase().startsWith("TO_TIMESTAMP")) {
            return dateStr;
        }
        //automatically tries to use the correct format pattern based on the date value
        Pattern p = Pattern.compile("\\d\\d-[a-zA-Z]{3}-\\d\\d");
        if(p.matcher(dateStr).matches()) {
            dateStr = "TO_DATE('" + dateStr + "', 'DD-MON-YY')";
        } else {
            p = Pattern.compile("\\d\\d\\d\\d-\\d\\d-\\d\\d");
            if(p.matcher(dateStr).matches()) {
                dateStr = "TO_DATE('" + dateStr + "', 'YYYY-MM-DD')";
            } else {
                p = Pattern.compile("\\d\\d\\d\\d-[a-zA-Z]{3}-\\d\\d");
                if(p.matcher(dateStr).matches()) {
                    dateStr = "TO_DATE('" + dateStr + "', 'YYYY-MON-DD')";
                } else {
                    dateStr = "TO_DATE('" + dateStr + "')";
                }
            }
        }
        return dateStr;
    }
}

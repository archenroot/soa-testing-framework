package com.ibm.soatf.component.database;

import com.ibm.soatf.flow.OperationResult;
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

public final class StatementGenerator {

    private static final Logger logger = LogManager.getLogger(StatementGenerator.class);
    private static final int DEFAULT_NUMBER_PRECISION = 9;

    public static void generateInsertStatement(Connection conn, DatabaseComponent.DbObjectConfig config, File file) throws DatabaseComponentException {
        OperationResult cor = OperationResult.getInstance();
        String objectName = config.getDbObjectName();
        
        Map<String, String> customValuesMap = config.getCustomValuesMap();
        String outputScriptFilePath = "";
        try {
            logger.info("Generating INSERT statements for: " + objectName);
            Calendar cal = Calendar.getInstance();
            Statement stmt = conn.createStatement();
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
                        v = customValuesMap.containsKey(columnName) ? customValuesMap.get(columnName) : String.valueOf(RandomGenerator.getNumeric(size == 0 ? DEFAULT_NUMBER_PRECISION : size));
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
                        v = customValuesMap.containsKey(columnName) ? customValuesMap.get(columnName) : String.format("TO_DATE('%s', 'HH24:MI:SS')", DatabaseComponent.TIME_FORMAT.format(cal.getTime()));
                        break;
                    default:
                        if (customValuesMap.containsKey(columnName)) {
                            v = customValuesMap.get(columnName).replaceAll("'", "''");
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
            
            if (file.exists()) {
                FileUtils.forceDelete(file);
            }
            String insert = String.format("INSERT INTO %s (%s\n) VALUES (%s\n);",
                    objectName,
                    columnNames,
                    columnValues);
            outputScriptFilePath = file.getCanonicalPath();
            FileUtils.writeStringToFile(file, insert);
            msg = "Successfuly stored INSERT statement for object: " + objectName + " in file: " + outputScriptFilePath;
            logger.debug(msg);
            cor.addMsg(msg);
            cor.markSuccessful();
        } catch (SQLException ex) {
            final String msg = String.format("Failed to generate INSERT statement. Reason: %s", ex.getMessage());
            cor.addMsg(msg);
            throw new DatabaseComponentException(msg, ex);
        } catch (IOException ex) {
            final String msg = String.format("Failed to save INSERT statement file %s. Reason: %s", outputScriptFilePath, ex.getMessage());
            cor.addMsg(msg);
            throw new DatabaseComponentException(msg, ex);
        }
    }

    public static String dateField(String dateStr) {
        if(dateStr.startsWith("TO_DATE")) {
            return dateStr;
        }
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

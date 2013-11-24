package com.ibm.soatf.component.database;

import com.ibm.soatf.component.ComponentResult;
import com.ibm.soatf.config.iface.db.DbObject;
import java.io.*;
import java.sql.*;
import com.ibm.soatf.tool.RandomGenerator;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class StatementGenerator {

    private static final Logger logger = LogManager.getLogger(StatementGenerator.class);
    private static final int DEFAULT_NUMBER_PRECISION = 9;

    public static void generateInsertStatement(Connection conn, DbObject object, String outputFilePath, ComponentResult cor) throws StatementGeneratorException {
        File file = null;
        String objectName = object.getName();
        List<DbObject.CustomValue> customValues = object.getCustomValue();
        
        Map<String, String> customValuesMap = new HashMap<>();
        if(customValues != null) {
            for (DbObject.CustomValue customValue : customValues) {
                final String name = customValue.getColumnName().toUpperCase();
                final String value = customValue.getColumnValue();
                customValuesMap.put(name, value.equalsIgnoreCase("null") ? "null" : value);
            }
        }
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
                        v = customValuesMap.containsKey(columnName) ? customValuesMap.get(columnName) : String.format("TO_DATE('%s', 'YYYY/MM/DD HH24:MI:SS')", DatabaseComponent.DATE_FORMAT.format(cal.getTime()));
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

            file = new File(outputFilePath);

            if (file.exists()) {
                FileUtils.forceDelete(file);
            }
            String insert = String.format("INSERT INTO %s (%s\n) VALUES (%s\n);",
                    objectName,
                    columnNames,
                    columnValues);
            
            
            FileUtils.writeStringToFile(file, insert);
            logger.debug("Successfuly stored INSERT statement for object: " + objectName + " in file: " + file.getCanonicalPath());
            cor.addMsg("Successfuly stored INSERT statement for object: " + objectName + " in file: " + file.getCanonicalPath());
            cor.setOverallResultSuccess(true);
            //cor.setResultMessage(objectName);
        } catch (SQLException ex) {
            String sqlExMsg = "SQLException " + ex.getErrorCode() + ": " + ex.getMessage();
            logger.error(sqlExMsg);
            throw new StatementGeneratorException("Failed to generate INSERT statement: " + sqlExMsg, ex);
        } catch (IOException ex) {
            String msg = "IOException: " + ex.getMessage();
            logger.error(msg);
            String canonicalPath = null;
            try {
                if(file != null) {
                    canonicalPath = file.getCanonicalPath();
                }
            } catch (IOException ex1) {
                //nothing to do
            }
            throw new StatementGeneratorException("Failed to save INSERT statement file (" + canonicalPath + "): " + msg, ex);
        }
    }
}

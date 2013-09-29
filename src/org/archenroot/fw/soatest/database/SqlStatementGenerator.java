package org.archenroot.fw.soatest.database;

import java.io.*;
import java.math.BigInteger;
import java.sql.*;
import java.text.SimpleDateFormat;
import org.archenroot.fw.soatest.tool.RandomGenerator;

public final class SqlStatementGenerator {

    private static final SimpleDateFormat dateFormat
            = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private String objectName;
    private Connection conn;
    private String outputSQLScriptFileName;

    private SqlStatementGenerator() {
    }

    public SqlStatementGenerator(
            Connection conn, String objectName, String outputSQLScriptFileName) {
        this.conn = conn;
        this.objectName = objectName;
        this.outputSQLScriptFileName = outputSQLScriptFileName;
    }

    public void generateInsertStatementsFromObject()
            throws Exception {
        log("Generating Insert statements for: " + objectName);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + objectName);
        ResultSetMetaData rsmd = rs.getMetaData();
        int numColumns = rsmd.getColumnCount();
        int[] columnTypes = new int[numColumns];
        String columnNames = "";
        for (int i = 0; i < numColumns; i++) {
            columnTypes[i] = rsmd.getColumnType(i + 1);
            if (i != 0) {
                columnNames += ",";
            }
            columnNames += rsmd.getColumnName(i + 1);
        }

        java.util.Date d = null;
        PrintWriter p = new PrintWriter(new FileWriter(objectName + "_insert.sql"));
        p.println("set sqlt off");
        p.println("set sqlblanklines on");
        p.println("set define off");
        while (rs.next()) {
            String columnValues = "";
            for (int i = 0; i < numColumns; i++) {
                if (i != 0) {
                    columnValues += ",";
                }

                switch (columnTypes[i]) {
                    case Types.BIGINT:
                    case Types.BIT:
                    case Types.BOOLEAN:
                    case Types.DECIMAL:
                    case Types.DOUBLE:
                    case Types.FLOAT:
                    case Types.INTEGER:
                    case Types.SMALLINT:
                    case Types.TINYINT:
                        String v = rs.getString(i + 1);
                        columnValues += v;
                        break;

                    case Types.DATE:
                        d = rs.getDate(i + 1);
                    case Types.TIME:
                        if (d == null) {
                        d = rs.getTime(i + 1);
                    }
                    case Types.TIMESTAMP:
                        if (d == null) {
                        d = rs.getTimestamp(i + 1);
                    }

                        if (d == null) {
                            columnValues += "null";
                        } else {
                            columnValues += "TO_DATE('"
                                    + dateFormat.format(d)
                                    + "', 'YYYY/MM/DD HH24:MI:SS')";
                        }
                        break;

                    default:
                        v = rs.getString(i + 1);
                        if (v != null) {
                            columnValues += "'" + v.replaceAll("'", "''") + "'";
                        } else {
                            columnValues += "null";
                        }
                        break;
                }
            }
            p.println(String.format("INSERT INTO %s (%s) values (%s)\n/",
                    objectName,
                    columnNames,
                    columnValues));
        }
        p.close();
    }

    public void generateOneRowSampleInsertStatement() throws SQLException, IOException {
        log("Generating Insert statements for: " + objectName);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + objectName);
        ResultSetMetaData rsmd = rs.getMetaData();
        int numColumns = rsmd.getColumnCount();
        
        
        int[] columnTypes = new int[numColumns];
        String columnNames = "";
        String columnValues = "";
        for (int i = 0; i < numColumns; i++) {
            if (i != 0) {
                columnNames += ", ";
                columnValues += ", ";
            }
            columnNames += rsmd.getColumnName(i + 1);
            
            switch (rsmd.getColumnType(i + 1)) {
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
                        String v = RandomGenerator.getNumeric(rsmd.getPrecision(i + 1));
                        columnValues += v;
                        break;
                    /*
                    case Types.DATE:
                        d = rs.getDate(i + 1);
                    case Types.TIME:
                        if (d == null) {
                        d = rs.getTime(i + 1);
                    }
                    case Types.TIMESTAMP:
                        if (d == null) {
                        d = rs.getTimestamp(i + 1);
                    }

                        if (d == null) {
                            columnValues += "null";
                        } else {
                            columnValues += "TO_DATE('"
                                    + dateFormat.format(d)
                                    + "', 'YYYY/MM/DD HH24:MI:SS')";
                        }
                        break;
                    */
                    default:
                        columnValues += "'" + RandomGenerator.getRandomAlphabetical(rsmd.getPrecision(i + 1)) + "'";
                        break;
                }
        }
        
        java.util.Date d = null;
        String outFile;
        if (outputSQLScriptFileName.isEmpty()){
            outFile = objectName + "_insert.sql";
        } else{
            outFile = this.outputSQLScriptFileName;
        }
        PrintWriter p = new PrintWriter(new FileWriter(outFile));
        p.println("set sqlt off");
        p.println("set sqlblanklines on");
        p.println("set define off");
        
        
        p.println(String.format("INSERT INTO %s (%s) values (%s)\n/",
                    objectName,
                    columnNames,
                    columnValues));
        
        p.close();
    }

    private void log(String s) {
        System.out.println(s);
    }

    private void usage() {
        System.out.println("java GenerateInsertStatements [username/password] objectName|-f fileName");
    }
}

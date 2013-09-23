package org.archenroot.fw.soatest.database;

import java.io.*;
import java.math.BigInteger;
import java.sql.*;
import java.text.SimpleDateFormat;

public class SQLStatementGenerator {
    
    private String jdbcDriver;
    private String jdbcUrl = "jdbc:oracle:thin:@10.200.100.80:1521:MY_SID";
    private String jdbcUserName = "MY_SCHEMA";
    private String jdbcPassword = "THE_PASSWORD";
    private String tableName;
    
    private static final SimpleDateFormat dateFormat = 
                         new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    SQLStatementGenerator(){}
    
    SQLStatementGenerator(String jdbcDriver
            , String hostName
            , BigInteger port
            , String userName
            , String password
            , String serviceId
            , String connectAs
            , String tableName){
        this.jdbcDriver = jdbcDriver;
        this.jdbcUrl = constructJdbcUrl(hostName, port, serviceId);
        this.jdbcUserName = userName;
        this.jdbcPassword = password;
        this.tableName = tableName;
    }
    
    private String constructJdbcUrl(String hostName, BigInteger port, String serviceId){
        return "jdbc:oracle:thin:@" 
                + hostName + ":" 
                + port.toString() + ":" 
                + serviceId;
    }
    public void createInsertStatement() throws Exception {
        /*
        if (args.length < 1) {
            usage();
            System.exit(1);
        }

        int i = 0;
        String tableName = this.tableName;
        String fileName = null;
        if (tableName.contains("/")) { // username/password provided
            String[] uid_pass = args[0].split("/");
            if ((uid_pass.length != 2) || (args.length < 2)) {
                usage();
                System.exit(1);
            }
            JDBC_USER = uid_pass[0];
            JDBC_PASSWD = uid_pass[1];
            i++;
            tableName = args[i];
        }
        */
        /*
        if ("-f".equals(tableName)) {
            tableName = null;
            if (args.length < (i + 2)) {
                usage();
                System.exit(1);
            }
            fileName = args[i + 1];
        }
        */
        
        Class.forName(this.jdbcDriver);
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(this.jdbcUrl, this.jdbcUserName, this.jdbcPassword);
            if (tableName != null) {
                generateInsertStatements(conn, tableName);
            }
            else {
                /*
                PrintWriter p = new PrintWriter(new FileWriter("insert_all.sql"));
                p.println("spool insert_all.log");

                //BufferedReader r = new BufferedReader(new FileReader(fileName));
                tableName = r.readLine();
                while (tableName != null) {
                    p.println(String.format("@%s_insert.sql", tableName));
                    generateInsertStatements(conn, tableName);
                    tableName = r.readLine();
                }
                r.close();

                p.println("spool off");
                p.close();
                        */
            }
        }
        finally {
            if (conn != null) conn.close();
        }
    }

    private static void generateInsertStatements(Connection conn, String tableName) 
                        throws Exception {
        log("Generating Insert statements for: " + tableName);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName); 
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
        PrintWriter p = new PrintWriter(new FileWriter(tableName + "_insert.sql"));
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
                        if (d == null) d = rs.getTime(i + 1);
                    case Types.TIMESTAMP:
                        if (d == null) d = rs.getTimestamp(i + 1);

                        if (d == null) {
                            columnValues += "null";
                        }
                        else {
                            columnValues += "TO_DATE('"
                                      + dateFormat.format(d)
                                      + "', 'YYYY/MM/DD HH24:MI:SS')";
                        }
                        break;

                    default:
                        v = rs.getString(i + 1);
                        if (v != null) {
                            columnValues += "'" + v.replaceAll("'", "''") + "'";
                        }
                        else {
                            columnValues += "null";
                        }
                        break;
                }
            }
            p.println(String.format("INSERT INTO %s (%s) values (%s)\n/", 
                                    tableName,
                                    columnNames,
                                    columnValues));
        }
        p.close();
    }

    private static void generateOneRowSampleInsertStatement(){
        
    }
    
    private static void log(String s) {
        System.out.println(s);
    }

    private static void usage() {
        System.out.println("java GenerateInsertStatements [username/password] tableName|-f fileName");
    }
}
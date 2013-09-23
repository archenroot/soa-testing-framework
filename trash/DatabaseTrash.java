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
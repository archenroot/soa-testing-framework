package ibm.soatest;


import java.util.EnumSet;
import java.util.Set;

public enum CompOperType {

        // Database related framework operations
        // Database related framework operations
        DB_OPERATIONS_DEFINITION_BEGIN("Dummy constant for database enumset EnumSet.range() begining."),
        
        DB_CHECK_NUMBER_OF_ROWS(SOATFCompType.DATABASE, "Check number of records in the database and log the result"),
        DB_EXECUTE_INSERT_FROM_FILE(SOATFCompType.DATABASE, "Execute INSERT statement over database from provided SQL script file"),
        DB_EXECUTE_RANDOMLY_GENERATED_INSERT(SOATFCompType.DATABASE, "Execute INSERT statement over database dynamically generated from database object structure"),
        DB_EXECUTE_SELECT_AND_EXPORT_TO_CSV(SOATFCompType.DATABASE, "Execute SELECT statement over database by provided SQL script"),
        DB_EXECUTE_UPDATE_FROM_FILE(SOATFCompType.DATABASE, "Execute UPDATE statement over database from provided SQL script file"),
        DB_EXECUTE_UPDATE_ONE_ROW_RANDOM(SOATFCompType.DATABASE, "Generate dynamically update statement with random values based on column definition for one row based on defined primary/unique key column and its value"),
        DB_EXECUTE_RANDOMLY_GENERATED_UPDATE_FOR_ALL_ROWS_COLUMNS_BY_KEY_COLUMN(SOATFCompType.DATABASE, "Generates dynamically update statements for all rows and columns for defined object based on defined primary/unique key column"),
        DB_GENERATE_INSERT_ONE_ROW_RANDOM(SOATFCompType.DATABASE, "Generate dynamicaly one row of INSERT sql statement for provided database object object"),
        DB_GENERATE_INSERT_ALL_ROWS(SOATFCompType.DATABASE, "Generate INSERT statement for all rows in provided database object"),
        DB_GENERATE_SELECT(SOATFCompType.DATABASE, "Generate SELECT statement for defined object in database"),
        
        
        DB_OPERATIONS_DEFINITION_END("Dummy constant for database operations EnumSet.range() end"),
        
        // FILE related framework operations
        FILE_OPERATIONS_DEFINITION_BEGIN("Dummy constant for FILE enumset EnumSet.range() begining"),
        // Not implemented yet
        FILE_OPERATIONS_DEFINITION_END("Dummy constant for FILE operations EnumSet.range() end"),
        
        // FTP related framework operations
        FTP_OPERATIONS_DEFINITION_BEGIN("Dummy constant for FTP enumset EnumSet.range() begining"),
        // Not implemented yet
        FTP_OPERATIONS_DEFINITION_END("Dummy constant for FTP operations EnumSet.range() end"),
        
        // JMS related freamework operations
        JMS_OPERATIONS_DEFINITION_BEGIN("Dummy constant for JMS enumset EnumSet.range() begining"),
        
        JMS_READ_MESSAGE_IN_QUEUE_BY_ID(SOATFCompType.JMS, "Read message in queue by provided id - the message will not be dropped from the queue and save it to file."),
        JMS_READ_ALL_MASSAGES_IN_QUEUE(SOATFCompType.JMS, "Read all messages in queue - the message will not be dropped from the queue and save them to files."),
        JMS_CHECK_NUMBER_OF_MESSAGES_IN_QUEUE(SOATFCompType.JMS, "Get number of messages in queue and log it."),
        JMS_CHECK_NUMBER_OF_MESSAGES_IN_TOPIC(SOATFCompType.JMS, "Get number of messages in topic and log it."),
        JMS_PURGE_QUEUE(SOATFCompType.JMS,"Delete all messages in queue."),
        JMS_PURGE_TOPIC(SOATFCompType.JMS,"Delete all messages in topic."),
        
        JMS_OPERATIONS_DEFINITION_END("Dummy constant for JMS operations EnumSet.range() end"),
        
        // OSB related framework operations
        OSB_OPERATIONS_DEFINITION_BEGIN("Dummy constant for OSB enumset EnumSet.range() begining"),
        
        OSB_ENABLE_BUSINESS_SERVICE(SOATFCompType.OSB,"Access the OSB cluster provided via configuration file and enable business service"),
        OSB_ENABLE_PROXY_SERVICE(SOATFCompType.OSB,"Access the OSB cluster provided via configuration file and enable proxy service"),
        OSB_DISABLE_BUSINESS_SERVICE(SOATFCompType.OSB,"Access the OSB cluster provided via configuration file and disable business service"),
        OSB_DISABLE_PROXY_SERVICE(SOATFCompType.OSB, "Access the OSB cluster provided via configuration file and disable business service"),
        
        OSB_OPERATIONS_DEFINITION_END("Dummy constant for OSB operations EnumSet.range() end"),
        
        // REST related framework operations
        REST_OPERATIONS_DEFINITION_BEGIN("Dummy constant for REST enumset EnumSet.range() begining"),
        REST_OPERATIONS_DEFINITION_END("Dummy constant for REST operations EnumSet.range() end"),
        
        // SOAP related framework operations
        SOAP_OPERATIONS_DEFINITION_BEGIN("Dummy constant for SOAP enumset EnumSet.range() begining"),
        SOAP_GENERATE_DYNAMICALLY_SOAP_REQUEST_TO_FILE(SOATFCompType.SOAP, "Read WSDL from service, generate default SOAP Envelope message and save it to file."),
        SOAP_INVOKE_SERVICE_WITH_PROVIDED_ENVELOPE(SOATFCompType.SOAP, "Read SOAP envelope message and use it for proxy/business or any WSDL based service invokation"),
        SOAP_VALIDATE_SOAP_REQUEST_FILE(SOATFCompType.SOAP, "Validate SOAP Envelope request message file"),
        SOAP_VALIDATE_SOAP_RESPONSE_FILE(SOATFCompType.SOAP, "Validate SOAP Envelope response message file"),
        SOAP_OPERATIONS_DEFINITION_END("Dummy constant for SOAP operations EnumSet.range() end"),
        
        // TOOL related framework operations
        TOOL_OPERATIONS_DEFINITION_BEGIN("Dummy constant for TOOL enumset EnumSet.range() begining."),
        TOOL_CHECK_XML_FILE_AGAINST_DATABASE_ONE_ROW_SIMPLE_MAPPING(SOATFCompType.TOOL, "Check xml file against row in database using 1:1 simple mapping."),
        TOOL_EMAIL_RESULT("Send result of the test to configured email address."),
        TOOL_CHECK_XML_FILE_AGAINST_DATABASE_ONE_ROW_CUSTOM_MAPPING(SOATFCompType.TOOL, "Check xml file against row in database using custom mapping provided via mapping file."),
        TOOL_OPERATIONS_DEFINITION_END("Dummy constant for TOOL operations EnumSet.range() end."),
        
        // XML related framework operations
        XML_OPERATIONS_DEFINITION_BEGIN("Dummy constant for XML enumset EnumSet.range() begining."),
        
        XML_VALIDATE_FILE(SOATFCompType.TOOL,"Validate XML file against provided schema."),
               
        XML_OPERATIONS_DEFINITION_END("Dummy constant for XML operations EnumSet.range() end.");

        public static Set<CompOperType> databaseOperations = EnumSet.of(DB_OPERATIONS_DEFINITION_BEGIN, DB_OPERATIONS_DEFINITION_END);
        public static Set<CompOperType> fileOperations = EnumSet.of(FILE_OPERATIONS_DEFINITION_BEGIN, FILE_OPERATIONS_DEFINITION_END);
        public static Set<CompOperType> ftpOperations = EnumSet.of(FTP_OPERATIONS_DEFINITION_BEGIN, FTP_OPERATIONS_DEFINITION_END);
        public static Set<CompOperType> jmsOperations = EnumSet.of(JMS_OPERATIONS_DEFINITION_BEGIN, JMS_OPERATIONS_DEFINITION_END);
        public static Set<CompOperType> osbOperations = EnumSet.of(OSB_OPERATIONS_DEFINITION_BEGIN, SOAP_OPERATIONS_DEFINITION_END);
        public static Set<CompOperType> restOperations = EnumSet.of(REST_OPERATIONS_DEFINITION_BEGIN, REST_OPERATIONS_DEFINITION_END);
        public static Set<CompOperType> soapOperations = EnumSet.of(SOAP_OPERATIONS_DEFINITION_BEGIN, SOAP_OPERATIONS_DEFINITION_END);
        public static Set<CompOperType> xmlOperations = EnumSet.of(XML_OPERATIONS_DEFINITION_BEGIN, XML_OPERATIONS_DEFINITION_END);
        
        //public static Set<FrameworkOperation> fileOperations = EnumSet.range();
        private String soaTestingFrameworkTypeDescription;
        private String frameworkOperationDesription;

        private CompOperType(String operationDescription) {
            this.frameworkOperationDesription = operationDescription;
        }

        private CompOperType(SOATFCompType type, String operationDescription) {
            this.soaTestingFrameworkTypeDescription = type.getDisplayName();
            this.frameworkOperationDesription = operationDescription;
        }

        public String getSoaTestingFrameworkTypeDescription() {
            return this.soaTestingFrameworkTypeDescription;
        }
        public String getFrameworkOperationDesription(){
            return this.frameworkOperationDesription;
        }
    }
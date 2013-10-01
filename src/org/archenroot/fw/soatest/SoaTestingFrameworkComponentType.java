package org.archenroot.fw.soatest;

import java.util.EnumSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.archenroot.fw.soatest.database.DatabaseComponent;

public enum SoaTestingFrameworkComponentType {
    
    
    
    DATABASE("Database Testing Framework Type Domain"),
    FILE("File Testing Framework Type Domain"),
    FTP("FTP Testing Framework Type Domain"),
    JMS("JMS Testing Framework Type Domain"),
    OSB("OSB Testing Framework Type Domain"),
    REST("REST Testing Framework Type Domain"),
    SOAP("SOAP Testing Framework Type Domain"),
    TOOL("TOOL Testing Framework Type Domain"),
    XML("XML Testing Framework Type Domain");

    private static final Logger logger = LogManager.getLogger(DatabaseComponent.class.getName());
    
    private final String displayName;

    private SoaTestingFrameworkComponentType(final String displayDescription) {
        this.displayName = displayDescription;
    }

    public String getDisplayName() {
        return displayName;
    }

    public enum ComponentOperation {

        // Database related framework operations
        // Database related framework operations
        DATABASE_OPERATIONS_DEFINITION_BEGIN("Dummy constant for database enumset EnumSet.range() begining"),
        CHECK_NUMBER_OF_ROWS_IN_DATABASE(SoaTestingFrameworkComponentType.DATABASE, "Check number of records in the database and log the result"),
        EXECUTE_INSERT_FROM_FILE(SoaTestingFrameworkComponentType.DATABASE, "Execute INSERT statement over database from provided SQL script file"),
        EXECUTE_INSERT_DYNAMIC(SoaTestingFrameworkComponentType.DATABASE, "Execute INSERT statement over database dynamically generated from database object structure"),
        EXECUTE_SELECT_AND_EXPORT_TO_CSV(SoaTestingFrameworkComponentType.DATABASE, "Execute SELECT statement over database by provided SQL script"),
        EXECUTE_UPDATE_FROM_FILE(SoaTestingFrameworkComponentType.DATABASE, "Execute UPDATE statement over database from provided SQL script file"),
        GENERATE_INSERT_DYNAMICALLY_ONE_ROW(SoaTestingFrameworkComponentType.DATABASE, "Generate dynamicaly one row of INSERT sql statement for provided database object object"),
        GENRATE_INSERT_ALL_EXISTING_ROWS(SoaTestingFrameworkComponentType.DATABASE, "Generate INSERT statement for all rows in provided database object"),
        GENERATE_SELECT(SoaTestingFrameworkComponentType.DATABASE, "Generate SELECT statement for provided object in database"),
        UPDATE_DYNAMIC_ONE_ROW(SoaTestingFrameworkComponentType.DATABASE, "Database insert operation"),
        UPDATE_DYNAMIC(SoaTestingFrameworkComponentType.DATABASE, "Database insert operation"),
        DATABASE_OPERATIONS_DEFINITION_END("Dummy constant for database operations EnumSet.range() end"),
        
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
        READ_NEW_MESSAGE_IN_QUEUE(SoaTestingFrameworkComponentType.JMS, "Read message in queue by provided id - the message will not be dropped from the queue"),
        READ_ALL_MASSAGES_IN_QUEUE(SoaTestingFrameworkComponentType.JMS, "Read all messages in queue - the message will not be dropped from the queue"),
        CHECK_NUMBER_OF_MESSAGES_IN_QUEUE(SoaTestingFrameworkComponentType.JMS, "Get number of messages in queue and log it"),
        CHECK_NUMBER_OF_MESSAGES_IN_TOPIC(SoaTestingFrameworkComponentType.JMS, "Get number of messages in topic and log it"),
        JMS_OPERATIONS_DEFINITION_END("Dummy constant for JMS operations EnumSet.range() end"),
        
        // OSB related framework operations
        OSB_OPERATIONS_DEFINITION_BEGIN("Dummy constant for OSB enumset EnumSet.range() begining"),
        ENABLE_BUSINESS_SERVICE(SoaTestingFrameworkComponentType.OSB,"Access the OSB cluster provided via configuration file and enable business service"),
        ENABLE_PROXY_SERVICE(SoaTestingFrameworkComponentType.OSB,"Access the OSB cluster provided via configuration file and enable proxy service"),
        DISABLE_BUSINESS_SERVICE(SoaTestingFrameworkComponentType.OSB,"Access the OSB cluster provided via configuration file and disable business service"),
        DISABLE_PROXY_SERVICE(SoaTestingFrameworkComponentType.OSB, "Access the OSB cluster provided via configuration file and disable business service"),
        OSB_OPERATIONS_DEFINITION_END("Dummy constant for OSB operations EnumSet.range() end"),
        
        // REST related framework operations
        REST_OPERATIONS_DEFINITION_BEGIN("Dummy constant for REST enumset EnumSet.range() begining"),
        REST_OPERATIONS_DEFINITION_END("Dummy constant for REST operations EnumSet.range() end"),
        
        // SOAP related framework operations
        SOAP_OPERATIONS_DEFINITION_BEGIN("Dummy constant for SOAP enumset EnumSet.range() begining"),
        GENERATE_DYNAMICALLY_SOAP_REQUEST_TO_FILE(SoaTestingFrameworkComponentType.SOAP, "Read WSDL from service, generate default SOAP Envelope message and save it to file."),
        INVOKE_SERVICE_WITH_PROVIDED_ENVELOPE(SoaTestingFrameworkComponentType.SOAP, "Read SOAP envelope message and use it for proxy/business or any WSDL based service invokation"),
        VALIDATE_SOAP_REQUEST_FILE(SoaTestingFrameworkComponentType.SOAP, "Validate SOAP Envelope request message file"),
        VALIDATE_SOAP_RESPONSE_FILE(SoaTestingFrameworkComponentType.SOAP, "Validate SOAP Envelope response message file"),
        SOAP_OPERATIONS_DEFINITION_END("Dummy constant for SOAP operations EnumSet.range() end"),
        
        // TOOL related framework operations
        TOOL_OPERATIONS_DEFINITION_BEGIN("Dummy constant for TOOL enumset EnumSet.range() begining"),
        CHECK_XML_FILE_AGAINST_DATABASE_ROW_SIMPLE_MAPPING(SoaTestingFrameworkComponentType.TOOL, "Check xml file against row in database using 1:1 simple mapping"),
        TOOL_OPERATIONS_DEFINITION_END("Dummy constant for TOOL operations EnumSet.range() end"),
        
        // XML related framework operations
        XML_OPERATIONS_DEFINITION_BEGIN("Dummy constant for XML enumset EnumSet.range() begining"),
        XML_OPERATIONS_DEFINITION_END("Dummy constant for XML operations EnumSet.range() end");

        public static Set<ComponentOperation> databaseOperations = EnumSet.of(DATABASE_OPERATIONS_DEFINITION_BEGIN, DATABASE_OPERATIONS_DEFINITION_END);
        public static Set<ComponentOperation> fileOperations = EnumSet.of(FILE_OPERATIONS_DEFINITION_BEGIN, FILE_OPERATIONS_DEFINITION_END);
        public static Set<ComponentOperation> ftpOperations = EnumSet.of(FTP_OPERATIONS_DEFINITION_BEGIN, FTP_OPERATIONS_DEFINITION_END);
        public static Set<ComponentOperation> jmsOperations = EnumSet.of(JMS_OPERATIONS_DEFINITION_BEGIN, JMS_OPERATIONS_DEFINITION_END);
        public static Set<ComponentOperation> osbOperations = EnumSet.of(OSB_OPERATIONS_DEFINITION_BEGIN, SOAP_OPERATIONS_DEFINITION_END);
        public static Set<ComponentOperation> restOperations = EnumSet.of(REST_OPERATIONS_DEFINITION_BEGIN, REST_OPERATIONS_DEFINITION_END);
        public static Set<ComponentOperation> soapOperations = EnumSet.of(SOAP_OPERATIONS_DEFINITION_BEGIN, SOAP_OPERATIONS_DEFINITION_END);
        public static Set<ComponentOperation> xmlOperations = EnumSet.of(XML_OPERATIONS_DEFINITION_BEGIN, XML_OPERATIONS_DEFINITION_END);

        //public static Set<FrameworkOperation> fileOperations = EnumSet.range();
        private String soaTestingFrameworkTypeDescription;
        private String frameworkOperationDesription;

        private ComponentOperation(String operationDescription) {
            this.frameworkOperationDesription = operationDescription;
        }

        private ComponentOperation(SoaTestingFrameworkComponentType type, String operationDescription) {
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
}

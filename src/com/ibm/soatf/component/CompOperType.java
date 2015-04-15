package com.ibm.soatf.component;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * This class need to be completely removed due to
 * duplicate configuration here and within the XML schemas. My proposal is to
 * delete this class and keep enumarions only within XML schema files.
 *
 * @author Ladislav Jech <archenroot@gmail.com>
 */
@Deprecated
public enum CompOperType {

    /**
     * Dummy constant for database enumset EnumSet.range() begining.
     */
    DB_OPERATIONS_DEFINITION_BEGIN("Dummy constant for database enumset EnumSet.range() begining."),
    /**
     * Check number of records in the database and log the result.
     */
    DB_CHECK_NUMBER_OF_ROWS(SOATFCompType.DATABASE, "Check number of records in the database and log the result."),
    /**
     * Check number of records in the database and log the result.
     */
    DB_EXECUTE_INSERT(SOATFCompType.DATABASE, "Execute INSERT statement over database from provided SQL script file."),
    /**
     * Execute INSERT statement over database from provided SQL script file.
     */
    DB_EXECUTE_RANDOMLY_GENERATED_INSERT(SOATFCompType.DATABASE, "Execute INSERT statement over database dynamically generated from database object structure."),
    /**
     * Execute SELECT statement over database by provided SQL script.
     */
    DB_EXECUTE_SELECT_AND_EXPORT_TO_CSV(SOATFCompType.DATABASE, "Execute SELECT statement over database by provided SQL script."),
    /**
     * Execute UPDATE statement over database from provided SQL script file.
     */
    DB_EXECUTE_UPDATE_FROM_FILE(SOATFCompType.DATABASE, "Execute UPDATE statement over database from provided SQL script file."),
    /**
     *
     */
    DB_CHECK_REPORTING(SOATFCompType.DATABASE, ""),
    /**
     *
     */
    DB_CHECK_RECORD_NOT_POOLED(SOATFCompType.DATABASE, ""),
    /**
     * Generate dynamically update statement with random values based on column
     * definition for one row based on defined primary/unique key column and its
     * value.
     */
    DB_EXECUTE_UPDATE_ONE_ROW_RANDOM(SOATFCompType.DATABASE, "Generate dynamically update statement with random values based on column definition for one row based on defined primary/unique key column and its value."),
    /**
     * Generate dynamically update statements for all rows and columns for
     * defined object based on defined primary/unique key column.
     */
    DB_EXECUTE_RANDOMLY_GENERATED_UPDATE_FOR_ALL_ROWS_COLUMNS_BY_KEY_COLUMN(SOATFCompType.DATABASE, "Generate dynamically update statements for all rows and columns for defined object based on defined primary/unique key column."),
    /**
     * Generate dynamicaly one row of INSERT sql statement for provided database
     * object object.
     */
    DB_GENERATE_INSERT_ONE_ROW_RANDOM(SOATFCompType.DATABASE, "Generate dynamicaly one row of INSERT sql statement for provided database object object."),
    /**
     * Generate INSERT statement for all rows in provided database object.
     */
    DB_GENERATE_INSERT_ALL_ROWS(SOATFCompType.DATABASE, "Generate INSERT statement for all rows in provided database object."),
    /**
     * Generate SELECT statement for defined object in database.
     */
    DB_GENERATE_SELECT(SOATFCompType.DATABASE, "Generate SELECT statement for defined object in database."),
    /**
     *
     */
    DB_CLEAR_REPORTING(SOATFCompType.DATABASE, ""),
    /**
     *
     */
    DB_DELETE_RECORD(SOATFCompType.DATABASE, ""),
    /**
     *
     */
    DB_INSERT_RECORD(SOATFCompType.DATABASE, ""),
    /**
     *
     */
    DB_CHECK_RECORD_POOLED(SOATFCompType.DATABASE, ""),
    /**
     * Dummy constant for database operations EnumSet.range() end.
     */
    DB_OPERATIONS_DEFINITION_END("Dummy constant for database operations EnumSet.range() end."),
    // FILE related framework operations

    /**
     * Dummy constant for FILE enumset EnumSet.range() begining.
     */
    FILE_OPERATIONS_DEFINITION_BEGIN("Dummy constant for FILE enumset EnumSet.range() begining."),
    // Not implemented yet

    /**
     * Dummy constant for FILE operations EnumSet.range() end.
     */
    FILE_OPERATIONS_DEFINITION_END("Dummy constant for FILE operations EnumSet.range() end."),
    // FTP related framework operations

    /**
     * Dummy constant for FTP enumset EnumSet.range() begining.
     */
    FTP_OPERATIONS_DEFINITION_BEGIN("Dummy constant for FTP enumset EnumSet.range() begining."),
    /**
     *
     */
    FTP_UPLOAD_FILE("Send file to FTP server."),
    /**
     *
     */
    FTP_DOWNLOAD_FILE("Receive file from FTP server."),
    /**
     *
     */
    FTP_SEARCH_FOR_FILE("Search for file/files on FTP server."),
    /**
     *
     */
    FTP_CHECK_DELIVERED_FOLDER_FOR_FILE("Search for file/files on FTP server."),
    /**
     *
     */
    FTP_CHECK_ERROR_FOLDER_FOR_FILE("Search for file/files on FTP server."),
    /**
     * Dummy constant for FTP operations EnumSet.range() end.
     */
    FTP_OPERATIONS_DEFINITION_END("Dummy constant for FTP operations EnumSet.range() end."),
    // JMS related freamework operations

    /**
     * Dummy constant for JMS enumset EnumSet.range() begining.
     */
    JMS_OPERATIONS_DEFINITION_BEGIN("Dummy constant for JMS enumset EnumSet.range() begining."),
    /**
     * Generate sample message based on provided XML schema file.
     */
    JMS_GENERATE_MESSAGE_FROM_XSD(SOATFCompType.JMS, "Generat sample message based on provided XML schema file."),
    /**
     * Pickup the sample message/s and send them to queue.
     */
    JMS_SEND_MESSAGE_TO_QUEUE(SOATFCompType.JMS, "Pickup the sample message/s and send them to queue."),
    /**
     * Read message in queue by provided id - the message will not be dropped
     * from the queue and save it to file.
     */
    JMS_READ_MESSAGE_IN_QUEUE_BY_ID(SOATFCompType.JMS, "Read message in queue by provided id - the message will not be dropped from the queue and save it to file."),
    /**
     * Read all messages in queue - the message will not be dropped from the
     * queue and save them to files.
     */
    JMS_RECEIVE_MESSAGE_FROM_QUEUE(SOATFCompType.JMS, "Read all messages in queue - the message will not be dropped from the queue and save them to files."),
    /**
     * Check error queue against content of stored messages.
     */
    JMS_CHECK_ERROR_QUEUE_FOR_MESSAGE(SOATFCompType.JMS, "Check error queue against content of stored messages."),
    /**
     * Get number of messages in queue and log it.
     */
    JMS_CHECK_NUMBER_OF_MESSAGES_IN_QUEUE(SOATFCompType.JMS, "Gets number of messages in queue and log it."),
    /**
     * Get number of messages in topic and log it.
     */
    JMS_CHECK_NUMBER_OF_MESSAGES_IN_TOPIC(SOATFCompType.JMS, "Get number of messages in topic and log it."),
    /**
     * Delete all messages in queue.
     */
    JMS_PURGE_QUEUE(SOATFCompType.JMS, "Delete all messages in queue."),
    /**
     * Delete all messages in topic.
     */
    JMS_PURGE_TOPIC(SOATFCompType.JMS, "Delete all messages in topic."),
    /**
     *
     */
    JMS_CHECK_REPORTING(SOATFCompType.JMS, ""),
    /**
     * Validate message file against provided schema.
     */
    JMS_VALIDATE_MESSAGE(SOATFCompType.JMS, "Validate message file against provided schema."),
    /**
     * Dummy constant for JMS operations EnumSet.range() end.
     */
    JMS_OPERATIONS_DEFINITION_END("Dummy constant for JMS operations EnumSet.range() end."),
    /**
     * Dummy constant for OSB enumset EnumSet.range() begining.
     */
    OSB_OPERATIONS_DEFINITION_BEGIN("Dummy constant for OSB enumset EnumSet.range() begining."),
    /**
     * Access the OSB cluster provided via configuration file and enable
     * business or proxy service.
     */
    OSB_ENABLE_SERVICE(SOATFCompType.OSB, "Access the OSB cluster provided via configuration file and enable business or proxy service."),
    /**
     * Access the OSB cluster provided via configuration file and disable
     * business or proxy service.
     */
    OSB_DISABLE_SERVICE(SOATFCompType.OSB, "Access the OSB cluster provided via configuration file and disable business or proxy service."),
    /**
     * Dummy constant for OSB operations EnumSet.range() end.
     */
    OSB_OPERATIONS_DEFINITION_END("Dummy constant for OSB operations EnumSet.range() end."),
    // REST related framework operations

    /**
     * Dummy constant for REST enumset EnumSet.range() begining.
     */
    REST_OPERATIONS_DEFINITION_BEGIN("Dummy constant for REST enumset EnumSet.range() begining."),
    /**
     * Dummy constant for REST operations EnumSet.range() end.
     */
    REST_OPERATIONS_DEFINITION_END("Dummy constant for REST operations EnumSet.range() end."),
    /**
     * Dummy constant for SOAP enumset EnumSet.range() begining.
     */
    SOAP_OPERATIONS_DEFINITION_BEGIN("Dummy constant for SOAP enumset EnumSet.range() begining."),
    /**
     * Read WSDL from service, generate default SOAP Envelope message and save
     * it to file.
     */
    SOAP_GENERATE_DYNAMICALLY_SOAP_REQUEST_TO_FILE(SOATFCompType.SOAP, "Read WSDL from service, generate default SOAP Envelope message and save it to file."),
    /**
     * Read SOAP envelope message and use it for proxy/business or any WSDL
     * based service invokation.
     */
    SOAP_INVOKE_SERVICE_WITH_PROVIDED_ENVELOPE(SOATFCompType.SOAP, "Read SOAP envelope message and use it for proxy/business or any WSDL based service invokation."),
    /**
     * Validate SOAP Envelope request message file.
     */
    SOAP_VALIDATE_SOAP_REQUEST_FILE(SOATFCompType.SOAP, "Validate SOAP Envelope request message file."),
    /**
     * Validate SOAP Envelope response message file.
     */
    SOAP_VALIDATE_SOAP_RESPONSE_FILE(SOATFCompType.SOAP, "Validate SOAP Envelope response message file."),
    /**
     * Dummy constant for SOAP operations EnumSet.range() end.
     */
    SOAP_OPERATIONS_DEFINITION_END("Dummy constant for SOAP operations EnumSet.range() end."),
    // TOOL related framework operations

    /**
     *
     */
    UTIL_DELAY(SOATFCompType.SOAP, ""),
    /**
     * Dummy constant for TOOL enumset EnumSet.range() begining.
     */
    TOOL_OPERATIONS_DEFINITION_BEGIN("Dummy constant for TOOL enumset EnumSet.range() begining."),
    /**
     * Check xml file against row in database using 1:1 simple mapping.
     */
    TOOL_CHECK_XML_FILE_AGAINST_DATABASE_ONE_ROW_SIMPLE_MAPPING(SOATFCompType.TOOL, "Check xml file against row in database using 1:1 simple mapping."),
    /**
     * Send result of the test to configured email address.
     *
     */
    TOOL_EMAIL_RESULT("Send result of the test to configured email address."),
    /**
     * Check xml file against row in database using custom mapping provided via
     * mapping file.
     */
    TOOL_CHECK_XML_FILE_AGAINST_DATABASE_ONE_ROW_CUSTOM_MAPPING(SOATFCompType.TOOL, "Check xml file against row in database using custom mapping provided via mapping file."),
    /**
     * Dummy constant for TOOL operations EnumSet.range() end.
     *
     */
    TOOL_OPERATIONS_DEFINITION_END("Dummy constant for TOOL operations EnumSet.range() end."),
    /**
     * Dummy constant for XML enumset EnumSet.range() begining.
     *
     */
    XML_OPERATIONS_DEFINITION_BEGIN("Dummy constant for XML enumset EnumSet.range() begining."),
    /**
     * Dummy constant for XML operations EnumSet.range() end.
     *
     */
    XML_OPERATIONS_DEFINITION_END("Dummy constant for XML operations EnumSet.range() end."),
    /**
     * Dummy constant for MAPPING enumset EnumSet.range() begining.
     *
     */
    MAPPING_OPERATIONS_DEFINITION_BEGIN("Dummy constant for MAPPING enumset EnumSet.range() begining."),
    /**
     * Compare results of two given component outputs.
     *
     */
    MAPPING_VALIDATE_SCENARIO(SOATFCompType.TOOL, "Compare results of two given component outputs."),
    /**
     * Dummy constant for MAPPING operations EnumSet.range() end.
     *
     */
    MAPPING_OPERATIONS_DEFINITION_END("Dummy constant for MAPPING operations EnumSet.range() end."),
    /**
     *
     */
    UTIL_CLEAR_REPORTING(SOATFCompType.UTIL, ""),
    /**
     *
     */
    UTIL_CHECK_REPORTING_FOR_SUCCESS(SOATFCompType.UTIL, ""),
    /**
     *
     */
    UTIL_CHECK_REPORTING_FOR_FAILURE(SOATFCompType.UTIL, ""),
    /**
     *
     */
    REPORT_OPERATIONS_DEFINITION_BEGIN("Dummy constant for REPORT enumset EnumSet.range() begining."),
    /**
     *
     */
    REPORT_GENERATE(SOATFCompType.REPORT, "Compare results of two given component outputs."),
    /**
     *
     */
    REPORT_OPERATIONS_DEFINITION_END("Dummy constant for REPORT operations EnumSet.range() end.");

    /**
     * Constant with all supported database related operations.
     */
    public static final Set<CompOperType> DATABASE_OPERATIONS = getOperations(DB_OPERATIONS_DEFINITION_BEGIN, DB_OPERATIONS_DEFINITION_END);

    /**
     * Constant with all supported file related operations.
     */
    public static final Set<CompOperType> FILE_OPERATIONS = getOperations(FILE_OPERATIONS_DEFINITION_BEGIN, FILE_OPERATIONS_DEFINITION_END);

    /**
     * Constant with all supported FTP related operations.
     */
    public static final Set<CompOperType> FTP_OPERATIONS = getOperations(FTP_OPERATIONS_DEFINITION_BEGIN, FTP_OPERATIONS_DEFINITION_END);

    /**
     * Constant with all supported JMS related operations.
     */
    public static final Set<CompOperType> JMS_OPERATIONS = getOperations(JMS_OPERATIONS_DEFINITION_BEGIN, JMS_OPERATIONS_DEFINITION_END);

    /**
     * Constant with all supported OSB related operations.
     */
    public static final Set<CompOperType> OSB_OPERATIONS = getOperations(OSB_OPERATIONS_DEFINITION_BEGIN, OSB_OPERATIONS_DEFINITION_END);

    /**
     * Constant with all supported REST related operations.
     */
    public static final Set<CompOperType> REST_OPERATIONS = getOperations(REST_OPERATIONS_DEFINITION_BEGIN, REST_OPERATIONS_DEFINITION_END);

    /**
     * Constant with all supported SOAP related operations.
     */
    public static final Set<CompOperType> SOAP_OPERATIONS = getOperations(SOAP_OPERATIONS_DEFINITION_BEGIN, SOAP_OPERATIONS_DEFINITION_END);

    /**
     * Constant with all supported XML related operations.
     */
    public static final Set<CompOperType> XML_OPERATIONS = getOperations(XML_OPERATIONS_DEFINITION_BEGIN, XML_OPERATIONS_DEFINITION_END);

    /**
     * Constant with all supported mapping related operations.
     */
    public static final Set<CompOperType> MAPPING_OPERATIONS = getOperations(MAPPING_OPERATIONS_DEFINITION_BEGIN, MAPPING_OPERATIONS_DEFINITION_END);

    private String soaTestingFrameworkTypeDescription;
    private String frameworkOperationDesription;

    private CompOperType(String operationDescription) {
        this.frameworkOperationDesription = operationDescription;
    }

    private CompOperType(SOATFCompType type, String operationDescription) {
        this.soaTestingFrameworkTypeDescription = type.getDisplayName();
        this.frameworkOperationDesription = operationDescription;
    }

    /**
     * Returns TODO!!! Need to revise all private attributes if some of them are
     * not meaningless!!!
     *
     * @return
     */
    public String getSoaTestingFrameworkTypeDescription() {
        return this.soaTestingFrameworkTypeDescription;
    }

    /**
     * Returns description for the selected operation type.
     *
     * @return
     */
    public String getFrameworkOperationDesription() {
        return this.frameworkOperationDesription;
    }

    /**
     * Returns an unmodifiable set consisting of elements in range between
     * <code>first</code> and <code>last</code> enum elements exclusive
     *
     * @param first enum element defining the beginning of the range
     * @param last enum element defining the end of the range
     * @return unmodifiable set of elements in the specified range (excluding
     * the <code>first</code> and <code>last</code>
     */
    private static Set<CompOperType> getOperations(CompOperType first, CompOperType last) {
        Set<CompOperType> set = EnumSet.range(first, last);
        set.remove(first);
        set.remove(last);
        return Collections.unmodifiableSet(set);
    }

    /**
     * Returns a component type extracted from the operation type. Each
     * operation type has strict relationship to component type.
     *
     * @param cot Component operation type
     * @return SOA Testing Framework component type
     */
    protected static SOATFCompType getComponentType(CompOperType cot) {

        if (DATABASE_OPERATIONS.contains(cot)) {
            return SOATFCompType.DATABASE;
        }
        if (FILE_OPERATIONS.contains(cot)) {
            return SOATFCompType.FILE;
        }
        if (FTP_OPERATIONS.contains(cot)) {
            return SOATFCompType.FTP;
        }
        if (JMS_OPERATIONS.contains(cot)) {
            return SOATFCompType.JMS;
        }
        if (OSB_OPERATIONS.contains(cot)) {
            return SOATFCompType.OSB;
        }
        if (REST_OPERATIONS.contains(cot)) {
            return SOATFCompType.REST;
        }
        if (SOAP_OPERATIONS.contains(cot)) {
            return SOATFCompType.SOAP;
        }
        if (XML_OPERATIONS.contains(cot)) {
            return SOATFCompType.XML;
        }
        if (MAPPING_OPERATIONS.contains(cot)) {
            return SOATFCompType.MAPPING;
        }

        return null;
    }

}

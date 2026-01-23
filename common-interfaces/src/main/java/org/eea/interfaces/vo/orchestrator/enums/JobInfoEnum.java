package org.eea.interfaces.vo.orchestrator.enums;

public enum JobInfoEnum {
    ERROR_WRONG_FILE_NAME("File name does not match any table name. Please correct your file name and try again."),

    ERROR_NO_HEADERS_MATCHING("The headers of the import file do not match the field names of the table. Please correct your file and try importing again."),

    ERROR_WRONG_DELIMITER_SIZE("The delimiter used in the file must be a single character (e.g., comma, tab). Please adjust your file format and try again."),

    ERROR_NOT_REPORTABLE_DATASET("Import is not allowed for this dataset."),

    ERROR_UPDATING_PROCESS("Import failed  because a process could not be updated due to a system error. Please try again or contact the Service Desk."),

    ERROR_EMPTY_ZIP("The ZIP file you uploaded is empty. Please include at least one valid file and try again."),

    ERROR_EMPTY_FILENAME("We couldn’t detect the file name. Please check your file and try importing again."),

    WARNING_SOME_FILENAMES_DO_NOT_MATCH_TABLES("Some of the imported files do not match the name of the dataset's tables, so certain tables were not populated with data."),

    ERROR_ALL_FILES_ARE_EMPTY("The uploaded files do not contain any records. Please ensure your files contain data and try again."),

    WARNING_SOME_FILES_ARE_EMPTY("One or more of your import files are empty, so some tables could not be populated with data."),

    ERROR_CSV_ILLEGAL_CHARACTERS("An illegal character was found in the imported file. Please remove any special or non-standard characters and try again."),

    ERROR_CSV_MULTIPLE_QUOTES("We found formatting issues with quotation marks in your file. Please correct them before importing."),

    ERROR_CSV_MULTIPLE_QUOTES_WITH_LINE_NUM("Line %d in your file contains incorrectly formatted quotation marks. Please correct it and try again."),

    WARNING_SOME_IMPORT_FAILED_FIXED_NUM_WITHOUT_REPLACE_DATA("Some imports to fixed-size tables failed because the Replace Data option was not selected. As a result, those tables were not populated."),

    ERROR_IMPORT_FAILED_FIXED_NUM_WITHOUT_REPLACE_DATA("Import failed because all tables to be imported have a fixed number of records and the Replace Data option was not selected. Please select it and try again."),

    WARNING_SOME_IMPORT_FAILED_WRONG_NUM_OF_RECORDS("Some imports to fixed-size tables failed because the number of records in the files was incorrect. As a result, those tables were not populated."),

    ERROR_IMPORT_FAILED_WRONG_NUM_OF_RECORDS("Import failed for fixed-size tables because the number of records in the files was incorrect. Please verify and adjust your data."),

    WARNING_SOME_IMPORT_FAILED_ONLY_READ_ONLY_FIELDS("Some tables couldn’t be updated because all their fields are read-only."),

    ERROR_IMPORT_FAILED_ONLY_READ_ONLY_FIELDS("Import failed because all fields in the tables are read-only."),

    WARNING_SOME_IMPORT_FAILED_READ_ONLY_TABLES("Some tables are read-only and could not be updated during the import."),

    ERROR_IMPORT_FAILED_READ_ONLY_TABLES("Import failed because the tables are read only."),

    WARNING_SOME_IMPORT_MISMATCH_OF_DATA("Some rows in your files have more values than expected columns. As a result those files were not imported."),

    ERROR_DREMIO_ENDPOINT_RESPONSE("Import failed due to a system error while storing the data. Please try again or contact the Service Desk."),

    ERROR_COULD_NOT_UPLOAD_FILE_TO_PUBLIC_S3("Import failed because the file could not be uploaded to our cloud storage. Please try again or contact the Service Desk."),

    ERROR_ICEBERG_TABLE_EXISTS("The job failed because editing is currently enabled for the dataset. Please disable editing and try again."),

    WARNING_SOME_IMPORT_FILES_CONTAIN_WRONG_HEADERS("Some import files have headers that do not exactly match the field names of the reportnet tables. As a result those files were not imported."),

    ERROR_IMPORT_FILES_CONTAIN_WRONG_HEADERS("Import files contain incorrect headers. Please ensure the headers in your files exactly match the field names of the corresponding tables."),

    ERROR_ETL_EXPORT_V4_CITUS("ETL Export v4 isn’t compatible with non–big data dataflows. Please use a supported export version."),

    ERROR_ETL_EXPORT_V5_CITUS("ETL Export v5 isn’t compatible with non–big data dataflows. Please use a supported export version."),

    ERROR_RELEASE_CANCELED_BLOCKERS("There are canceled validation tasks for blocker checks. Please review and address the issues before retrying."),

    ERROR_MATERIALIZED_VIEWS_ARE_NOT_CORRECT("The necessary reportnet data views could not be created. Please try again or contact the Service Desk."),

    WARNING_HAS_CANCELED_VALIDATION_TASKS("There are canceled validation tasks. Please review and address the issues before retrying."),
    
    IMPORT_JOB_FAILED_STUCK_QUEUED("The import job failed because it was stuck in status QUEUED for a long time. Please try again."),

    IMPORT_JOB_RESTART_FAILED("The import job could not be completed as it became stuck following a restart. Please try again or contact the Service Desk."),

    ERROR_NO_FILE_IN_S3("The import job failed because no imported file was found in the cloud storage. Please try again."),

    ERROR_NO_FILE_RETURNED_FROM_FME("The import job failed because no file returned from FME."),

    ERROR_VALIDATION_FAILURE("Validation failed due to system error. Please contact the Service Desk for support."),

    ERROR_ILLEGAL_HEADER_CHARACTER("Validation failed due to an illegal character found in a field name of the table."),

    ERROR_RELEASE_PARTIALLY_COMPLETED("Not all datasets were able to release. Please try again or contact the Service Desk"),

    ERROR_DATASET_IS_LOCKED_FOR_EDITING("Dataset is locked for editing"),

    ERROR_IMPORT_FAILED_READ_ONLY_TABLE("Import failed because the table is read only."),

    ERROR_IMPORT_FAILED_FIXED_NUM("Import failed because the table has fixed number of records."),

    ERROR_IMPORT_FAILED_READ_ONLY_FIELDS("Import failed because the table contains read only fields."),

    ERROR_IMPORT_FAILED_FILE_NOT_ZIP("Import failed because the file is not zip."),

    ERROR_ZIP_FOLDER_WITHOUT_CSV_FILES("The ZIP file you uploaded does not contain any csv files for import. Please include at least one csv file and try again.");


    /** The value. */
    private final String value;

    /**
     * Instantiates a new job info.
     *
     * @param value the value
     */
    JobInfoEnum(String value) {
        this.value = value;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue(Integer lineNumber) {
        if(lineNumber != null){
            return String.format(value, lineNumber);
        }
        return value;
    }
}

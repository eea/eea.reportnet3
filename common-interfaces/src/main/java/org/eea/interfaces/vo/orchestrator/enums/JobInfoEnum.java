package org.eea.interfaces.vo.orchestrator.enums;

public enum JobInfoEnum {
    ERROR_WRONG_FILE_NAME("File name does not match any table name"),

    ERROR_NO_HEADERS_MATCHING("No headers matching FieldSchemas"),

    ERROR_WRONG_DELIMITER_SIZE("The size of the delimiter cannot be greater than 1"),

    ERROR_NOT_REPORTABLE_DATASET("Dataset is not reportable"),

    ERROR_UPDATING_PROCESS("Could not update process"),

    ERROR_EMPTY_ZIP("Empty zip file"),

    ERROR_EMPTY_FILENAME("Could not retrieve file name"),

    WARNING_SOME_FILENAMES_DO_NOT_MATCH_TABLES("Some of the imported files do not match the name of the dataset's tables");

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
    public String getValue() {
        return value;
    }
}

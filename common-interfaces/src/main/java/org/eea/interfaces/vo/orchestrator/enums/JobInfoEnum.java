package org.eea.interfaces.vo.orchestrator.enums;

public enum JobInfoEnum {
    ERROR_WRONG_FILE_NAME("File name does not match any table name"),

    ERROR_NO_HEADERS_MATCHING("No headers matching FieldSchemas");

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

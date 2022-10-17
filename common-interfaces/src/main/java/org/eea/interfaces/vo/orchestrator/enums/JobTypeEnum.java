package org.eea.interfaces.vo.orchestrator.enums;

public enum JobTypeEnum {

    /** The import type. */
    IMPORT("IMPORT"),

    /** The validation type. */
    VALIDATION("VALIDATION"),

    /** The release type. */
    RELEASE("RELEASE");

    /** The value. */
    private final String value;

    /**
     * Instantiates a new job type.
     *
     * @param value the value
     */
    JobTypeEnum(String value) {
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

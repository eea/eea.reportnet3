package org.eea.interfaces.vo.orchestrator.enums;

public enum JobTypeEnum {

    /** The import type. */
    IMPORT("IMPORT"),

    /** The validation type. */
    VALIDATION("VALIDATION"),

    /** The release type. */
    RELEASE("RELEASE"),

    /** The export type. */
    EXPORT("EXPORT"),

    /** The copy to eu dataset type. */
    COPY_TO_EU_DATASET("COPY_TO_EU_DATASET"),

    /** The file export type. */
    FILE_EXPORT("FILE_EXPORT"),

    /** The etl import type. */
    ETL_IMPORT("ETL_IMPORT");

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

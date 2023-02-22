package org.eea.interfaces.vo.orchestrator.enums;

public enum JobStatusEnum {

    /** The queued. */
    QUEUED("QUEUED"),

    /** The in progress. */
    IN_PROGRESS("IN_PROGRESS"),

    /** The refused. */
    REFUSED("REFUSED"),

    /** The canceled. */
    CANCELED("CANCELED"),

    /** The failed. */
    FAILED("FAILED"),

    /** The finished. */
    FINISHED("FINISHED"),

    /** The canceled by admin */
    CANCELED_BY_ADMIN("CANCELED_BY_ADMIN");

    /** The value. */
    private final String value;

    /**
     * Instantiates a new job status.
     *
     * @param value the value
     */
    JobStatusEnum(String value) {
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

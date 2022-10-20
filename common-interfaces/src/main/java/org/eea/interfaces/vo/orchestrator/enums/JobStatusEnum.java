package org.eea.interfaces.vo.orchestrator.enums;

public enum JobStatusEnum {

    /** The created. */
    CREATED("CREATED"),

    /** The queued. */
    QUEUED("QUEUED"),

    /** The in progress. */
    IN_PROGRESS("IN_PROGRESS"),

    /** The aborted. */
    ABORTED("ABORTED"),

    /** The success. */
    SUCCESS("SUCCESS"),

    /** The job failure. */
    FAILURE("FAILURE");

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

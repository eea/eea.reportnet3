package org.eea.interfaces.vo.orchestrator.enums;

public enum FmeJobStatusEnum {

    SUBMITTED("SUBMITTED"),
    QUEUED("QUEUED"),
    PULLED("PULLED"),
    ABORTED("ABORTED"),
    FME_FAILURE("FME_FAILURE"),
    JOB_FAILURE("JOB_FAILURE"),
    SUCCESS("SUCCESS");


    /** The value. */
    private final String value;

    /**
     * Instantiates a new job status.
     *
     * @param value the value
     */
    FmeJobStatusEnum(String value) {
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

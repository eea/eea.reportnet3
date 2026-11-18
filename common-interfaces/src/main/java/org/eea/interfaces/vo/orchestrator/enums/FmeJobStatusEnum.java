package org.eea.interfaces.vo.orchestrator.enums;

public enum FmeJobStatusEnum {

    // Legacy FME v3 statuses, kept so existing FME_STATUS DB values (EnumType.STRING) still
    // deserialize
    SUBMITTED("SUBMITTED"),
    PULLED("PULLED"),
    ABORTED("ABORTED"),
    FME_FAILURE("FME_FAILURE"),
    JOB_FAILURE("JOB_FAILURE"),

    // FME v4 statuses (FME REST API V4, JobResponse.status enum: queued, running, success,
    // failure, cancelled).
    QUEUED("QUEUED"),
    RUNNING("RUNNING"),
    SUCCESS("SUCCESS"),
    FAILURE("FAILURE"),
    CANCELLED("CANCELLED");


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

    /**
     * Resolves an FME v4 API status string (e.g. "queued", "failure") to its enum constant,
     * matching case-insensitively since the v4 API returns lowercase values.
     *
     * @param apiStatus the status string as returned by the FME REST API
     * @return the matching enum constant
     * @throws IllegalArgumentException if no constant matches
     */
    public static FmeJobStatusEnum fromApiValue(String apiStatus) {
        for (FmeJobStatusEnum status : values()) {
            if (status.name().equalsIgnoreCase(apiStatus)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown FME job status: " + apiStatus);
    }
}

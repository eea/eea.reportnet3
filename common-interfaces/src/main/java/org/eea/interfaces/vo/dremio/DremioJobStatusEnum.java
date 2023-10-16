package org.eea.interfaces.vo.dremio;

public enum DremioJobStatusEnum {

    COMPLETED("COMPLETED"),
    CANCELED("CANCELED"),
    FAILED("FAILED"),
    NOT_SUBMITTED("NOT_SUBMITTED"),
    STARTING("STARTING"),
    RUNNING("RUNNING"),
    CANCELLATION_REQUESTED("CANCELLATION_REQUESTED"),
    PLANNING("PLANNING"),
    PENDING("PENDING"),
    METADATA_RETRIEVAL("METADATA_RETRIEVAL"),
    QUEUED("QUEUED"),
    ENGINE_START("ENGINE_START"),
    EXECUTION_PLANNING("EXECUTION_PLANNING"),
    INVALID_STATE("INVALID_STATE");

    private final String value;
    DremioJobStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}

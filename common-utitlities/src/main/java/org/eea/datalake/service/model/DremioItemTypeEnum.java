package org.eea.datalake.service.model;

public enum DremioItemTypeEnum {

    DATASET("DATASET"),
    CONTAINER("CONTAINER");

    private final String value;
    DremioItemTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

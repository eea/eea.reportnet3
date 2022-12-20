package org.eea.interfaces.vo.metabase;

public enum TaskType {


    VALIDATION_TASK("VALIDATION_TASK"),
    IMPORT_TASK("IMPORT_TASK"),
    RELEASE_TASK("RELEASE_TASK"),
    COPY_TO_EU_DATASET_TASK("COPY_TO_EU_DATASET_TASK"),
    RESTORE_REPORTING_DATASET_TASK("RESTORE_REPORTING_DATASET_TASK"),
    RESTORE_DESIGN_DATASET_TASK("RESTORE_DESIGN_DATASET_TASK");

    private final String value;

    /**
     * Instantiates a new type status enum.
     *
     * @param value the value
     */
    TaskType(String value) {
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

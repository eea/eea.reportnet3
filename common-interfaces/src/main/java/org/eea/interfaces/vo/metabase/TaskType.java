package org.eea.interfaces.vo.metabase;

public enum TaskType {


    VALIDATION_TASK("VALIDATION_TASK"),
    IMPORT_TASK("IMPORT_TASK");

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

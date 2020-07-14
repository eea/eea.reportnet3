package org.eea.interfaces.vo.ums.enums;

import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The enum Resource enum.
 */
public enum ResourceTypeEnum {
  /**
   * Dashboard resource enum.
   */
  DASHBOARD("Dashboard"),
  /**
   * Data collection resource enum.
   */
  DATA_COLLECTION("DataCollection"),
  /**
   * Dataflow resource enum.
   */
  DATAFLOW("Dataflow"),
  /**
   * Data schema resource enum.
   */
  DATA_SCHEMA("Dataschema"),
  /**
   * Document resource enum.
   */
  DOCUMENT("Document"),
  /**
   * Dataset resource enum.
   */
  DATASET("Dataset"),

  /** The eu dataset. */
  EU_DATASET("EUDataset"),
  /**
   * Join request resource enum.
   */
  JOIN_REQUEST("JoinRequest"),
  /**
   * Legal instrument resource enum.
   */
  LEGAL_INSTRUMENT("Legalinstrument"),
  /**
   * Partition dataset resource enum.
   */
  PARTITION_DATASET("PartitionDataset"),
  /**
   * Submission agreement resource enum.
   */
  SUBMISSION_AGREEMENT("SubmissionAgreement"),
  /**
   * Validation definition resource enum.
   */
  VALIDATION_DEFINITION("ValidationDefinition"),
  /**
   * Validation result resource enum.
   */
  VALIDATION_RESULT("ValidationResult");

  private final String resource;

  private ResourceTypeEnum(String resource) {
    this.resource = resource;
  }

  @Override
  @JsonValue
  public String toString() {
    return this.resource;
  }

  /**
   * From value resource enum.
   *
   * @param value the value
   *
   * @return the resource enum
   */
  @JsonCreator
  public static ResourceTypeEnum fromValue(String value) {
    return Arrays.stream(ResourceTypeEnum.values()).filter(e -> e.resource.equals(value))
        .findFirst().get();
  }

}

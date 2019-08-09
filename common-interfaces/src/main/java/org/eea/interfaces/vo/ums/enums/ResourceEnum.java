package org.eea.interfaces.vo.ums.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

/**
 * The enum Resource enum.
 */
public enum ResourceEnum {
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
  /**
   * Eu dataset resource enum.
   */
  EU_DATASET("EuDataset"),
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

  private ResourceEnum(String resource) {
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
  public static ResourceEnum fromValue(String value) {
    return Arrays.stream(ResourceEnum.values()).filter(e -> e.resource.equals(value)).findFirst()
        .get();
  }

}

package org.eea.interfaces.vo.ums.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum ResourceEnum {
  DASHBOARD("Dashboard"),
  DATA_COLLECTION("DataCollection"),
  DATAFLOW("Dataflow"),
  DATA_SCHEMA("Dataschema"),
  DOCUMENT("Document"),
  DATASET("Dataset"),
  EU_DATASET("EuDataset"),
  JOIN_REQUEST("JoinRequest"),
  LEGAL_INSTRUMENT("Legalinstrument"),
  PARTITION_DATASET("PartitionDataset"),
  SUBMISSION_AGREEMENT("SubmissionAgreement"),
  VALIDATION_DEFINITION("ValidationDefinition"),
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

  @JsonCreator
  public static ResourceEnum fromValue(String value) {
    return Arrays.stream(ResourceEnum.values()).filter(e -> e.resource.equals(value)).findFirst()
        .get();
  }

}

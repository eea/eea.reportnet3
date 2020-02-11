package org.eea.validation.persistence.schemas.rule;

import java.util.List;
import java.util.Objects;
import javax.persistence.Id;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class Rule.
 */
@Getter
@Setter
@ToString
@Document(collection = "RulesSchema")
public class RulesSchema {
  /** The rule id. */
  @Id
  @Field(value = "_id")
  private ObjectId rulesSchemaId;

  /** The dataset schema id. */
  @Field(value = "idDatasetSchema")
  private ObjectId idDatasetSchema;

  /** The rules dataset. */
  @Field(value = "rulesDataset")
  private List<Rule> rulesDataset;

  /** The rules tables. */
  @Field(value = "rulesTables")
  private List<Rule> rulesTables;

  /** The rules records. */
  @Field(value = "rulesRecords")
  private List<Rule> rulesRecords;

  /** The rules fields. */
  @Field(value = "rulesFields")
  private List<Rule> rulesFields;


  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(rulesSchemaId, idDatasetSchema, rulesDataset, rulesTables, rulesRecords,
        rulesFields);
  }

  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    RulesSchema other = (RulesSchema) obj;
    return Objects.equals(rulesSchemaId, other.rulesSchemaId)
        && Objects.equals(idDatasetSchema, other.idDatasetSchema)
        && Objects.equals(rulesDataset, other.rulesDataset)
        && Objects.equals(rulesTables, other.rulesTables)
        && Objects.equals(rulesRecords, other.rulesRecords)
        && Objects.equals(rulesFields, other.rulesFields);
  }
}

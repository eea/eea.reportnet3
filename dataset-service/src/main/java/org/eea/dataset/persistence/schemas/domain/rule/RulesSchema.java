package org.eea.dataset.persistence.schemas.domain.rule;

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
  @Field(value = "rules")
  private List<Rule> rules;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(rulesSchemaId, idDatasetSchema, rules);
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
        && Objects.equals(rules, other.rules);
  }
}

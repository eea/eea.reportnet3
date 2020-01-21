package org.eea.dataset.persistence.schemas.domain;

import java.util.List;
import java.util.Objects;
import javax.persistence.Id;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.rule.RuleDataSet;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class DataSetSchema.
 *
 */
@Getter
@Setter
@ToString
@Document(collection = "DataSetSchema")
public class DataSetSchema {

  /** The id. */
  @Id
  @Field(value = "_id")
  private ObjectId idDataSetSchema;

  /** The description. */
  @Field(value = "description")
  private String description;

  /** The idDataFlow. */
  @Field(value = "idDataFlow")
  private Long idDataFlow;

  /** The table schemas. */
  @Field(value = "tableSchemas")
  private List<TableSchema> tableSchemas;

  /** The rule data set. */
  @Field(value = "rules")
  private List<RuleDataSet> ruleDataSet;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(idDataFlow, description, idDataSetSchema, ruleDataSet, tableSchemas);
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
    DataSetSchema other = (DataSetSchema) obj;
    return Objects.equals(idDataFlow, other.idDataFlow)
        && Objects.equals(description, other.description)
        && Objects.equals(idDataSetSchema, other.idDataSetSchema)
        && Objects.equals(ruleDataSet, other.ruleDataSet)
        && Objects.equals(tableSchemas, other.tableSchemas);
  }
}

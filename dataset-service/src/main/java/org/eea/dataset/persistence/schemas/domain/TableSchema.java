package org.eea.dataset.persistence.schemas.domain;

import java.util.List;
import java.util.Objects;
import javax.persistence.Id;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.rule.RuleTable;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class TableSchema.
 */
@Getter
@Setter
@ToString
public class TableSchema {

  /** The id table schema. */
  @Id
  @Field(value = "_id")
  private ObjectId idTableSchema;

  /** The description. */
  @Field(value = "description")
  private String description;

  /** The name table schema. */
  @Field(value = "nameTableSchema")
  private String nameTableSchema;

  /** The Id data set. */
  @Field(value = "idDataSet")
  private ObjectId idDataSet;

  /** The record schema. */
  @Field(value = "recordSchema")
  private RecordSchema recordSchema;

  /** The rule table. */
  @Field(value = "rules")
  private List<RuleTable> ruleTable;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(idDataSet, description, idTableSchema, nameTableSchema, recordSchema,
        ruleTable);
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
    TableSchema other = (TableSchema) obj;
    return Objects.equals(idDataSet, other.idDataSet)
        && Objects.equals(description, other.description)
        && Objects.equals(idTableSchema, other.idTableSchema)
        && Objects.equals(nameTableSchema, other.nameTableSchema)
        && Objects.equals(recordSchema, other.recordSchema)
        && Objects.equals(ruleTable, other.ruleTable);
  }
}

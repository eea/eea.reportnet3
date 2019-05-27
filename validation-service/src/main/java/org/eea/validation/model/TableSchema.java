/*
 * 
 */
package org.eea.validation.model;

import java.util.List;
import java.util.Objects;
import javax.persistence.Id;
import org.bson.types.ObjectId;
import org.eea.validation.model.rules.TableRule;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class TableSchema {
  /** The id table schema. */
  @Id
  @Field(value = "_id")
  private ObjectId IdTableSchema;

  /** The name schema. */
  @Field(value = "nameSchema")
  private String nameSchema;

  /** The record schema. */
  @Field(value = "recordSchema")
  private RecordSchema recordSchema;

  /** The table rule list. */
  @Field(value = "tableRules")
  private List<TableRule> tableRuleList;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(IdTableSchema, nameSchema, recordSchema, tableRuleList);
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
    return Objects.equals(IdTableSchema, other.IdTableSchema)
        && Objects.equals(nameSchema, other.nameSchema)
        && Objects.equals(recordSchema, other.recordSchema)
        && Objects.equals(tableRuleList, other.tableRuleList);
  }
}

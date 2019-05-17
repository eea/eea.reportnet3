/*
 * 
 */
package org.eea.validation.model;

import java.util.List;
import java.util.Objects;
import javax.persistence.Id;
import org.bson.types.ObjectId;
import org.eea.validation.model.rules.RecordRule;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class RecordSchema.
 */
@Getter
@Setter
@ToString
public class RecordSchema {

  /** The id record schema. */
  @Id
  @Field(value = "_id")
  private ObjectId idRecordSchema;

  /** The name schema. */
  @Field(value = "nameSchema")
  private String nameSchema;

  /** The field schema. */
  @Field(value = "fieldSchemas")
  private List<FieldSchema> fieldSchema;

  /** The record rule list. */
  @Field(value = "recordRules")
  private List<RecordRule> recordRuleList;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(fieldSchema, idRecordSchema, nameSchema, recordRuleList);
  }

  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RecordSchema other = (RecordSchema) obj;
    return Objects.equals(fieldSchema, other.fieldSchema)
        && Objects.equals(idRecordSchema, other.idRecordSchema)
        && Objects.equals(nameSchema, other.nameSchema)
        && Objects.equals(recordRuleList, other.recordRuleList);
  }


}

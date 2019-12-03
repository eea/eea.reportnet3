/**
 *
 */
package org.eea.dataset.persistence.schemas.domain;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.rule.RuleRecord;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class RecordSchema.
 *
 * @author Mario Severa
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

  /** The field TableSchema. */
  @Field(value = "idTableSchema")
  private ObjectId idTableSchema;

  /** The field schema. */
  @Field(value = "fieldSchemas")
  private List<FieldSchema> fieldSchema;

  /** The rule record. */
  @Field(value = "rules")
  private List<RuleRecord> ruleRecord;


  /**
   * To JSON.
   *
   * @return the string
   */
  public String toJSON() {
    return "{\"_id\": {\"$oid\":\"" + idRecordSchema + "\"}, \"fieldSchemas\": ["
        + ((fieldSchema != null) && !fieldSchema.isEmpty() ? fieldSchema.stream()
            .filter(Objects::nonNull).map(FieldSchema::toJSON).collect(Collectors.joining(","))
            : "")
        + "], \"rules\": []}";
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(fieldSchema, idRecordSchema, idTableSchema, nameSchema, ruleRecord);
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
    RecordSchema other = (RecordSchema) obj;
    return Objects.equals(fieldSchema, other.fieldSchema)
        && Objects.equals(idRecordSchema, other.idRecordSchema)
        && Objects.equals(idTableSchema, other.idTableSchema)
        && Objects.equals(nameSchema, other.nameSchema)
        && Objects.equals(ruleRecord, other.ruleRecord);
  }



}

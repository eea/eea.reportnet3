/**
 * 
 */
package org.eea.dataset.persistence.schemas.domain;

import java.util.List;
import java.util.Objects;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Mario Severa
 *
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
  private ObjectId IdTableSchema;

  /** The field schema. */
  @Field(value = "fieldSchemas")
  private List<FieldSchema> fieldSchema;

  /**
   * 
   *
   * @return
   */
  @Override
  public int hashCode() {
    return Objects.hash(IdTableSchema, fieldSchema, idRecordSchema, nameSchema);
  }

  /**
   * 
   *
   * @param obj
   * @return
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
    return Objects.equals(IdTableSchema, other.IdTableSchema)
        && Objects.equals(fieldSchema, other.fieldSchema)
        && Objects.equals(idRecordSchema, other.idRecordSchema)
        && Objects.equals(nameSchema, other.nameSchema);
  }



}

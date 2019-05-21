package org.eea.dataset.persistence.schemas.domain;

import java.util.Objects;
import javax.persistence.Id;
import org.bson.types.ObjectId;
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
  private ObjectId IdTableSchema;

  /** The name table schema. */
  @Field(value = "nameTableSchema")
  private String nameTableSchema;

  /** The Id data set. */
  @Field(value = "idDataSet")
  private ObjectId IdDataSet;

  /** The name schema. */
  @Field(value = "nameSchema")
  private String nameSchema;

  /** The record schema. */
  @Field(value = "recordSchema")
  private RecordSchema recordSchema;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(IdDataSet, IdTableSchema, nameSchema, nameTableSchema, recordSchema);
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
    TableSchema other = (TableSchema) obj;
    return Objects.equals(IdDataSet, other.IdDataSet)
        && Objects.equals(IdTableSchema, other.IdTableSchema)
        && Objects.equals(nameSchema, other.nameSchema)
        && Objects.equals(nameTableSchema, other.nameTableSchema)
        && Objects.equals(recordSchema, other.recordSchema);
  }



}

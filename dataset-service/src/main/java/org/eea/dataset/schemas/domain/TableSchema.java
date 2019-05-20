package org.eea.dataset.schemas.domain;

import java.util.Objects;
import javax.persistence.Id;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 */
@Getter
@Setter
@ToString
public class TableSchema {
  /** The id table schema. */
  @Id
  @Field(value = "_id")
  private ObjectId IdTableSchema;


  /**  */
  @Field(value = "idDataSet")
  private ObjectId IdDataSet;

  /** The name schema. */
  @Field(value = "nameSchema")
  private String nameSchema;

  /** The record schema. */
  @Field(value = "recordSchema")
  private RecordSchema recordSchema;

  /**
   * 
   *
   * @return
   */
  @Override
  public int hashCode() {
    return Objects.hash(IdDataSet, IdTableSchema, nameSchema, recordSchema);
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
    TableSchema other = (TableSchema) obj;
    return Objects.equals(IdDataSet, other.IdDataSet)
        && Objects.equals(IdTableSchema, other.IdTableSchema)
        && Objects.equals(nameSchema, other.nameSchema)
        && Objects.equals(recordSchema, other.recordSchema);
  }



}

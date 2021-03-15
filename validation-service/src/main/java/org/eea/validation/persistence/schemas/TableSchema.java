package org.eea.validation.persistence.schemas;

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
  private ObjectId idTableSchema;

  /** The name table schema. */
  @Field(value = "nameTableSchema")
  private String nameTableSchema;

  /** The description. */
  @Field(value = "description")
  private String description;

  /** The Id data set. */
  @Field(value = "idDataSet")
  private ObjectId idDataSet;

  /** The read only. */
  @Field(value = "readOnly")
  private Boolean readOnly;

  /** The to prefill. */
  @Field(value = "toPrefill")
  private Boolean toPrefill;

  /** The not empty. */
  @Field(value = "notEmpty")
  private Boolean notEmpty;

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
    return Objects.hash(idDataSet, idTableSchema, nameTableSchema, recordSchema, description,
        notEmpty);
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
        && Objects.equals(idTableSchema, other.idTableSchema)
        && Objects.equals(nameTableSchema, other.nameTableSchema)
        && Objects.equals(recordSchema, other.recordSchema)
        && Objects.equals(description, other.description)
        && Objects.equals(notEmpty, other.notEmpty);
  }
}

/*
 * 
 */
package org.eea.interfaces.vo.dataset.schemas;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class TableSchemaVO {
  /** The id table schema. */
  private String IdTableSchema;

  /** The table name schema. */
  private String nameTableSchema;

  /** The record schema. */
  private RecordSchemaVO recordSchema;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(IdTableSchema, nameTableSchema, recordSchema);
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
    TableSchemaVO other = (TableSchemaVO) obj;
    return Objects.equals(IdTableSchema, other.IdTableSchema)
        && Objects.equals(nameTableSchema, other.nameTableSchema)
        && Objects.equals(recordSchema, other.recordSchema);
  }
}

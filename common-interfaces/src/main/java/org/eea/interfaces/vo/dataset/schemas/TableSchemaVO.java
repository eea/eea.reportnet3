package org.eea.interfaces.vo.dataset.schemas;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class TableSchemaVO.
 */
@Getter
@Setter
@ToString
public class TableSchemaVO {

  /** The id table schema. */
  private String idTableSchema;

  /** The description. */
  private String description;

  /** The table name schema. */
  private String nameTableSchema;

  /** The read only. */
  private Boolean readOnly;

  /** The record schema. */
  private RecordSchemaVO recordSchema;


  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(idTableSchema, description, nameTableSchema, recordSchema);
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
    TableSchemaVO other = (TableSchemaVO) obj;
    return Objects.equals(idTableSchema, other.idTableSchema)
        && Objects.equals(description, other.description)
        && Objects.equals(nameTableSchema, other.nameTableSchema)
        && Objects.equals(recordSchema, other.recordSchema);
  }
}

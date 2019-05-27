/*
 * 
 */
package org.eea.interfaces.vo.dataset.schemas;

import java.util.List;
import java.util.Objects;
import org.bson.types.ObjectId;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class RecordSchema.
 */
@Getter
@Setter
@ToString
public class RecordSchemaVO {

  /** The id record schema. */
  private ObjectId idRecordSchema;

  /** The name schema. */
  private String nameSchema;

  /** The field schema. */
  private List<FieldSchemaVO> fieldSchema;


  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(fieldSchema, idRecordSchema, nameSchema);
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
    RecordSchemaVO other = (RecordSchemaVO) obj;
    return Objects.equals(fieldSchema, other.fieldSchema)
        && Objects.equals(idRecordSchema, other.idRecordSchema)
        && Objects.equals(nameSchema, other.nameSchema);
  }


}

package org.eea.interfaces.vo.dataset.schemas;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class ReferencedFieldSchemaVO.
 */
@Getter
@Setter
@ToString
public class ReferencedFieldSchemaVO {


  /** The id dataset schema. */
  private String idDatasetSchema;


  /** The id pk. */
  private String idPk;


  private String label;


  private String conditional;



  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(idDatasetSchema, idPk);
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
    ReferencedFieldSchemaVO other = (ReferencedFieldSchemaVO) obj;
    return Objects.equals(idDatasetSchema, other.idDatasetSchema)
        && Objects.equals(idPk, other.idPk);
  }



}

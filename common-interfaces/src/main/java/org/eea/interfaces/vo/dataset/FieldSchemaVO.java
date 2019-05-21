package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class FieldSchemaVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 2680945261242083928L;

  /** The id. */
  private String id;

  /** The id record. */
  private String idRecord;

  /** The name. */
  private String name;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, idRecord, name);
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
    FieldSchemaVO other = (FieldSchemaVO) obj;
    return Objects.equals(id, other.id) && Objects.equals(idRecord, other.idRecord)
        && Objects.equals(name, other.name);
  }


}

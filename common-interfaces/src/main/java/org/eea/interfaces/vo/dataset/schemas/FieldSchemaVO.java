package org.eea.interfaces.vo.dataset.schemas;

import java.util.Objects;
import org.eea.interfaces.vo.dataset.enums.TypeData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class FieldSchemaVO.
 */
@Getter
@Setter

/**
 * To string.
 *
 * @return the java.lang. string
 */

/**
 * To string.
 *
 * @return the java.lang. string
 */
@ToString
public class FieldSchemaVO {

  /** The id. */
  private String id;

  /** The description. */
  private String description;

  /** The id record. */
  private String idRecord;

  /** The name. */
  private String name;

  /** The type. */
  private TypeData type;

  /** The id code list. */
  private Long idCodeList;


  /** The required. */
  private Boolean required;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, description, idRecord, name, type, idCodeList, required);
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
    FieldSchemaVO other = (FieldSchemaVO) obj;
    return Objects.equals(id, other.id) && Objects.equals(idRecord, other.idRecord)
        && Objects.equals(description, other.description) && Objects.equals(name, other.name)
        && Objects.equals(type, other.type) && Objects.equals(idCodeList, other.idCodeList)
        && Objects.equals(required, other.required);
  }
}

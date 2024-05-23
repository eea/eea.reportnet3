package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class FieldVO.
 */
@Getter
@Setter
@ToString
public class FieldVO implements Serializable {

  /**
   * The Constant serialVersionUID.
   */
  private static final long serialVersionUID = -5257537261370694057L;

  /**
   * The type.
   */
  private DataType type;

  /**
   * The value.
   */
  private String value;

  private byte[] byteArrayValue;
  /**
   * The id.
   */
  private String id;

  /**
   * The id header.
   */
  private String idFieldSchema;

  /** The label. */
  private String label;

  /**
   * The validations.
   */
  private List<FieldValidationVO> fieldValidations;

  /** The level error. */
  private ErrorTypeEnum levelError;

  private String name;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, idFieldSchema, type, value);
  }

  /**
   * Equals.
   *
   * @param obj the obj
   *
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
    FieldVO other = (FieldVO) obj;
    return Objects.equals(id, other.id) && Objects.equals(idFieldSchema, other.idFieldSchema)
        && Objects.equals(type, other.type) && Objects.equals(value, other.value);
  }

}

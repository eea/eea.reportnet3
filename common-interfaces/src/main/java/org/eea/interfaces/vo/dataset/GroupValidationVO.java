package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.Objects;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;



/**
 * The Class GroupValidationVO.
 */
@Getter
@Setter
@ToString
public class GroupValidationVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -5257537261370694057L;

  /** The type. */
  private String message;

  /** The id rule. */
  private String idRule;

  /** The level error. */
  private ErrorTypeEnum levelError;

  /** The type entity. */
  private EntityTypeEnum typeEntity;

  /** The number of records. */
  private Integer numberOfRecords;

  /** The name table schema. */
  private String nameTableSchema;

  /** The short code. */
  private String shortCode;

  /** The field name schema. */
  private String nameFieldSchema;

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(idRule, levelError, message, numberOfRecords, nameTableSchema, typeEntity,
        shortCode, nameFieldSchema);
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
    GroupValidationVO other = (GroupValidationVO) obj;
    return Objects.equals(idRule, other.idRule) && levelError == other.levelError
        && Objects.equals(message, other.message)
        && Objects.equals(numberOfRecords, other.numberOfRecords)
        && Objects.equals(nameTableSchema, other.nameTableSchema)
        && (typeEntity == other.typeEntity) && (shortCode == other.shortCode)
        && (nameFieldSchema == other.nameFieldSchema);
  }

}

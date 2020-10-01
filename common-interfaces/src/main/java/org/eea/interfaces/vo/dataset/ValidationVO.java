package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.Objects;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;



/**
 * The Class ValidationVO.
 */
@Getter
@Setter
@ToString
public class ValidationVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -5257537261370694057L;

  /** The type. */
  private String message;

  /** The id. */
  private Long id;

  /** The id rule. */
  private String idRule;

  /** The level error. */
  private ErrorTypeEnum levelError;

  /** The type entity. */
  private EntityTypeEnum typeEntity;

  /** The validation date. */
  private String validationDate;

  @Override
  public int hashCode() {
    return Objects.hash(id, idRule, levelError, message, typeEntity, validationDate);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ValidationVO other = (ValidationVO) obj;
    return Objects.equals(id, other.id) && Objects.equals(idRule, other.idRule)
        && levelError == other.levelError && Objects.equals(message, other.message)
        && typeEntity == other.typeEntity && Objects.equals(validationDate, other.validationDate);
  }

}

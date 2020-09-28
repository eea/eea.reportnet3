package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
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

  /** The origin name. */
  private String originName;

}

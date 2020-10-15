package org.eea.interfaces.vo.dataset;


import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;



/**
 * The Class ErrorsValidationVO.
 */
@Getter
@Setter

/**
 * To string.
 *
 * @return the java.lang. string
 */
@ToString
public class ErrorsValidationVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 5689991711182227303L;

  /** The id validation. */
  private Long idValidation;

  /** The id object. */
  private String idObject;

  /** The id table schema. */
  private String idTableSchema;

  /** The name table schema. */
  private String nameTableSchema;

  /** The message. */
  private String message;

  /** The level error. */
  private String levelError;

  /** The type entity. */
  private String typeEntity;

  /** The validation date. */
  private String validationDate;

  /** The field name schema. */
  private String nameFieldSchema;

  /** The short code. */
  private String shortCode;
}

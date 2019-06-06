package org.eea.dataset.persistence.data.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.interfaces.vo.dataset.enums.TypeErrorEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Dataset.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "VALIDATION")
public class Validation {

  /**
   * The id.
   */
  @Id
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  /** The id rule. */
  @Column(name = "ID_RULE")
  private Long idRule;

  /** The validation date. */
  @Column(name = "VALIDATION_DATE")
  private String validationDate;

  /** The message. */
  @Column(name = "MESSAGE")
  private String message;

  /** The level error. */
  @Column(name = "LEVEL_ERROR")
  private TypeErrorEnum levelError;

  /** The type entity. */
  @Column(name = "TYPE_ENTITY")
  private TypeEntityEnum typeEntity;

  /** The field validation. */
  @OneToOne(mappedBy = "validation")
  private FieldValidation fieldValidation;

  @OneToOne(mappedBy = "validation")
  private TableValidation tableValidation;

  @OneToOne(mappedBy = "validation")
  private RecordValidation recordValidation;

  @OneToOne(mappedBy = "validation")
  private DatasetValidation datasetValidation;

}

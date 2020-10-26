package org.eea.validation.persistence.data.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
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
  @SequenceGenerator(name = "validation_sequence_generator", sequenceName = "validation_sequence",
      allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "validation_sequence_generator")
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  /**
   * The id rule.
   */
  @Column(name = "ID_RULE")
  private String idRule;

  /**
   * The validation date.
   */
  @Column(name = "VALIDATION_DATE")
  private String validationDate;

  /**
   * The message.
   */
  @Column(name = "MESSAGE")
  private String message;

  /**
   * The level error.
   */
  @Column(name = "LEVEL_ERROR")
  @Enumerated(EnumType.STRING)
  private ErrorTypeEnum levelError;

  /**
   * The type entity.
   */
  @Column(name = "TYPE_ENTITY")
  @Enumerated(EnumType.STRING)
  private EntityTypeEnum typeEntity;


  /** The table name. */
  @Column(name = "TABLE_NAME")
  private String tableName;


  /** The field name. */
  @Column(name = "FIELD_NAME")
  private String fieldName;


  /** The short code. */
  @Column(name = "SHORT_CODE")
  private String shortCode;
}

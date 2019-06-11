package org.eea.dataset.persistence.data.domain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class Record.
 */
@Entity

/**
 * Gets the id validation.
 *
 * @return the id validation
 */
@Getter

/**
 * Sets the id validation.
 *
 * @param idValidation the new id validation
 */
@Setter

/**
 * To string.
 *
 * @return the java.lang. string
 */
@ToString
@Table(name = "FIELD_VALIDATION")
public class FieldValidation {

  /**
   * The id.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  /** The id field. */
  @ManyToOne
  @JoinColumn(name = "ID_FIELD")
  private FieldValue fieldValue;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = false)
  @JoinColumn(name = "ID_VALIDATION", referencedColumnName = "id")
  private Validation validation;

}

package org.eea.validation.persistence.data.domain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class Record.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "RECORD_VALIDATION")
public class RecordValidation {

  /**
   * The id.
   */
  @Id
  @SequenceGenerator(name = "record_validation_sequence_generator",
      sequenceName = "record_validation_sequence", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO,
      generator = "record_validation_sequence_generator")
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  /** The id field. */
  @ManyToOne
  @JoinColumn(name = "ID_RECORD")
  private RecordValue recordValue;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = false)
  @JoinColumn(name = "ID_VALIDATION", referencedColumnName = "id")
  private Validation validation;

}

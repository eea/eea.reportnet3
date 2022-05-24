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
 * The Class DatasetValidation.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "DATASET_VALIDATION")
public class DatasetValidation {

  /** The id. */
  @Id
  @SequenceGenerator(name = "dataset_validation_sequence_generator",
      sequenceName = "dataset_validation_sequence", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO,
      generator = "dataset_validation_sequence_generator")
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  /** The dataset value. */
  @ManyToOne
  @JoinColumn(name = "ID_DATASET")
  private DatasetValue datasetValue;

  /** The validation. */
  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = false)
  @JoinColumn(name = "ID_VALIDATION", referencedColumnName = "id")
  private Validation validation;

}

package org.eea.dataflow.persistence.domain;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.hibernate.annotations.DiscriminatorOptions;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class OperationParameters.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "INTEGRATION_OPERATION_PARAMETERS")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "PARAMETER_TYPE", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorOptions(force = true)
public class OperationParameters {

  /** The id. */
  @Id
  @Column(name = "ID", columnDefinition = "serial")
  @GeneratedValue(strategy = GenerationType.SEQUENCE,
      generator = "integration_operation_parameters_id_seq")
  @SequenceGenerator(name = "integration_operation_parameters_id_seq",
      sequenceName = "integration_operation_parameters_id_seq", allocationSize = 1)
  private Long id;

  /** The parameter. */
  @Column(name = "PARAMETER")
  private String parameter;

  /** The value. */
  @Column(name = "VALUE")
  private String value;

  /** The integration. */
  @ManyToOne
  @JoinColumn(name = "INTEGRATION_ID")
  private Integration integration;

}

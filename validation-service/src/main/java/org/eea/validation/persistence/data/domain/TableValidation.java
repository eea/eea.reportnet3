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
import org.hibernate.annotations.DynamicUpdate;
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
@DynamicUpdate
@Table(name = "TABLE_VALIDATION")
public class TableValidation {

  /**
   * The id.
   */
  @Id
  @SequenceGenerator(name = "table_validation_sequence_generator",
      sequenceName = "table_validation_sequence", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "table_validation_sequence_generator")
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  /** The id field. */
  @ManyToOne
  @JoinColumn(name = "ID_TABLE")
  private TableValue tableValue;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = false)
  @JoinColumn(name = "ID_VALIDATION", referencedColumnName = "id")
  private Validation validation;

}

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
@Table(name = "RECORD_VALIDATION")
public class RecordValidation {

  /**
   * The id.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
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

package org.eea.dataflow.persistence.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class LeadReporter.
 */
@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "representative_leadreporter", schema = "public")
public class LeadReporter {

  /** The id. */
  @Id
  @Column(name = "ID", columnDefinition = "serial")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "leadreporter_id_seq")
  @SequenceGenerator(name = "leadreporter_id_seq", sequenceName = "leadreporter_id_seq",
      allocationSize = 1)
  private Long id;

  /** The user email. */
  @Column(name = "email")
  private String email;

  /** The representative. */
  @ManyToOne
  @JoinColumn(name = "representative_id")
  private Representative representative;

  /** The invalid lead reporter. */
  @Column(name = "invalid")
  private Boolean invalid;

}

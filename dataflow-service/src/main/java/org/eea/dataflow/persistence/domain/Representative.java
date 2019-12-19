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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class DataflowRepresentative.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "representative")
public class Representative {

  /** The id. */
  @Id
  @Column(name = "ID", columnDefinition = "serial")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "representative_id_seq")
  @SequenceGenerator(name = "representative_id_seq", sequenceName = "representative_id_seq",
      allocationSize = 1)
  private Long id;

  /** The dataflow. */
  @ManyToOne
  @JoinColumn(name = "dataflow_id")
  private Dataflow dataflow;

  /** The representative. */
  @ManyToOne
  @JoinColumn(name = "data_provider_id")
  private DataProvider dataProvider;

  /** The user id. */
  @Column(name = "user_id")
  private String userId;

  /** The user mail. */
  @Column(name = "user_mail")
  private String userMail;
}

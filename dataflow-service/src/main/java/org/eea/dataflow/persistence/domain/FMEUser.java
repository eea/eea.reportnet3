package org.eea.dataflow.persistence.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class FmeUser.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "FME_USER")
public class FMEUser {

  /** The id. */
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fme_user_id_seq")
  @SequenceGenerator(name = "fme_user_id_seq", allocationSize = 1)
  @Column(name = "ID")
  private Long id;

  /** The user name. */
  @Column(name = "USER_NAME")
  private String username;

  /** The password. */
  @Column(name = "PASSWORD")
  private String password;
}

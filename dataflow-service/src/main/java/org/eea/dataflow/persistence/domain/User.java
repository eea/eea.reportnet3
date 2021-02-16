package org.eea.dataflow.persistence.domain;

import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class User.
 */
@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user", schema = "public")
public class User {

  /** The user mail. */
  @Id
  @Column(name = "user_mail")
  private String userMail;

  /** The representatives. */
  @ManyToMany(mappedBy = "reporters", fetch = FetchType.EAGER)
  private Set<Representative> representatives;

}

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

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user", schema = "public")
public class User {

  @Id
  @Column(name = "ID")
  private String id;

  /** The label. */
  @Column(name = "userMail")
  private String userMail;

  @ManyToMany(mappedBy = "reporters", fetch = FetchType.EAGER)
  private Set<Representative> representatives;

}

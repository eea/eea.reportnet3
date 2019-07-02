package org.eea.dataflow.persistence.domain;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import org.eea.interfaces.vo.dataflow.enums.TypeRequestEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(name = "USER_REQUEST")
public class UserRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  @Column(name = "USER_REQUESTER")
  private Long userRequester;

  @Column(name = "USER_REQUESTED")
  private Long userRequested;

  @Column(name = "REQUEST_TYPE")
  @Enumerated(EnumType.STRING)
  private TypeRequestEnum requestType;

  @ManyToMany(mappedBy = "userRequests")
  private Set<Dataflow> dataflows = new HashSet<>();

}

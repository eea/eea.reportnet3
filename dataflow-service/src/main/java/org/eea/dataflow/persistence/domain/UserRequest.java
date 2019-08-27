package org.eea.dataflow.persistence.domain;

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

/**
 * The Class UserRequest.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "USER_REQUEST")
public class UserRequest {

  /** The id. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", columnDefinition = "serial")
  private Long id;

  /** The user requester. */
  @Column(name = "USER_REQUESTER")
  private String userRequester;

  /** The user requested. */
  @Column(name = "USER_REQUESTED")
  private String userRequested;

  /** The request type. */
  @Column(name = "REQUEST_TYPE")
  @Enumerated(EnumType.STRING)
  private TypeRequestEnum requestType;

  /** The dataflows. */
  @ManyToMany(mappedBy = "userRequests")
  private Set<Dataflow> dataflows;

}

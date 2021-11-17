package org.eea.dataflow.persistence.domain;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class TempUser.
 */
@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "temp_user", schema = "public")
public class TempUser {

  /** The id. */
  @Id
  @Column(name = "ID", columnDefinition = "serial")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "temp_user_id_seq")
  @SequenceGenerator(name = "temp_user_id_seq", sequenceName = "temp_user_id_seq",
      allocationSize = 1)
  private Long id;

  /** The user email. */
  @Column(name = "email")
  private String email;

  /** The user type. */
  @Column(name = "usertype")
  private String role;

  /** The dataflow id. */
  @Column(name = "dataflowid")
  private Long dataflowId;

  /** The dataprovider id. */
  @Column(name = "dataproviderid")
  private Long dataProviderId;

  /** The registered date. */
  @Column(name = "registered")
  private Date registeredDate;

}

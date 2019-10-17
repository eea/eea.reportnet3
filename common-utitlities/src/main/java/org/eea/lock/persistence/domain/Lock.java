package org.eea.lock.persistence.domain;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.eea.interfaces.lock.enums.LockType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(name = "LOCK")
public class Lock {

  @Id
  @Column(name = "ID")
  private Integer id;

  @Column(name = "CREATE_DATE")
  private Timestamp createDate;

  @Column(name = "CREATED_BY")
  private String createdBy;

  @Column(name = "LOCK_TYPE")
  private LockType lockType;

  @Column(name = "LOCK_CRITERIA")
  private byte[] lockCriteria;
}

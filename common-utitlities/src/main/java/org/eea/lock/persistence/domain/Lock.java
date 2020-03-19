package org.eea.lock.persistence.domain;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.eea.interfaces.vo.lock.enums.LockType;

/**
 * The Class Lock.
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "LOCK")
public class Lock {

  /**
   * The id.
   */
  @Id
  @Column(name = "ID")
  private Integer id;

  /**
   * The create date.
   */
  @Column(name = "CREATE_DATE")
  private Timestamp createDate;

  /**
   * The created by.
   */
  @Column(name = "CREATED_BY")
  private String createdBy;

  /**
   * The lock type.
   */
  @Column(name = "LOCK_TYPE")
  private LockType lockType;

  /**
   * The lock criteria.
   */
  @Column(name = "LOCK_CRITERIA")
  private byte[] lockCriteria;
}

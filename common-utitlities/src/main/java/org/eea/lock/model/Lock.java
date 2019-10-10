package org.eea.lock.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Map;
import org.eea.interfaces.lock.enums.LockType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
/**
 * 
 * This is the Lock Model that represents lock at any level on Reportnet 3.0
 *
 */
public class Lock implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 6204847320615411241L;

  /** The create date. */
  private Timestamp createDate;

  /** The created by. */
  private String createdBy;

  /** The lock type. */
  private LockType lockType;

  /** The id. */
  private Integer id;

  /** The lock criteria. */
  private Map<Integer, Object> lockCriteria;

  /**
   * Instantiates a new lock.
   *
   * @param createDate the create date
   * @param createdBy the created by
   * @param lockType the lock type
   * @param id the id
   * @param lockCriteria the lock criteria
   */
  public Lock(Timestamp createDate, String createdBy, LockType lockType, Integer id,
      Map<Integer, Object> lockCriteria) {
    this.createDate = createDate;
    this.createdBy = createdBy;
    this.lockType = lockType;
    this.id = id;
    this.lockCriteria = lockCriteria;
  }
}

package org.eea.interfaces.vo.lock;

import java.sql.Timestamp;
import java.util.Map;
import org.eea.interfaces.vo.lock.enums.LockType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class LockVO. This is the Lock Model that represents lock at any level on Reportnet 3.0.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class LockVO {

  /**
   * The id.
   */
  private Integer id;

  /**
   * The create date.
   */
  private Timestamp createDate;

  /**
   * The created by.
   */
  private String createdBy;

  /**
   * The lock type.
   */
  private LockType lockType;

  /**
   * The lock criteria.
   */
  private Map<String, Object> lockCriteria;

  /**
   * Instantiates a new lock.
   *
   * @param createDate the create date
   * @param createdBy the created by
   * @param lockType the lock type
   * @param id the id
   * @param lockCriteria the lock criteria
   */
  public LockVO(Timestamp createDate, String createdBy, LockType lockType, Integer id,
      Map<String, Object> lockCriteria) {
    this.createDate = createDate;
    this.createdBy = createdBy;
    this.lockType = lockType;
    this.id = id;
    this.lockCriteria = lockCriteria;
  }
}

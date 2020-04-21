package org.eea.lock.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.lock.LockVO;
import org.eea.interfaces.vo.lock.enums.LockType;

/**
 * The Interface LockService.
 */
public interface LockService {

  /**
   * Creates the lock.
   *
   * @param createDate the create date
   * @param createdBy the created by
   * @param lockType the lock type
   * @param lockCriteria the lock criteria
   * @return the lock VO
   * @throws EEAException the EEA exception
   */
  public LockVO createLock(Timestamp createDate, String createdBy, LockType lockType,
      Map<String, Object> lockCriteria) throws EEAException;

  /**
   * Removes the lock.
   *
   * @param lockId the lock id
   * @return the boolean
   */
  public Boolean removeLock(Integer lockId);

  /**
   * Removes the lock by criteria.
   *
   * @param args the args
   * @return the boolean
   */
  public Boolean removeLockByCriteria(List<Object> args);

  /**
   * Find by id.
   *
   * @param lockId the lock id
   * @return the lock VO
   */
  public LockVO findById(Integer lockId);

  /**
   * Find all.
   *
   * @return the list
   */
  public List<LockVO> findAll();

  /**
   * Find by criteria.
   *
   * @param lockCriteria the lock criteria
   * @return the lock VO
   */
  public LockVO findByCriteria(Map<String, Object> lockCriteria);

  /**
   * Schedule lock removal task.
   *
   * @param lockId the lock id
   */
  void scheduleLockRemovalTask(Integer lockId);
}

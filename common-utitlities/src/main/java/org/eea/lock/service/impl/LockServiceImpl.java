package org.eea.lock.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.lock.LockVO;
import org.eea.interfaces.vo.lock.enums.LockType;
import org.eea.lock.mapper.LockMapper;
import org.eea.lock.persistence.domain.Lock;
import org.eea.lock.persistence.repository.LockRepository;
import org.eea.lock.service.LockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * The Class LockServiceImpl.
 */
@Service("lockService")
public class LockServiceImpl implements LockService {

  /** The delay. */
  @Value("${lock.releaseDelay}")
  private long delay;

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(LockServiceImpl.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant SCHEDULER. */
  private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);

  /** The Constant TASKS. */
  private static final Map<Integer, ScheduledFuture<?>> TASKS = new ConcurrentHashMap<>();

  /** The lock repository. */
  @Autowired
  private LockRepository lockRepository;

  /** The lock mapper. */
  @Autowired
  private LockMapper lockMapper;

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
  @Override
  public LockVO createLock(Timestamp createDate, String createdBy, LockType lockType,
      Map<String, Object> lockCriteria) throws EEAException {

    LockVO lockVO =
        new LockVO(createDate, createdBy, lockType, generateHashCode(lockCriteria), lockCriteria);

    if (lockRepository.saveIfAbsent(lockVO.getId(), lockMapper.classToEntity(lockVO))) {
      LOG.info("Lock added: {}", lockVO.getId());
      return lockVO;
    }

    LOG_ERROR.error("Already locked: {}", lockVO.getId());
    throw new EEAException(EEAErrorMessage.METHOD_LOCKED + ": " + lockVO);
  }

  /**
   * Removes the lock.
   *
   * @param lockId the lock id
   * @return the boolean
   */
  @Override
  public Boolean removeLock(Integer lockId) {
    Boolean isRemoved = lockRepository.deleteIfPresent(lockId);
    if (Boolean.TRUE.equals(isRemoved)) {
      ScheduledFuture<?> task = TASKS.remove(lockId);
      if (task != null) {
        task.cancel(false);
      }
    }
    LOG.info("Lock removed: {} - {}", lockId, isRemoved);
    return isRemoved;
  }

  /**
   * Removes the lock by criteria.
   *
   * @param args the args
   * @return the boolean
   */
  @Override
  public Boolean removeLockByCriteria(Map<String, Object> args) {
    return removeLock(generateHashCode(args));
  }

  /**
   * Find by id.
   *
   * @param lockId the lock id
   * @return the lock VO
   */
  @Override
  public LockVO findById(Integer lockId) {
    Lock lock = lockRepository.findById(lockId).orElse(null);
    if (lock != null) {
      return lockMapper.entityToClass(lock);
    }
    return null;
  }

  /**
   * Find all.
   *
   * @return the list
   */
  @Override
  public List<LockVO> findAll() {
    List<LockVO> list = new ArrayList<>();
    lockRepository.findAll().forEach(e -> list.add(lockMapper.entityToClass(e)));
    return list;
  }

  /**
   * Generate hash code.
   *
   * @param args the args
   * @return the integer
   */
  private Integer generateHashCode(Map<String, Object> criteria) {
    SortedSet<String> treeSet = new TreeSet<>();
    for (Entry<String, Object> entry : criteria.entrySet()) {
      treeSet.add(entry.getKey() + "-" + entry.getValue());
    }
    int hashCode = 7;
    Iterator<String> iterator = treeSet.iterator();
    while (iterator.hasNext()) {
      hashCode = hashCode * 31 + iterator.next().hashCode();
    }
    return hashCode;
  }

  /**
   * Find by criteria.
   *
   * @param lockCriteria the lock criteria
   * @return the lock VO
   */
  @Override
  public LockVO findByCriteria(Map<String, Object> lockCriteria) {
    return findById(generateHashCode(lockCriteria));
  }

  /**
   * Schedule lock removal task.
   *
   * @param lockId the lock id
   */
  @Override
  public void scheduleLockRemovalTask(Integer lockId) {
    ScheduledFuture<?> task =
        SCHEDULER.schedule(() -> scheduledRemoveLock(lockId), delay, TimeUnit.MILLISECONDS);
    TASKS.put(lockId, task);
  }

  /**
   * Scheduled remove lock.
   *
   * @param lockId the lock id
   */
  private void scheduledRemoveLock(Integer lockId) {
    Boolean isRemoved = lockRepository.deleteIfPresent(lockId);
    if (Boolean.TRUE.equals(isRemoved)) {
      ScheduledFuture<?> task = TASKS.remove(lockId);
      if (task != null) {
        task.cancel(false);
      }
    }
    LOG.warn("Lock removed by scheduler: {} - {}", lockId, isRemoved);
  }
}

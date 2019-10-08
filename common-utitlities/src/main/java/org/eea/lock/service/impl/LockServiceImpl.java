package org.eea.lock.service.impl;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.eea.interfaces.lock.enums.LockType;
import org.eea.lock.model.Lock;
import org.eea.lock.service.LockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("lockService")
public class LockServiceImpl implements LockService {

  private static final Logger LOG = LoggerFactory.getLogger(LockServiceImpl.class);

  private Map<Integer, Lock> locks = new ConcurrentHashMap<>();
  // private KafkaSender kafkaSender;

  @Override
  public Lock createLock(Timestamp createDate, String createdBy, LockType lockType,
      Map<Integer, Object> lockCriteria, String signature) {

    Lock lock = new Lock(createDate, createdBy, lockType,
        generateHashCode(signature, lockCriteria.values().stream().collect(Collectors.toList())),
        lockCriteria);

    if (locks.putIfAbsent(lock.getId(), lock) == null) {
      LOG.info("Lock added: {}", lock.getId());
      return lock;
    }

    LOG.info("Already locked: {}", lock.getId());
    return null;
  }

  @Override
  public Boolean removeLock(Integer lockId) {
    Boolean isRemoved = locks.remove(lockId) != null;
    LOG.info("Lock removed: {} - {}", lockId, isRemoved);
    return isRemoved;
  }

  @Override
  public Boolean removeLockByCriteria(String signature, List<Object> args) {
    return removeLock(generateHashCode(signature, args));
  }

  @Override
  public Lock findLock(Integer lockId) {
    return locks.get(lockId);
  }

  @Override
  public List<Lock> findAll() {
    return locks.values().stream().collect(Collectors.toList());
  }

  private Integer generateHashCode(String signature, List<Object> args) {
    return Objects.hash(signature, args.hashCode());
  }
}

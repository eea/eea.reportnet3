package org.eea.lock.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import org.eea.interfaces.lock.enums.LockType;
import org.eea.lock.model.Lock;

public interface LockService {

  public Lock createLock(Timestamp createDate, String createdBy, LockType lockType,
      Map<Integer, Object> lockCriteria, String signature);

  public Boolean removeLock(Integer lockId);

  public Boolean removeLockByCriteria(String signature, List<Object> args);

  public Lock findLock(Integer lockId);

  public List<Lock> findAll();
}

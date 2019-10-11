package org.eea.lock.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import org.eea.interfaces.lock.enums.LockType;
import org.eea.interfaces.vo.lock.LockVO;

public interface LockService {

  public LockVO createLock(Timestamp createDate, String createdBy, LockType lockType,
      Map<Integer, Object> lockCriteria, String signature);

  public Boolean removeLock(Integer lockId);

  public Boolean removeLockByCriteria(String signature, List<Object> args);

  public LockVO findLock(Integer lockId);

  public List<LockVO> findAll();
}

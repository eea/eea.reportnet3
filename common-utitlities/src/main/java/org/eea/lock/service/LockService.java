package org.eea.lock.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import org.eea.interfaces.vo.lock.LockVO;
import org.eea.interfaces.vo.lock.enums.LockType;

public interface LockService {

  public LockVO createLock(Timestamp createDate, String createdBy, LockType lockType,
      Map<String, Object> lockCriteria);

  public Boolean removeLock(Integer lockId);

  public Boolean removeLockByCriteria(List<Object> args);

  public LockVO findLock(Integer lockId);

  public List<LockVO> findAll();
}

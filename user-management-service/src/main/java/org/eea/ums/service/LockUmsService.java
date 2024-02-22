package org.eea.ums.service;

import org.eea.exception.EEAException;
import org.eea.interfaces.vo.lock.enums.LockSignature;

import java.util.Map;

public interface LockUmsService {
  boolean lockNotExists(LockSignature lockSignature, Map<String, Object> mapCriteria);

  void createLock(Map<String, Object> mapCriteria, String userName) throws EEAException;

  void removeLock(Map<String, Object> mapCriteria);
}

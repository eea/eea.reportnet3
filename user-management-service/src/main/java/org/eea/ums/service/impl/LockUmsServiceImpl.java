package org.eea.ums.service.impl;

import org.eea.exception.EEAException;
import org.eea.interfaces.vo.lock.LockVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.lock.enums.LockType;
import org.eea.lock.service.LockService;
import org.eea.ums.service.LockUmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Map;

@Service
public class LockUmsServiceImpl implements LockUmsService {

  private final LockService lockService;

  @Autowired
  public LockUmsServiceImpl(LockService lockUmsService) {
    this.lockService = lockUmsService;
  }

  /**
   * The method check if the lock exists in DB
   *
   * @param lockSignature The LockSignature must be unique
   * @param mapCriteria   The criteria for the lock
   * @return true , if lock does not exist on DB
   */
  @Override
  public boolean lockNotExists(LockSignature lockSignature, Map<String, Object> mapCriteria) {
    mapCriteria.put("signature", lockSignature.getValue());
    LockVO lockVO = lockService.findByCriteria(mapCriteria);
    return lockVO == null;
  }

  /**
   * This method creates the lock
   *
   * @param mapCriteria The criteria for the lock
   * @param userName The current userName
   *
   * @throws EEAException eeaException
   */
  @Override
  public void createLock(Map<String, Object> mapCriteria, String userName) throws EEAException {
    lockService.createLock(new Timestamp(System.currentTimeMillis()), userName, LockType.METHOD,
        mapCriteria);
  }

  /**
   * Removes the lock from the DB
   *
   * @param mapCriteria The criteria for the lock
   */
  @Override
  public void removeLock(Map<String, Object> mapCriteria) {
    lockService.removeLockByCriteria(mapCriteria);
  }
}

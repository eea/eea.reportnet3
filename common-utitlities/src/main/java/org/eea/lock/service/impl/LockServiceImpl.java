package org.eea.lock.service.impl;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.eea.interfaces.lock.enums.LockType;
import org.eea.interfaces.vo.lock.LockVO;
import org.eea.lock.service.LockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("lockService")
public class LockServiceImpl implements LockService {

  private static final Logger LOG = LoggerFactory.getLogger(LockServiceImpl.class);

  private Map<Integer, LockVO> lockVOs = new ConcurrentHashMap<>();
  // private KafkaSender kafkaSender;

  @Override
  public LockVO createLock(Timestamp createDate, String createdBy, LockType lockType,
      Map<Integer, Object> lockCriteria, String signature) {

    LockVO lockVO = new LockVO(createDate, createdBy, lockType,
        generateHashCode(signature, lockCriteria.values().stream().collect(Collectors.toList())),
        lockCriteria);

    if (lockVOs.putIfAbsent(lockVO.getId(), lockVO) == null) {
      LOG.info("Lock added: {}", lockVO.getId());
      return lockVO;
    }

    LOG.info("Already locked: {}", lockVO.getId());
    return null;
  }

  @Override
  public Boolean removeLock(Integer lockId) {
    Boolean isRemoved = lockVOs.remove(lockId) != null;
    LOG.info("Lock removed: {} - {}", lockId, isRemoved);
    return isRemoved;
  }

  @Override
  public Boolean removeLockByCriteria(String signature, List<Object> args) {
    return removeLock(generateHashCode(signature, args));
  }

  @Override
  public LockVO findLock(Integer lockId) {
    return lockVOs.get(lockId);
  }

  @Override
  public List<LockVO> findAll() {
    return lockVOs.values().stream().collect(Collectors.toList());
  }

  private Integer generateHashCode(String signature, List<Object> args) {
    return Objects.hash(signature, args.hashCode());
  }
}

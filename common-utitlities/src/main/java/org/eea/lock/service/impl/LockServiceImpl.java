package org.eea.lock.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eea.interfaces.lock.enums.LockSignature;
import org.eea.interfaces.lock.enums.LockType;
import org.eea.interfaces.vo.lock.LockVO;
import org.eea.lock.mapper.LockMapper;
import org.eea.lock.persistence.domain.Lock;
import org.eea.lock.persistence.repository.LockRepository;
import org.eea.lock.service.LockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("lockService")
public class LockServiceImpl implements LockService {

  private static final Logger LOG = LoggerFactory.getLogger(LockServiceImpl.class);

  @Autowired
  private LockRepository lockRepository;

  @Autowired
  private LockMapper lockMapper;

  @Override
  public LockVO createLock(Timestamp createDate, String createdBy, LockType lockType,
      Map<Integer, Object> lockCriteria, String signature) {

    LockVO lockVO = new LockVO(createDate, createdBy, lockType,
        generateHashCode(signature, lockCriteria.values().stream().collect(Collectors.toList())),
        lockCriteria);

    if (lockRepository.saveIfAbsent(lockVO.getId(), lockMapper.classToEntity(lockVO))) {
      LOG.info("Lock added: {}", lockVO.getId());
      return lockVO;
    }

    LOG.info("Already locked: {}", lockVO.getId());
    return null;
  }

  @Override
  public Boolean removeLock(Integer lockId) {
    Boolean isRemoved = lockRepository.deleteIfPresent(lockId);
    LOG.info("Lock removed: {} - {}", lockId, isRemoved);
    return isRemoved;
  }

  @Override
  public Boolean removeLockByCriteria(LockSignature signature, List<Object> args) {
    return removeLock(generateHashCode(signature.getValue(), args));
  }

  @Override
  public LockVO findLock(Integer lockId) {
    Lock lock = lockRepository.findById(lockId).orElse(null);
    if (lock != null) {
      return lockMapper.entityToClass(lock);
    }
    return null;
  }

  @Override
  public List<LockVO> findAll() {
    List<LockVO> list = new ArrayList<>();
    lockRepository.findAll().forEach(e -> list.add(lockMapper.entityToClass(e)));
    return list;
  }

  private Integer generateHashCode(String signature, List<Object> args) {
    return Objects.hash(signature, args.hashCode());
  }
}

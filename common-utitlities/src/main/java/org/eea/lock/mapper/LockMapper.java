package org.eea.lock.mapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import org.eea.interfaces.vo.lock.LockVO;
import org.eea.lock.persistence.domain.Lock;
import org.eea.mapper.IMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Interface LockMapper.
 */
@Mapper(componentModel = "spring")
public abstract class LockMapper implements IMapper<Lock, LockVO> {

  /**
   * The Constant LOG_ERROR.
   */
  Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


  /**
   * Entity to class.
   *
   * @param entity the entity
   *
   * @return the lock VO
   */
  @Mapping(target = "lockCriteria", ignore = true)
  @Override
  public abstract LockVO entityToClass(Lock entity);

  /**
   * Class to entity.
   *
   * @param model the model
   *
   * @return the lock
   */
  @Mapping(target = "lockCriteria", ignore = true)
  @Override
  public abstract Lock classToEntity(LockVO model);

  /**
   * Byte to hash map.
   *
   * @param lock the lock
   * @param lockVO the lock VO
   */
  @AfterMapping
  public void byteToHashMap(Lock lock, @MappingTarget LockVO lockVO) {

    ByteArrayInputStream byteIn = new ByteArrayInputStream(lock.getLockCriteria());
    ObjectInputStream in;
    try {
      in = new ObjectInputStream(byteIn);
      lockVO.setLockCriteria((Map<String, Object>) in.readObject());
    } catch (ClassNotFoundException | IOException e) {
      LOG_ERROR.error("Error in afterMapping byteToHashMap from LockMapper. Message {}:",
          e.getMessage());
    }
  }

  /**
   * Hash map to byte.
   *
   * @param lockVO the lock VO
   * @param lock the lock
   */
  @AfterMapping
  public void hashMapToByte(LockVO lockVO, @MappingTarget Lock lock) {
    try {
      ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(byteOut);
      out.writeObject(lockVO.getLockCriteria());
      lock.setLockCriteria(byteOut.toByteArray());
    } catch (IOException e) {
      LOG_ERROR.error("Error in afterMapping hashMapToByte from LockMapper. Message {}:",
          e.getMessage());
    }
  }
}

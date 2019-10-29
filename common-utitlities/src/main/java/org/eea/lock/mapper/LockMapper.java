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

/**
 * The Interface LockMapper.
 */
@Mapper(componentModel = "spring")
public interface LockMapper extends IMapper<Lock, LockVO> {

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the lock VO
   */
  @Mapping(target = "lockCriteria", ignore = true)
  @Override
  LockVO entityToClass(final Lock entity);

  /**
   * Class to entity.
   *
   * @param model the model
   * @return the lock
   */
  @Mapping(target = "lockCriteria", ignore = true)
  @Override
  Lock classToEntity(final LockVO model);

  /**
   * Byte to hash map.
   *
   * @param lock the lock
   * @param lockVO the lock VO
   */
  @SuppressWarnings("unchecked")
  @AfterMapping
  default void byteToHashMap(Lock lock, @MappingTarget LockVO lockVO) {

    ByteArrayInputStream byteIn = new ByteArrayInputStream(lock.getLockCriteria());
    ObjectInputStream in;
    try {
      in = new ObjectInputStream(byteIn);
      lockVO.setLockCriteria((Map<String, Object>) in.readObject());
    } catch (ClassNotFoundException | IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Hash map to byte.
   *
   * @param lockVO the lock VO
   * @param lock the lock
   */
  @AfterMapping
  default void hashMapToByte(LockVO lockVO, @MappingTarget Lock lock) {
    try {
      ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(byteOut);
      out.writeObject(lockVO.getLockCriteria());
      lock.setLockCriteria(byteOut.toByteArray());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}

package org.eea.communication.mapper;

import org.bson.types.ObjectId;
import org.eea.communication.persistence.SystemNotification;
import org.eea.interfaces.vo.communication.SystemNotificationVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;

/**
 * The Interface SystemNotificationMapper.
 */
@Mapper(componentModel = "spring")
public interface SystemNotificationMapper
    extends IMapper<SystemNotification, SystemNotificationVO> {

  /**
   * Map.
   *
   * @param value the value
   * @return the string
   */
  default String map(ObjectId value) {
    return value != null ? value.toString() : null;
  }

  /**
   * Map.
   *
   * @param value the value
   * @return the object id
   */
  default ObjectId map(String value) {
    return value == null ? new ObjectId() : new ObjectId(value);
  }
}

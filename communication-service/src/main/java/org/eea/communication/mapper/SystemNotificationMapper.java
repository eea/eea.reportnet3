package org.eea.communication.mapper;

import org.bson.types.ObjectId;
import org.eea.communication.persistence.SystemNotification;
import org.eea.interfaces.vo.communication.SystemNotificationVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The Interface SystemNotificationMapper.
 */
@Mapper(componentModel = "spring")
public interface SystemNotificationMapper
    extends IMapper<SystemNotification, SystemNotificationVO> {

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the system notification VO
   */
  @Override
  @Mapping(source = "id", target = "id")
  @Mapping(source = "message", target = "message")
  @Mapping(source = "enabled", target = "enabled")
  @Mapping(source = "level", target = "level")
  SystemNotificationVO entityToClass(SystemNotification entity);


  /**
   * Class to entity.
   *
   * @param entity the entity
   * @return the system notification
   */
  @Override
  @Mapping(source = "message", target = "message")
  @Mapping(source = "enabled", target = "enabled")
  @Mapping(source = "level", target = "level")
  SystemNotification classToEntity(SystemNotificationVO entity);

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
    return new ObjectId(value);
  }
}

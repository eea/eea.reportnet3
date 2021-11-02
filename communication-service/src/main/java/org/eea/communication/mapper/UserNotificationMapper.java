package org.eea.communication.mapper;

import org.eea.communication.persistence.UserNotification;
import org.eea.interfaces.vo.communication.UserNotificationVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The Interface UserNotificationMapper.
 */
@Mapper(componentModel = "spring")
public interface UserNotificationMapper extends IMapper<UserNotification, UserNotificationVO> {


  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the user notification VO
   */
  @Override
  @Mapping(source = "dataflowId", target = "content.dataflowId")
  @Mapping(source = "dataflowName", target = "content.dataflowName")
  @Mapping(source = "datasetId", target = "content.datasetId")
  @Mapping(source = "datasetName", target = "content.datasetName")
  @Mapping(source = "providerId", target = "content.dataProviderName")
  @Mapping(source = "typeStatus", target = "content.typeStatus")
  UserNotificationVO entityToClass(UserNotification entity);

  /**
   * Class to entity.
   *
   * @param entity the entity
   * @return the user notification
   */
  @Override
  @Mapping(source = "content.dataflowId", target = "dataflowId")
  @Mapping(source = "content.dataflowName", target = "dataflowName")
  @Mapping(source = "content.datasetId", target = "datasetId")
  @Mapping(source = "content.datasetName", target = "datasetName")
  @Mapping(source = "content.providerId", target = "dataProviderName")
  @Mapping(source = "content.typeStatus", target = "typeStatus")
  UserNotification classToEntity(UserNotificationVO entity);

}

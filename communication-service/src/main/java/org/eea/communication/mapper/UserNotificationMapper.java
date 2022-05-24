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
  @Mapping(source = "providerId", target = "content.providerId")
  @Mapping(source = "dataProviderName", target = "content.dataProviderName")
  @Mapping(source = "typeStatus", target = "content.typeStatus")
  @Mapping(source = "customContent", target = "content.customContent")
  @Mapping(source = "type", target = "content.type")
  @Mapping(source = "tableSchemaName", target = "content.tableSchemaName")
  @Mapping(source = "fileName", target = "content.fileName")
  @Mapping(source = "shortCode", target = "content.shortCode")
  @Mapping(source = "invalidRules", target = "content.invalidRules")
  @Mapping(source = "disabledRules", target = "content.disabledRules")
  @Mapping(source = "datasetStatus", target = "content.datasetStatus")
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
  @Mapping(source = "content.providerId", target = "providerId")
  @Mapping(source = "content.dataProviderName", target = "dataProviderName")
  @Mapping(source = "content.typeStatus", target = "typeStatus")
  @Mapping(source = "content.customContent", target = "customContent")
  @Mapping(source = "content.type", target = "type")
  @Mapping(source = "content.tableSchemaName", target = "tableSchemaName")
  @Mapping(source = "content.fileName", target = "fileName")
  @Mapping(source = "content.shortCode", target = "shortCode")
  @Mapping(source = "content.invalidRules", target = "invalidRules")
  @Mapping(source = "content.disabledRules", target = "disabledRules")
  @Mapping(source = "content.datasetStatus", target = "datasetStatus")
  UserNotification classToEntity(UserNotificationVO entity);

}

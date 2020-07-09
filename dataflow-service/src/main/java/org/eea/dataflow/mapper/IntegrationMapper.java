package org.eea.dataflow.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.dataflow.persistence.domain.ExternalOperationParameters;
import org.eea.dataflow.persistence.domain.Integration;
import org.eea.dataflow.persistence.domain.InternalOperationParameters;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.mapper.IMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;


/**
 * The Interface IntegrationMapper.
 */
@Mapper(componentModel = "spring")
public interface IntegrationMapper extends IMapper<Integration, IntegrationVO> {


  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the integration VO
   */
  @Override
  IntegrationVO entityToClass(Integration entity);


  /**
   * Class to entity.
   *
   * @param model the model
   * @return the integration
   */
  @Override
  Integration classToEntity(IntegrationVO model);

  /**
   * Map internal.
   *
   * @param list the list
   * @return the map
   */
  default Map<String, String> mapInternal(List<InternalOperationParameters> list) {
    Map<String, String> mapOperation = new HashMap<>();
    for (InternalOperationParameters internal : list) {
      mapOperation.put(internal.getParameter(), internal.getValue());
    }
    return mapOperation;
  }

  /**
   * Map internal.
   *
   * @param map the map
   * @return the list
   */
  default List<InternalOperationParameters> mapInternal(Map<String, String> map) {
    List<InternalOperationParameters> list = new ArrayList<>();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      InternalOperationParameters internal = new InternalOperationParameters();
      internal.setParameter(entry.getKey());
      internal.setValue(entry.getValue());
      list.add(internal);
    }
    return list;
  }

  /**
   * Map external.
   *
   * @param list the list
   * @return the map
   */
  default Map<String, String> mapExternal(List<ExternalOperationParameters> list) {
    Map<String, String> mapOperation = new HashMap<>();
    for (ExternalOperationParameters external : list) {
      mapOperation.put(external.getParameter(), external.getValue());
    }
    return mapOperation;
  }

  /**
   * Map external.
   *
   * @param map the map
   * @return the list
   */
  default List<ExternalOperationParameters> mapExternal(Map<String, String> map) {
    List<ExternalOperationParameters> list = new ArrayList<>();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      ExternalOperationParameters external = new ExternalOperationParameters();
      external.setParameter(entry.getKey());
      external.setValue(entry.getValue());
      list.add(external);
    }
    return list;
  }

  /**
   * Fill integration.
   *
   * @param integration the integration
   */
  @AfterMapping
  default void fillIntegration(@MappingTarget Integration integration) {
    if (integration.getExternalParameters() != null
        && !integration.getExternalParameters().isEmpty()) {
      for (ExternalOperationParameters external : integration.getExternalParameters()) {
        external.setIntegration(integration);
      }
    }
    if (integration.getInternalParameters() != null
        && !integration.getInternalParameters().isEmpty()) {
      for (InternalOperationParameters internal : integration.getInternalParameters()) {
        internal.setIntegration(integration);
        if ("dataflowId".equals(internal.getParameter())) {
          Dataflow df = new Dataflow();
          df.setId(Long.valueOf(internal.getValue()));
          integration.setDataflow(df);
        }
      }
    }
  }
}

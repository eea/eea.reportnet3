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


@Mapper(componentModel = "spring")
public interface IntegrationMapper extends IMapper<Integration, IntegrationVO> {


  @Override
  IntegrationVO entityToClass(Integration entity);


  @Override
  Integration classToEntity(IntegrationVO model);

  default Map<String, String> mapInternal(List<InternalOperationParameters> list) {
    Map<String, String> mapOperation = new HashMap<String, String>();
    for (InternalOperationParameters internal : list) {
      mapOperation.put(internal.getParameter(), internal.getValue());
    }
    return mapOperation;
  }

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

  default Map<String, String> mapExternal(List<ExternalOperationParameters> list) {
    Map<String, String> mapOperation = new HashMap<String, String>();
    for (ExternalOperationParameters external : list) {
      mapOperation.put(external.getParameter(), external.getValue());
    }
    return mapOperation;
  }

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

  @AfterMapping
  default void fillIntegration(@MappingTarget Integration integration) {
    if (integration.getExternalParameters() != null
        && integration.getExternalParameters().size() > 0) {
      for (ExternalOperationParameters external : integration.getExternalParameters()) {
        external.setIntegration(integration);
      }
    }
    if (integration.getInternalParameters() != null
        && integration.getInternalParameters().size() > 0) {
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

package org.eea.dataflow.mapper;

import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.document.DocumentVO;
import org.eea.mapper.IMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * The Interface DataflowMapper.
 */
@Mapper(componentModel = "spring")
public interface DataflowMapper extends IMapper<Dataflow, DataFlowVO> {


  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the data flow VO
   */
  @Override
  @Mapping(source = "obligationId", target = "obligation.obligationId")
  @Mapping(source = "deadlineDate", target = "deadlineDate", dateFormat = "dd/MM/yyyy")
  DataFlowVO entityToClass(Dataflow entity);

  /**
   * Class to entity.
   *
   * @param entity the entity
   * @return the dataflow
   */
  @Override
  @Mapping(source = "deadlineDate", target = "deadlineDate", dateFormat = "dd/MM/yyyy")
  @Mapping(source = "obligation.obligationId", target = "obligationId")
  Dataflow classToEntity(DataFlowVO entity);

  /**
   * Fill category.
   *
   * @param dataflow the dataflow
   * @param dataFlowVO the data flow VO
   */
  @AfterMapping
  default void fillCategory(Dataflow dataflow, @MappingTarget DataFlowVO dataFlowVO) {
    List<DocumentVO> documents = dataFlowVO.getDocuments();
    documents.stream().filter(document -> StringUtils.isNotBlank(document.getName()))
        .forEach(document -> {
          document.setCategory(
              document.getName().substring(document.getName().lastIndexOf('.') + 1).toLowerCase());
          document.setDataflowId(dataflow.getId());
        });
  }
}

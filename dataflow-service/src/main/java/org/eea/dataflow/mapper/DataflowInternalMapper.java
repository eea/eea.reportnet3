package org.eea.dataflow.mapper;

import org.apache.commons.lang.StringUtils;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.interfaces.vo.dataflow.DataflowInternalVO;
import org.eea.interfaces.vo.document.DocumentVO;
import org.eea.mapper.IMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * The Interface DataflowMapper.
 */
@Mapper(componentModel = "spring")
public interface DataflowInternalMapper extends IMapper<Dataflow, DataflowInternalVO> {


  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the data flow VO
   */
  @Override
  @Mapping(source = "obligationId", target = "obligation.obligationId")
  @Mapping(source = "deadlineDate", target = "deadlineDate", dateFormat = "dd/MM/yyyy")
  DataflowInternalVO entityToClass(Dataflow entity);

  /**
   * Class to entity.
   *
   * @param entity the entity
   * @return the dataflow
   */
  @Override
  @Mapping(source = "deadlineDate", target = "deadlineDate", dateFormat = "dd/MM/yyyy")
  @Mapping(source = "obligation.obligationId", target = "obligationId")
  Dataflow classToEntity(DataflowInternalVO entity);

  /**
   * Fill category.
   *
   * @param dataflow the dataflow
   * @param DataflowInternalVO the data flow VO
   */
  @AfterMapping
  default void fillCategory(Dataflow dataflow, @MappingTarget DataflowInternalVO DataflowInternalVO) {
    List<DocumentVO> documents = DataflowInternalVO.getDocuments();
    documents.stream().filter(document -> StringUtils.isNotBlank(document.getName()))
        .forEach(document -> {
          document.setCategory(
              document.getName().substring(document.getName().lastIndexOf('.') + 1).toLowerCase());
          document.setDataflowId(dataflow.getId());
        });
  }
}

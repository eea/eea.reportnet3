package org.eea.dataflow.mapper;

import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.interfaces.vo.dataflow.DataflowPrivateVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The Interface DataflowPrivateMapper.
 */
@Mapper(componentModel = "spring")
public interface DataflowPrivateMapper extends IMapper<Dataflow, DataflowPrivateVO> {

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the dataflow private VO
   */
  @Override
  @Mapping(source = "obligationId", target = "obligation.obligationId")
  @Mapping(source = "deadlineDate", target = "deadlineDate", dateFormat = "dd/MM/yyyy")
  DataflowPrivateVO entityToClass(Dataflow entity);

  /**
   * Class to entity.
   *
   * @param entity the entity
   * @return the dataflow
   */
  @Override
  @Mapping(source = "deadlineDate", target = "deadlineDate", dateFormat = "dd/MM/yyyy")
  @Mapping(source = "obligation.obligationId", target = "obligationId")
  Dataflow classToEntity(DataflowPrivateVO entity);

}

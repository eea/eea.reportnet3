package org.eea.dataflow.mapper;

import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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
  @Mapping(source = "deadlineDate", target = "deadlineDate", dateFormat = "dd/MM/yyyy")
  DataFlowVO entityToClass(Dataflow entity);


}

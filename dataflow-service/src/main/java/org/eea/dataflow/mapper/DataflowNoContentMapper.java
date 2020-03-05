package org.eea.dataflow.mapper;

import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The Interface DataflowNoContentMapper.
 */
@Mapper(componentModel = "spring")
public interface DataflowNoContentMapper extends IMapper<Dataflow, DataFlowVO> {


  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the data flow VO
   */
  @Override
  @Mapping(source = "deadlineDate", target = "deadlineDate", dateFormat = "dd/MM/yyyy")
  @Mapping(source = "documents", target = "documents", ignore = true)
  @Mapping(source = "weblinks", target = "weblinks", ignore = true)
  @Mapping(source = "contributors", target = "contributors", ignore = true)
  @Mapping(source = "representatives", target = "representatives", ignore = true)
  DataFlowVO entityToClass(Dataflow entity);

}

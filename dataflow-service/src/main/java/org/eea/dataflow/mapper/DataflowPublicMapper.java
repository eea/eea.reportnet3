package org.eea.dataflow.mapper;

import org.eea.dataflow.persistence.domain.Dataflow;
import org.eea.interfaces.vo.dataflow.DataflowPublicVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The Interface DataflowPublicMapper.
 */
@Mapper(componentModel = "spring")
public interface DataflowPublicMapper extends IMapper<Dataflow, DataflowPublicVO> {


  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the dataflow public VO
   */
  @Override
  @Mapping(source = "obligationId", target = "obligation.obligationId")
  @Mapping(source = "deadlineDate", target = "deadlineDate", dateFormat = "dd/MM/yyyy")
  DataflowPublicVO entityToClass(Dataflow entity);

  /**
   * Class to entity.
   *
   * @param entity the entity
   * @return the dataflow
   */
  @Override
  @Mapping(source = "deadlineDate", target = "deadlineDate", dateFormat = "dd/MM/yyyy")
  @Mapping(source = "obligation.obligationId", target = "obligationId")
  Dataflow classToEntity(DataflowPublicVO entity);


}

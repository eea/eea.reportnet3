package org.eea.dataflow.mapper;

import org.eea.dataflow.persistence.domain.FMEUser;
import org.eea.interfaces.vo.dataflow.FMEUserVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;

/**
 * The Interface FMEUserMapper.
 */
@Mapper(componentModel = "spring")
public interface FMEUserMapper extends IMapper<FMEUser, FMEUserVO> {


  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the data flow VO
   */
  @Override
  FMEUserVO entityToClass(FMEUser entity);

  /**
   * Class to entity.
   *
   * @param entity the entity
   * @return the dataflow
   */
  @Override
  FMEUser classToEntity(FMEUserVO entity);

}

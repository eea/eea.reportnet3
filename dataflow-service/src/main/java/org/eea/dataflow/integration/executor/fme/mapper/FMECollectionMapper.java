package org.eea.dataflow.integration.executor.fme.mapper;

import org.eea.dataflow.integration.executor.fme.domain.FMECollection;
import org.eea.interfaces.vo.integration.fme.FMECollectionVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;

/**
 * The Interface FMECollectionMapper.
 */
@Mapper(componentModel = "spring")
public interface FMECollectionMapper extends IMapper<FMECollection, FMECollectionVO> {

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the integration VO
   */
  @Override
  FMECollectionVO entityToClass(FMECollection entity);


  /**
   * Class to entity.
   *
   * @param model the model
   * @return the integration
   */
  @Override
  FMECollection classToEntity(FMECollectionVO model);

}

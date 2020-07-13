package org.eea.dataflow.mapper;

import org.eea.dataflow.integration.executor.fme.domain.Collection;
import org.eea.interfaces.vo.integration.CollectionVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CollectionMapper extends IMapper<Collection, CollectionVO> {

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the integration VO
   */
  @Override
  CollectionVO entityToClass(Collection entity);


  /**
   * Class to entity.
   *
   * @param model the model
   * @return the integration
   */
  @Override
  Collection classToEntity(CollectionVO model);

}

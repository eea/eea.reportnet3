package org.eea.dataflow.mapper;

import org.eea.dataflow.persistence.domain.DataProviderGroup;
import org.eea.interfaces.vo.dataflow.DataProviderGroupVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;

/**
 * The Interface DataProviderMapper.
 */
@Mapper(componentModel = "spring")
public interface DataProviderGroupMapper extends IMapper<DataProviderGroup, DataProviderGroupVO> {

  /**
   * Class to entity.
   *
   * @param model the model
   * @return the data provider
   */
  @Override
  DataProviderGroup classToEntity(DataProviderGroupVO model);

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the data provider VO
   */
  @Override
  DataProviderGroupVO entityToClass(DataProviderGroup entity);
}

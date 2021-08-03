package org.eea.dataflow.mapper;

import org.eea.dataflow.persistence.domain.DataProvider;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The Interface DataProviderMapper.
 */
@Mapper(componentModel = "spring")
public interface DataProviderMapper extends IMapper<DataProvider, DataProviderVO> {

  /**
   * Class to entity.
   *
   * @param model the model
   * @return the data provider
   */
  @Override
  @Mapping(source = "groupId", target = "dataProviderGroup.id")
  DataProvider classToEntity(DataProviderVO model);

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the data provider VO
   */
  @Override
  @Mapping(source = "dataProviderGroup.id", target = "groupId")
  DataProviderVO entityToClass(DataProvider entity);
}

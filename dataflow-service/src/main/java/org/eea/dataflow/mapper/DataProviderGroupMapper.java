package org.eea.dataflow.mapper;

import org.eea.dataflow.persistence.domain.DataProviderGroup;
import org.eea.interfaces.vo.dataflow.DataProviderCodeVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


/**
 * The Interface DataProviderGroupMapper.
 */
@Mapper(componentModel = "spring")
public interface DataProviderGroupMapper extends IMapper<DataProviderGroup, DataProviderCodeVO> {

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the DataProviderCodeVO
   */
  @Override
  @Mapping(source = "id", target = "dataProviderGroupId")
  @Mapping(source = "name", target = "label")
  DataProviderCodeVO entityToClass(DataProviderGroup entity);

  /**
   * Class to entity.
   *
   * @param entity the entity
   * @return the DataProviderGroup
   */
  @Override
  @Mapping(source = "dataProviderGroupId", target = "id")
  @Mapping(source = "label", target = "name")
  DataProviderGroup classToEntity(DataProviderCodeVO entity);

}

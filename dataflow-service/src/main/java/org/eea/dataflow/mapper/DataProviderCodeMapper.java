package org.eea.dataflow.mapper;

import org.eea.dataflow.persistence.domain.DataProviderCode;
import org.eea.interfaces.vo.dataflow.DataProviderCodeVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;

/**
 * The Interface DataProviderCodeMapper.
 */
@Mapper(componentModel = "spring")
public interface DataProviderCodeMapper extends IMapper<DataProviderCode, DataProviderCodeVO> {

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the data provider code VO
   */
  @Override
  DataProviderCodeVO entityToClass(DataProviderCode entity);

  /**
   * Class to entity.
   *
   * @param model the model
   * @return the data provider code
   */
  @Override
  DataProviderCode classToEntity(DataProviderCodeVO model);
}

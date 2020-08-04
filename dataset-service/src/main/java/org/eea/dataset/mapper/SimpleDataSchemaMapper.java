package org.eea.dataset.mapper;

import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.interfaces.vo.dataset.schemas.SimpleDatasetSchemaVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The Interface DataSetMapper.
 */
@Mapper(componentModel = "spring")
public interface SimpleDataSchemaMapper extends IMapper<DataSetSchema, SimpleDatasetSchemaVO> {


  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the simple dataset schema VO
   */
  @Override
  @Mapping(source = "tableSchemas", target = "tables")
  SimpleDatasetSchemaVO entityToClass(DataSetSchema entity);

}

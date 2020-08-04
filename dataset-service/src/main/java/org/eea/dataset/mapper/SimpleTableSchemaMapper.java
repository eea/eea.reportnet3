package org.eea.dataset.mapper;

import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.interfaces.vo.dataset.schemas.SimpleTableSchemaVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


/**
 * The Interface TableSchemaMapper.
 */
@Mapper(componentModel = "spring")
public interface SimpleTableSchemaMapper extends IMapper<TableSchema, SimpleTableSchemaVO> {


  /**
   * Entity to class.
   *
   * @param model the model
   * @return the field schema VO
   */
  @Mapping(source = "nameTableSchema", target = "tableName")
  @Mapping(source = "recordSchema.fieldSchemas", target = "fields")
  SimpleTableSchemaVO entityToClass(TableSchema tableSchema);

}


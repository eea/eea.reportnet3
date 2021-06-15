package org.eea.dataset.mapper;

import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.interfaces.vo.dataset.schemas.SimpleDatasetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.SimpleFieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.SimpleTableSchemaVO;
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

  /**
   * Entity to class.
   *
   * @param tableSchema the table schema
   * @return the field schema VO
   */
  @Mapping(source = "nameTableSchema", target = "tableName")
  @Mapping(source = "recordSchema.fieldSchema", target = "fields")
  SimpleTableSchemaVO entityToClass(TableSchema tableSchema);

  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the field schema VO
   */
  @Mapping(source = "headerName", target = "fieldName")
  @Mapping(source = "type", target = "fieldType")
  SimpleFieldSchemaVO entityToClass(FieldSchema entity);



}

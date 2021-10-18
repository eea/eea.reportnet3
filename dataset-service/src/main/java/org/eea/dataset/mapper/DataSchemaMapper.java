package org.eea.dataset.mapper;

import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The Interface DataSetMapper.
 */
@Mapper(componentModel = "spring")
public interface DataSchemaMapper extends IMapper<DataSetSchema, DataSetSchemaVO> {


  /**
   * Entity to class.
   *
   * @param entity the entity
   * @return the data set schema VO
   */
  @Override
  DataSetSchemaVO entityToClass(DataSetSchema entity);


  /**
   * Map.
   *
   * @param value the value
   * @return the string
   */
  default String map(ObjectId value) {
    return value != null ? value.toString() : null;
  }

  /**
   * Map.
   *
   * @param value the value
   * @return the object id
   */
  default ObjectId map(String value) {
    return new ObjectId(value);
  }

  /**
   * Entity to class.
   *
   * @param model the model
   * @return the field schema VO
   */
  @Mapping(source = "headerName", target = "name")
  @Mapping(source = "idFieldSchema", target = "id")
  FieldSchemaVO entityToClass(FieldSchema model);



}

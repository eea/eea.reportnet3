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
public abstract class DataSchemaMapper implements IMapper<DataSetSchema, DataSetSchemaVO> {
  

  public abstract DataSetSchemaVO entityToClass(DataSetSchema entity);
  
  
  String map(ObjectId value) {
    return value.toString();
  }
  ObjectId map(String value) {
    return new ObjectId(value);
  }
  
  @Mapping(source = "headerName", target = "name")
  @Mapping(source = "idFieldSchema", target = "id")
  public abstract FieldSchemaVO entityToClass(FieldSchema model);

}

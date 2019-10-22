package org.eea.dataset.mapper;

import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


/**
 * The Interface TableSchemaMapper.
 */
@Mapper(componentModel = "spring")
public interface TableSchemaMapper extends IMapper<TableSchema, TableSchemaVO> {

  /**
   * Map.
   *
   * @param value the value
   * @return the string
   */
  default String map(ObjectId value) {
    return value.toString();
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


package org.eea.dataset.mapper;

import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaIdNameVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;


/**
 * The Interface TableSchemaMapper.
 */
@Mapper(componentModel = "spring")
public interface TableSchemaIdNameMapper extends IMapper<TableSchema, TableSchemaIdNameVO> {

  /**
   * Map.
   *
   * @param value the value
   * @return the string
   */
  default String map(ObjectId value) {
    if (value != null) {
      return value.toString();
    } else {
      return null;
    }
  }

  /**
   * Map.
   *
   * @param value the value
   * @return the object id
   */
  default ObjectId map(String value) {
    if (value != null) {
      return new ObjectId(value);
    } else {
      return null;
    }
  }


}


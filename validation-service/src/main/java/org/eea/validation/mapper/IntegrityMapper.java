package org.eea.validation.mapper;

import org.bson.types.ObjectId;
import org.eea.interfaces.vo.dataset.schemas.rule.IntegrityVO;
import org.eea.mapper.IMapper;
import org.eea.validation.persistence.schemas.IntegritySchema;
import org.mapstruct.Mapper;

/**
 * The Interface RulesSchemaMapper.
 */
@Mapper(componentModel = "spring")
public interface IntegrityMapper extends IMapper<IntegritySchema, IntegrityVO> {

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

package org.eea.validation.mapper;

import org.bson.types.ObjectId;
import org.eea.interfaces.vo.dataset.schemas.rule.IntegrityVO;
import org.eea.mapper.IMapper;
import org.eea.validation.persistence.schemas.IntegritySchema;

/**
 * The Interface RulesSchemaMapper.
 */
public interface IntegrityMapper extends IMapper<IntegritySchema, IntegrityVO> {

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
}

package org.eea.dataset.mapper;

import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.rule.RulesSchema;
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;
import org.eea.mapper.IMapper;
import org.mapstruct.Mapper;

/**
 * The Interface RulesSchemaMapper.
 */
@Mapper(componentModel = "spring")
public interface RulesSchemaMapper extends IMapper<RulesSchema, RulesSchemaVO> {

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

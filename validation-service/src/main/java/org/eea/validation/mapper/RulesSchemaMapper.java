package org.eea.validation.mapper;

import org.bson.types.ObjectId;
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;
import org.eea.mapper.IMapper;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.mapstruct.Mapper;

/**
 * The Interface RulesSchemaMapper.
 */
@Mapper(componentModel = "spring", uses = {RuleMapper.class})
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

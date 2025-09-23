package org.eea.validation.mapper;

import org.bson.types.ObjectId;
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;
import org.eea.mapper.IMapper;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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
  @Mapping(
          target = "automaticQCsDefaultLevelError",
          source = "automaticQCsDefaultLevelError",
          defaultValue = "ERROR"
  )
  default String map(ObjectId value) {
    return value.toString();
  }

  /**
   * Map.
   *
   * @param value the value
   * @return the object id
   */
  @Mapping(
          target = "automaticQCsDefaultLevelError",
          source = "automaticQCsDefaultLevelError",
          defaultValue = "ERROR"
  )
  default ObjectId map(String value) {
    return new ObjectId(value);
  }
}

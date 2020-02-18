package org.eea.validation.mapper;

import org.bson.types.ObjectId;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.mapper.IMapper;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.mapstruct.Mapper;

/**
 * The Interface RuleMapper.
 */
@Mapper(componentModel = "spring")
public interface RuleMapper extends IMapper<Rule, RuleVO> {


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



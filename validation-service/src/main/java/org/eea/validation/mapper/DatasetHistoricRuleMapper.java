package org.eea.validation.mapper;

import org.bson.types.ObjectId;
import org.eea.interfaces.vo.dataset.schemas.audit.DatasetHistoricRuleVO;
import org.eea.mapper.IMapper;
import org.eea.validation.persistence.schemas.audit.RuleHistoricInfo;
import org.mapstruct.Mapper;

/**
 * The Interface DatasetHistoricRuleMapper.
 */
@Mapper(componentModel = "spring")
public interface DatasetHistoricRuleMapper
    extends IMapper<RuleHistoricInfo, DatasetHistoricRuleVO> {
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

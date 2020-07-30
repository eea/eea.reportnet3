package org.eea.dataset.mapper;

import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.rule.Rule;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.mapper.IMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * The Interface RuleMapper.
 */
@Mapper(componentModel = "spring")
public interface RuleMapper extends IMapper<Rule, RuleVO> {

  /**
   * Class to entity.
   *
   * @param ruleVO the rule VO
   * @return the rule
   */
  @Override
  @Mapping(source = "ruleId", target = "ruleId", ignore = true)
  @Mapping(source = "referenceId", target = "referenceId", ignore = true)
  @Mapping(source = "whenCondition", target = "whenCondition", ignore = true)
  Rule classToEntity(RuleVO ruleVO);

  /**
   * Entity to class.
   *
   * @param rule the rule
   * @return the rule VO
   */
  @Override
  @Mapping(source = "ruleId", target = "ruleId", ignore = true)
  @Mapping(source = "referenceId", target = "referenceId", ignore = true)
  @Mapping(source = "whenCondition", target = "whenCondition", ignore = true)
  RuleVO entityToClass(Rule rule);

  /**
   * After mapping.
   *
   * @param ruleVO the rule VO
   * @param rule the rule
   */
  @AfterMapping
  default void afterMapping(RuleVO ruleVO, @MappingTarget Rule rule) {
    String ruleId = ruleVO.getRuleId();
    String referenceId = ruleVO.getReferenceId();
    if (ruleId != null && !ruleId.isEmpty()) {
      rule.setRuleId(new ObjectId(ruleId));
    }
    if (referenceId != null && !referenceId.isEmpty()) {
      rule.setReferenceId(new ObjectId(referenceId));
    }
  }

  /**
   * After mapping.
   *
   * @param rule the rule
   * @param ruleVO the rule VO
   */
  @AfterMapping
  default void afterMapping(Rule rule, @MappingTarget RuleVO ruleVO) {
    ruleVO.setRuleId(rule.getRuleId().toString());
    ruleVO.setReferenceId(rule.getReferenceId().toString());
  }
}

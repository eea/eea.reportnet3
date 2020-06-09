package org.eea.validation.mapper;

import org.bson.types.ObjectId;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleExpressionVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.mapper.IMapper;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * The Interface RuleMapper.
 */
@Mapper(componentModel = "spring")
public interface RuleMapper extends IMapper<Rule, RuleVO> {

  @Override
  @Mapping(source = "ruleId", target = "ruleId", ignore = true)
  @Mapping(source = "referenceId", target = "referenceId", ignore = true)
  @Mapping(source = "whenCondition", target = "whenCondition", ignore = true)
  Rule classToEntity(RuleVO ruleVO);

  @Override
  @Mapping(source = "ruleId", target = "ruleId", ignore = true)
  @Mapping(source = "referenceId", target = "referenceId", ignore = true)
  @Mapping(source = "whenCondition", target = "whenCondition", ignore = true)
  RuleVO entityToClass(Rule rule);

  @AfterMapping
  default void afterMapping(RuleVO ruleVO, @MappingTarget Rule rule) {
    String ruleId = ruleVO.getRuleId();
    String referenceId = ruleVO.getReferenceId();
    RuleExpressionVO ruleExpressionVO = ruleVO.getWhenCondition();
    if (ruleId != null && !ruleId.isEmpty()) {
      rule.setRuleId(new ObjectId(ruleId));
    }
    if (referenceId != null && !referenceId.isEmpty()) {
      rule.setReferenceId(new ObjectId(referenceId));
    }
    if (ruleExpressionVO != null) {
      rule.setWhenCondition(ruleExpressionVO.toString());
    }
  }

  @AfterMapping
  default void afterMapping(Rule rule, @MappingTarget RuleVO ruleVO) {
    ruleVO.setRuleId(rule.getRuleId().toString());
    ruleVO.setReferenceId(rule.getReferenceId().toString());
    if (!rule.isAutomatic()) {
      ruleVO.setWhenCondition(new RuleExpressionVO(rule.getWhenCondition()));
    }
    if ((rule.getIntegrityConstraintId() != null)) {

    }
  }
}

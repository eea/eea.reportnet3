package org.eea.validation.mapper;

import org.bson.types.ObjectId;
import org.eea.interfaces.dto.dataset.schemas.rule.RuleExpressionDTO;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.mapper.IMapper;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.service.RuleExpressionService;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The Interface RuleMapper.
 */
@Mapper(componentModel = "spring")
public abstract class RuleMapper implements IMapper<Rule, RuleVO> {

  @Autowired
  private RuleExpressionService ruleExpressionService;

  @Override
  @Mapping(source = "ruleId", target = "ruleId", ignore = true)
  @Mapping(source = "referenceId", target = "referenceId", ignore = true)
  @Mapping(source = "whenCondition", target = "whenCondition", ignore = true)
  public abstract Rule classToEntity(RuleVO ruleVO);

  @Override
  @Mapping(source = "ruleId", target = "ruleId", ignore = true)
  @Mapping(source = "referenceId", target = "referenceId", ignore = true)
  @Mapping(source = "whenCondition", target = "whenCondition", ignore = true)
  public abstract RuleVO entityToClass(Rule rule);

  @AfterMapping
  public void afterMapping(RuleVO ruleVO, @MappingTarget Rule rule) {
    String ruleId = ruleVO.getRuleId();
    String referenceId = ruleVO.getReferenceId();
    RuleExpressionDTO ruleExpressionDTO = ruleVO.getWhenCondition();
    if (ruleId != null && !ruleId.isEmpty()) {
      rule.setRuleId(new ObjectId(ruleId));
    }
    if (referenceId != null && !referenceId.isEmpty()) {
      rule.setReferenceId(new ObjectId(referenceId));
    }
    if (ruleExpressionDTO != null) {
      rule.setWhenCondition(ruleExpressionService.convertToString(ruleExpressionDTO));
    }
  }

  @AfterMapping
  public void afterMapping(Rule rule, @MappingTarget RuleVO ruleVO) {
    ruleVO.setRuleId(rule.getRuleId().toString());
    ruleVO.setReferenceId(rule.getReferenceId().toString());

    if ((null == ruleVO.getSqlSentence() || ruleVO.getSqlSentence().isEmpty())
        && (!rule.isAutomatic() && !EntityTypeEnum.DATASET.equals(rule.getType())
            && !EntityTypeEnum.TABLE.equals(rule.getType()))) {
      ruleVO.setWhenCondition(ruleExpressionService.convertToDTO(rule.getWhenCondition()));
    }
  }
}

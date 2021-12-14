package org.eea.validation.mapper;

import org.apache.commons.lang3.StringUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The Interface RuleMapper.
 */
@Mapper(componentModel = "spring")
public abstract class RuleMapper implements IMapper<Rule, RuleVO> {

  /** The rule expression service. */
  @Autowired
  private RuleExpressionService ruleExpressionService;

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

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
  public abstract Rule classToEntity(RuleVO ruleVO);

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
  public abstract RuleVO entityToClass(Rule rule);

  /**
   * After mapping.
   *
   * @param ruleVO the rule VO
   * @param rule the rule
   */
  @AfterMapping
  public void afterMapping(RuleVO ruleVO, @MappingTarget Rule rule) {
    String ruleId = ruleVO.getRuleId();
    String referenceId = ruleVO.getReferenceId();
    RuleExpressionDTO ruleExpressionDTO = ruleVO.getWhenCondition();
    String sqlSentence = ruleVO.getSqlSentence();
    Double sqlCost = ruleVO.getSqlCost();
    if (ruleId != null && !ruleId.isEmpty()) {
      rule.setRuleId(new ObjectId(ruleId));
    }
    if (referenceId != null && !referenceId.isEmpty()) {
      rule.setReferenceId(new ObjectId(referenceId));
    }
    if (sqlSentence == null || sqlSentence.isEmpty()) {
      rule.setSqlSentence(null);
    }
    if (ruleExpressionDTO != null) {
      rule.setWhenCondition(ruleExpressionService.convertToString(ruleExpressionDTO));
    }
    if (sqlCost != null) {
      rule.setSqlCost(sqlCost);
    }
  }

  /**
   * After mapping.
   *
   * @param rule the rule
   * @param ruleVO the rule VO
   */
  @AfterMapping
  public void afterMapping(Rule rule, @MappingTarget RuleVO ruleVO) {
    ruleVO.setRuleId(rule.getRuleId().toString());
    ruleVO.setReferenceId(rule.getReferenceId().toString());
    // We have to convert the rule's when condition, in case of it's a manual and record or field
    // rule
    if (StringUtils.isBlank(ruleVO.getSqlSentence())
        && (!rule.isAutomatic() && !EntityTypeEnum.DATASET.equals(rule.getType())
            && !EntityTypeEnum.TABLE.equals(rule.getType()))) {
      try {
        ruleVO.setWhenCondition(ruleExpressionService.convertToDTO(rule.getWhenCondition()));
      } catch (IllegalStateException e) {
        ruleVO.setEnabled(false);
        LOG_ERROR.error("Error with the rule {}", ruleVO);
      }
    }
  }
}

package org.eea.validation.service;

import java.util.Map;
import org.eea.interfaces.dto.dataset.schemas.rule.RuleExpressionDTO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;

/** The Interface RuleExpressionService. */
public interface RuleExpressionService {

  /**
   * Converts a String containing a rule expression (Java code) into a RuleExpressionDTO data
   * structure.
   *
   * @param ruleExpressionString the rule expression string
   * @return the rule expression DTO
   */
  RuleExpressionDTO convertToDTO(String ruleExpressionString);

  /**
   * Converts the RuleExpressionDTO data structure into a Java executable code.
   *
   * @param ruleExpressionDTO the rule expression DTO
   * @return the string
   */
  String convertToString(RuleExpressionDTO ruleExpressionDTO);

  /**
   * Checks if the syntax of the rule expression String is Java compatible.
   *
   * @param ruleExpressionString the rule expression string
   * @param entityType the entity type
   * @param dataTypeMap the data type map
   * @return true, if is data type compatible
   */
  boolean isDataTypeCompatible(String ruleExpressionString, EntityTypeEnum entityType,
      Map<String, DataType> dataTypeMap);

  /**
   * Checks if is data type compatible.
   *
   * @param ruleExpressionDTO the rule expression DTO
   * @param entityType the entity type
   * @param dataTypeMap the data type map
   * @return true, if is data type compatible
   */
  boolean isDataTypeCompatible(RuleExpressionDTO ruleExpressionDTO, EntityTypeEnum entityType,
      Map<String, DataType> dataTypeMap);
}

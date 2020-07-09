package org.eea.interfaces.dto.dataset.schemas.rule;

import java.util.List;
import org.eea.interfaces.vo.dataset.schemas.rule.enums.RuleOperatorEnum;
import lombok.Data;

/** Instantiates a new rule expression DTO. */
@Data
public class RuleExpressionDTO {

  /** The operator. */
  private RuleOperatorEnum operator;

  /** The params. */
  private List<Object> params;
}

package org.eea.interfaces.vo.dataset.schemas.audit;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class RuleHistoricInfoVO.
 */
@Getter
@Setter
@ToString
public class RuleHistoricInfoVO {

  /** The rule info id. */
  private String ruleInfoId;

  /** The user. */
  private String user;

  /** The timestamp. */
  private Date timestamp;

  /** The rule id. */
  private String ruleId;

  /** The metadata. */
  private boolean metadata;

  /** The expression. */
  private boolean expression;

  /** The status. */
  private boolean status;

  /** The rule before. */
  private String ruleBefore;

}

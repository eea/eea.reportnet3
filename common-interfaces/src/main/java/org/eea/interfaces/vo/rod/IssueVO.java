package org.eea.interfaces.vo.rod;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Issue vo.
 */
@Getter
@Setter
@ToString
public class IssueVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 3508351291587165443L;

  /** The issue id. */
  private Integer issueId;

  /** The issue name. */
  private String issueName;
}

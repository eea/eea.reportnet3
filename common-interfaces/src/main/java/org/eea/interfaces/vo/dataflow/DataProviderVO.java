package org.eea.interfaces.vo.dataflow;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class DataProviderVO.
 */
@Getter
@Setter
@ToString
public class DataProviderVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -6924509754041958192L;

  /** The id. */
  private Long id;

  /** The group. */
  private String group;

  /** The label. */
  private String label;

  /** The code. */
  private String code;

  /** The group id. */
  private Long groupId;
}

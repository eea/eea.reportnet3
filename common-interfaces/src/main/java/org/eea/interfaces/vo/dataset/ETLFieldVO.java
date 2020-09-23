package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Class ETLFieldVO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ETLFieldVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -4908583655061668044L;

  /** The table name. */
  private String fieldName;

  /** The records. */
  private String value;
}

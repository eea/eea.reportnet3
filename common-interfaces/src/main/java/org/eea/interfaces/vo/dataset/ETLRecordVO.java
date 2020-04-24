package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Class ETLRecordVO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ETLRecordVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -8755775977405479894L;

  /** The records. */
  private List<ETLFieldVO> fields;
}

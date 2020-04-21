package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class ETLRecordVO.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ETLRecordVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -8755775977405479894L;

  /** The records. */
  private List<ETLFieldVO> fields;
}

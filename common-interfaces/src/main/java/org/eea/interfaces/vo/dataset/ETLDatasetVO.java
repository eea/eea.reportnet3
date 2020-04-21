package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class ETLDatasetVO.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ETLDatasetVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -6589783607872899667L;

  /** The etl table VO. */
  private List<ETLTableVO> tables;
}

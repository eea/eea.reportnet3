package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class ETLTableVO.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ETLTableVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -391481666784381904L;

  /** The table name. */
  private String tableName;

  /** The records. */
  private List<ETLRecordVO> records;
}

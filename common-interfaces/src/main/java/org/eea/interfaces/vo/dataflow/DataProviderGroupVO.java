package org.eea.interfaces.vo.dataflow;

import java.io.Serializable;
import org.eea.interfaces.vo.dataflow.enums.TypeDataProviderEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class DataProviderVO.
 */
@Getter
@Setter
@ToString
public class DataProviderGroupVO implements Serializable {

  private static final long serialVersionUID = 1L;

  /** The id. */
  private Long id;

  /** The group. */
  private String name;

  /** The label. */
  private TypeDataProviderEnum type;
}

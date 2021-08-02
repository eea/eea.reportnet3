package org.eea.interfaces.vo.dataflow;

import java.io.Serializable;
import org.eea.interfaces.vo.dataflow.enums.TypeDataProviderEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class DataProviderGroupVO.
 */
@Getter
@Setter
@ToString
public class DataProviderGroupVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The id. */
  private Long id;

  /** The name. */
  private String name;

  /** The type. */
  private TypeDataProviderEnum type;
}

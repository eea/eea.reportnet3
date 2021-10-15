package org.eea.interfaces.vo.dataflow;

import java.io.Serializable;
import org.eea.interfaces.vo.dataflow.enums.TypeDataflowEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class DataflowCountVO.
 */
@Getter
@Setter
@ToString
public class DataflowCountVO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The type. */
  protected TypeDataflowEnum type;


  /** The amount. */
  protected Long amount;


}

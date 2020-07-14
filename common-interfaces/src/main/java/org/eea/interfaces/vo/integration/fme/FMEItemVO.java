package org.eea.interfaces.vo.integration.fme;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class ItemVO.
 */
@Getter
@Setter
@ToString
public class FMEItemVO implements Serializable {

  private static final long serialVersionUID = 7386037189878102052L;

  /** The name. */
  private String name;

}

package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class ValidationLinkVO.
 */
@Setter
@Getter
@ToString
public class ValidationLinkVO implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 7490180881347461296L;
  /** The page. */
  private ValidationLinkContentVO page;

}

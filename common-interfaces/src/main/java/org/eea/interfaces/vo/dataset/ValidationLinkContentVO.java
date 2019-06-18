package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class ValidationLinkContentVO.
 */
@Setter
@Getter
@ToString
public class ValidationLinkContentVO implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -7699293404215293093L;

  /** The num page. */
  private Integer numPage;

  /** The table. */
  private TableVO table;

}

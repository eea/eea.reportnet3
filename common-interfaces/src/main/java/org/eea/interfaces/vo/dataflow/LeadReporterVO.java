package org.eea.interfaces.vo.dataflow;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class LeadReporterVO.
 */
@Getter
@Setter
@ToString
public class LeadReporterVO implements Serializable {

  private static final long serialVersionUID = 4137932359182882947L;

  /** The id. */
  private Long id;

  /** The email. */
  private String email;

  /** The representative id. */
  private Long representativeId;

  /** The invalid. */
  private Boolean invalid;

}

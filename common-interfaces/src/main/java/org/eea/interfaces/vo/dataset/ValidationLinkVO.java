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

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 7490180881347461296L;

  /** The position. */
  private Long position;

  /** The name table schema. */
  private String nameTableSchema;

  /** The id table schema. */
  private String idTableSchema;

  /** The id record. */
  private String idRecord;

}

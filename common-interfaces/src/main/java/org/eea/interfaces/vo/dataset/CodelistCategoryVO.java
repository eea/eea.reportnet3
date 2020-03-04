package org.eea.interfaces.vo.dataset;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class CodelistVO.
 */
@Getter
@Setter
@ToString
@Deprecated
public class CodelistCategoryVO {

  /** The id. */
  private Long id;

  /** The short code. */
  private String shortCode;

  /** The description. */
  private String description;

  /** The codelist number. */
  private int codelistNumber;

}

package org.eea.interfaces.vo.dataset;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class CodelistItemVO.
 *
 * @deprecated (unused)
 */
@Getter
@Setter
@ToString
@Deprecated
public class CodelistItemVO {

  /** The id. */
  private Long id;

  /** The short code. */
  private String shortCode;

  /** The label. */
  private String label;

  /** The definition. */
  private String definition;

  /** The codelist id. */
  private Long codelistId;
}

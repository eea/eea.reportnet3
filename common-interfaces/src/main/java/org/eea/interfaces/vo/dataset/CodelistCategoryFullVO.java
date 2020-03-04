package org.eea.interfaces.vo.dataset;

import java.util.List;
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
public class CodelistCategoryFullVO {

  /** The id. */
  private Long id;

  /** The short code. */
  private String shortCode;

  /** The description. */
  private String description;

  /** The codelists. */
  private List<CodelistVO> codelists;

}

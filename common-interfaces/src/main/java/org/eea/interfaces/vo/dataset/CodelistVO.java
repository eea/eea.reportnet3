package org.eea.interfaces.vo.dataset;

import java.util.List;
import org.eea.interfaces.vo.dataset.enums.CodelistStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class CodelistVO.
 */
@Getter
@Setter
@ToString
public class CodelistVO {

  /** The id. */
  private Long id;

  /** The name. */
  private String name;

  /** The description. */
  private String description;

  /** The category. */
  private CodelistCategoryVO category;

  /** The version. */
  private Long version;

  /** The items id. */
  private List<CodelistItemVO> items;

  /** The status. */
  private CodelistStatusEnum status;
}

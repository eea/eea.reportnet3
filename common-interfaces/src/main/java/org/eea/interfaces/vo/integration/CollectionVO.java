package org.eea.interfaces.vo.integration;

import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class CollectionVO.
 */
@Getter
@Setter
@ToString
public class CollectionVO implements Serializable {

  private static final long serialVersionUID = 5709304389024312767L;

  /** The total count. */
  private Integer totalCount;

  /** The items. */
  private List<ItemVO> items;

}

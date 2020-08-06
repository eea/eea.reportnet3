package org.eea.interfaces.vo.dataset.schemas;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class EUTableSchemaVO.
 */

@Getter
@Setter
@ToString
public class SimpleTableSchemaVO {

  /** The table name. */
  private String tableName;

  /** The fields. */
  private List<SimpleFieldSchemaVO> fields;

}

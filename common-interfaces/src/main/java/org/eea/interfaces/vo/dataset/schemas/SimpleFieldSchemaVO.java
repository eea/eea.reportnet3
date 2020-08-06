package org.eea.interfaces.vo.dataset.schemas;

import org.eea.interfaces.vo.dataset.enums.DataType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class FieldSchemaVO.
 */
@Getter
@Setter
@ToString
public class SimpleFieldSchemaVO {

  /** The name. */
  private String fieldName;

  /** The type. */
  private DataType fieldType;

}

package org.eea.dataset.persistence.data.util;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import org.eea.interfaces.vo.dataset.enums.DataType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The type Sort field.
 */
@Getter
@Setter
@ToString
public class SortField {

  private String fieldName;
  private Boolean asc;
  @Enumerated(EnumType.STRING)
  private DataType typefield;

}

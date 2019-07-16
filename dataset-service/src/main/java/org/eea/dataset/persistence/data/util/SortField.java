package org.eea.dataset.persistence.data.util;

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
  private String typefield;

}

package org.eea.interfaces.vo.dataset;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TableValidationVO {

  private Long id;


  private TableVO idTable;

  private ValidationVO validation;
}

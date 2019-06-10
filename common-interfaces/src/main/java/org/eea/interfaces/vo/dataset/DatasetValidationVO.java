package org.eea.interfaces.vo.dataset;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DatasetValidationVO {

  private Long id;


  private DataSetVO datasetValue;

  private ValidationVO validation;
}

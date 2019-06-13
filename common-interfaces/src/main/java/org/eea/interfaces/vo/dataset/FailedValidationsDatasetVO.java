package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class FailedValidationsDatasetVO implements Serializable{
  
  private Long idDataset;
  
  private String idDatasetSchema;
  
  private String nameDataSetSchema;
    
  private List<ErrorsValidationVO> errors;
  
  private Long totalErrors;
  
  
}

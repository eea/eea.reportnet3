package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.interfaces.vo.dataset.enums.TypeErrorEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class ErrorsValidationVO implements Serializable{
  
  private Long idValidation;
  
  private Long idObject;
  
  private String nameTableSchema;
  
  private String message;
  
  private TypeErrorEnum levelError;

  private TypeEntityEnum typeEntity;

  private String validationDate;
    
}

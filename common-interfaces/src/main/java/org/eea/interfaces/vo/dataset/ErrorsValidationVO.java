package org.eea.interfaces.vo.dataset;


import java.io.Serializable;
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
  
  private String levelError;

  private String typeEntity;

  private String validationDate;
  
    
}

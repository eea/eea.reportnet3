package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class TableStatisticsVO implements Serializable{

  private static final long serialVersionUID = 7063146120694063716L;
  
  private Long idTable;
  
  private Boolean tableErrors;
  
  private Long totalErrors;
  
  private Long errorsInTable;
  
  /*private Long totalFields;
  private Long totalFieldsWithErrors;
  private Long totalFieldsWithWarnings;*/
  
  private Long totalRecords;
  private Long totalRecordsWithErrors;
  private Long totalRecordsWithWarnings;
  
  

}

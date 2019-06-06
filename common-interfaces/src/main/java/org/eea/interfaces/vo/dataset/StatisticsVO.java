package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StatisticsVO implements Serializable{
  
  private static final long serialVersionUID = 3252008086898104546L;
  
  private Long datasetId;
  
  private Boolean datasetErrors;
  
  private Long totalDatasetErrors;
  
  private List<TableStatisticsVO> tables;
  

}

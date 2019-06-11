package org.eea.interfaces.vo.dataset;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The class TableStatisticsVO.
 *
 */

@Setter
@Getter
@ToString
public class TableStatisticsVO implements Serializable{

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 7063146120694063716L;
  
 
  /** The id table schema. */
  private String idTableSchema;
  
  /** The name table schema. */
  private String nameTableSchema;
  
  /** The table errors. */
  private Boolean tableErrors;
  
  /** The total errors. */
  private Long totalErrors;

  /** The total records. */
  private Long totalRecords;
  
  /** The total records with errors. */
  private Long totalRecordsWithErrors;
  
  /** The total records with warnings. */
  private Long totalRecordsWithWarnings;
  
  

}

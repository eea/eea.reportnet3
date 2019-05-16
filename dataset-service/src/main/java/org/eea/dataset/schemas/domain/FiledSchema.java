/**
 * 
 */
package org.eea.dataset.schemas.domain;

import org.springframework.data.annotation.Id;
import lombok.Data;

/**
 * @author Mario Severa
 *
 */
@Data
public class FiledSchema {
  @Id
  private Long id;

  private Long idRecord;

  private String headerName;

  private HeaderType headerType;

}

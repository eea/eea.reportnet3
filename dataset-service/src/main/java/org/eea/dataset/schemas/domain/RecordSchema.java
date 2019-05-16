/**
 * 
 */
package org.eea.dataset.schemas.domain;

import java.util.List;
import org.springframework.data.annotation.Id;
import lombok.Data;

/**
 * @author Mario Severa
 *
 */
@Data
public class RecordSchema {
  @Id
  private Long id;

  private Long idTable;

  private List<FiledSchema> filedSchemas;

}

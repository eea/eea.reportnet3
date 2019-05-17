package org.eea.dataset.schemas.domain;

import javax.persistence.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

/**
 * @author Mario Severa
 *
 */
@Data
@Document(collection = "DatabaseSequences")
public class DatabaseSequences {

  @Id
  private String id;

  private long seq;

}

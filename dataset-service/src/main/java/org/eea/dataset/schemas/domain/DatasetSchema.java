package org.eea.dataset.schemas.domain;


import javax.persistence.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

/**
 * @author Mario Severa
 *
 */


@Data
@Document(collection = "DatasetSchema")
public class DatasetSchema {
  @Id
  private Long id;

  private String value;

}

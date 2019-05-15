package org.eea.dataset.schemas.domain;


import java.util.List;
import javax.persistence.Id;
import javax.persistence.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

/**
 * @author Mario Severa
 *
 */


@Data
@Document(collection = "DataSetSchema")
public class DataSetSchema {

  @Transient
  public static final String SEQUENCE_NAME = "DataSetSchema_sequence";

  @Id
  private Long id;

  private List<TableSchema> tableSchemas;


}

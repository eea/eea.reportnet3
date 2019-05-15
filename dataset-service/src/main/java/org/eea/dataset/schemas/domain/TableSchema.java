package org.eea.dataset.schemas.domain;

import javax.persistence.Id;
import lombok.Data;

@Data
public class TableSchema {

  @Id
  private Long id;

  private RecordSchema recordSchema;

}

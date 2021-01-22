package org.eea.dataset.service.model;

import java.util.List;
import java.util.Map;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;



@Getter
@Setter
@ToString
public class ImportSchemas {


  private List<DataSetSchema> schemas;
  private Map<String, String> schemaNames;

}

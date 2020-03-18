package org.eea.dataset.persistence.metabase.domain;

import java.util.List;
import org.eea.dataset.persistence.schemas.domain.ReferencedFieldSchema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class FKDataCollection {


  private String idDatasetSchemaOrigin;

  private String representative;

  private Long idDatasetOrigin;

  private List<ReferencedFieldSchema> fks;


}

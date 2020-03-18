package org.eea.dataset.persistence.metabase.domain;

import java.util.List;
import org.eea.dataset.persistence.schemas.domain.ReferencedFieldSchema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;



/**
 * The Class FKDataCollection.
 */
@Getter
@Setter
@ToString
public class FKDataCollection {


  /** The id dataset schema origin. */
  private String idDatasetSchemaOrigin;

  /** The representative. */
  private String representative;

  /** The id dataset origin. */
  private Long idDatasetOrigin;

  /** The fks. */
  private List<ReferencedFieldSchema> fks;


}

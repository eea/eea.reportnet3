package org.eea.dataset.service.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class IntegrityDataCollection.
 */
@Getter
@Setter
@ToString
public class IntegrityDataCollection {

  /** The id dataset schema origin. */
  private String idDatasetSchemaOrigin;

  /** The id dataset origin. */
  private Long idDatasetOrigin;

  /** The id dataset schema referenced. */
  private String idDatasetSchemaReferenced;

  /** The data provider id. */
  private Long dataProviderId;

}

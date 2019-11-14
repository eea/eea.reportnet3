package org.eea.indexsearch.io.kafka.domain;

import lombok.Data;

/**
 * Instantiates a new elastic crossover filter.
 */
@Data
public class ElasticCrossoverFilter {

  /**
   * The Dataflow data.
   */
  private String DataflowData;

  /**
   * The Dataset data.
   */
  private String DatasetData;

  /**
   * The Data C collection data.
   */
  private String DataCCollectionData;

  /**
   * The Organization.
   */
  private String Organization;

}

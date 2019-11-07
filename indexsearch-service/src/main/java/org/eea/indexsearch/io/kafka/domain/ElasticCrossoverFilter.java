package org.eea.indexsearch.io.kafka.domain;

import lombok.Data;

/**
 * Instantiates a new elastic crossover filter.
 */
@Data
public class ElasticCrossoverFilter {

  /** The Dataflow data. */
  String DataflowData;

  /** The Dataset data. */
  String DatasetData;

  /** The Data C collection data. */
  String DataCCollectionData;

  /** The Organization. */
  String Organization;

}

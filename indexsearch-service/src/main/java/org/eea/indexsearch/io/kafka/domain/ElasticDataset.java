package org.eea.indexsearch.io.kafka.domain;

import lombok.Data;

/**
 * Instantiates a new elastic dataset.
 */
@Data
public class ElasticDataset {

  /** The Name. */
  String Name;

  /** The Countries. */
  String Countries;

  /** The Issue. */
  String Issue;

  /** The Release. */
  String Release;

  /** The Acces URL. */
  String AccesURL;

  /** The elastic user. */
  ElasticUser elasticUser;


}

package org.eea.indexsearch.io.kafka.domain;

import lombok.Data;

/**
 * Instantiates a new elastic dataset.
 */
@Data
public class ElasticDataset {

  /**
   * The Name.
   */
  private String Name;

  /**
   * The Countries.
   */
  private String Countries;

  /**
   * The Issue.
   */
  private String Issue;

  /**
   * The Release.
   */
  private String Release;

  /**
   * The Acces URL.
   */
  private String AccesURL;

  /**
   * The elastic user.
   */
  private ElasticUser elasticUser;


}

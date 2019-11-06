package org.eea.indexsearch.io.kafka.domain;

import lombok.Data;

/**
 * Instantiates a new elastic search data.
 */
@Data
public class ElasticSearchData {

  /** The id. */
  private String id;

  /** The elastic user. */
  private ElasticUser elasticUser;

  /** The elastic crossover filter. */
  private ElasticCrossoverFilter elasticCrossoverFilter;

  /** The role name. */
  private String roleName;

  /** The register user name. */
  private String registerUserName;

  /** The register user authorization. */
  private String registerUserAuthorization;

  /** The register user URL. */
  private String registerUserURL;

  /** The organization name. */
  private String organizationName;

  /** The entity event. */
  private EntityEvent entityEvent;


}

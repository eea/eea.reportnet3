package org.eea.indexsearch.io.kafka.domain;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ElasticSearchData {

  private String id;
  private ElasticUser elasticUser;
  private ElasticCrossoverFilter elasticCrossoverFilter;
  private String roleName;
  private String registerUserName;
  private String registerUserAuthorization;
  private String registerUserURL;
  private String organizationName;
  private EntityEvent entityEvent;

}

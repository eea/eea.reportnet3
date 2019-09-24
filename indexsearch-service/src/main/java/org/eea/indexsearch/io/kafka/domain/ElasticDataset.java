package org.eea.indexsearch.io.kafka.domain;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ElasticDataset {

  String Name;
  String Countries;
  String Issue;
  String Release;
  String AccesURL;
  ElasticUser elasticUser;


}

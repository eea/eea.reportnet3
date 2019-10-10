package org.eea.indexsearch.io.kafka.domain;

import java.util.Objects;
import lombok.Data;

@Data
public class ElasticDataset {

  String Name;
  String Countries;
  String Issue;
  String Release;
  String AccesURL;
  ElasticUser elasticUser;


}

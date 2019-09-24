package org.eea.indexsearch.io.kafka.domain;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ElasticCrossoverFilter {
  String DataflowData;
  String DatasetData;
  String DataCCollectionData;
  String Organization;
}

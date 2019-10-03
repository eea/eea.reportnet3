package org.eea.indexsearch.io.kafka.domain;

import java.util.Objects;
import lombok.Data;

@Data
public class ElasticCrossoverFilter {
  String DataflowData;
  String DatasetData;
  String DataCCollectionData;
  String Organization;

  @Override
  public int hashCode() {
    return Objects.hash(DataCCollectionData, DataflowData, DatasetData, Organization);
  }
}

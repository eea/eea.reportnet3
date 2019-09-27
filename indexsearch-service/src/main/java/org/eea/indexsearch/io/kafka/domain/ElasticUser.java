package org.eea.indexsearch.io.kafka.domain;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ElasticUser {
  private String UserId;
  private String FavoriteFlag;

}

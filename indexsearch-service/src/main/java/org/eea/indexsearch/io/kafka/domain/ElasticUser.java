package org.eea.indexsearch.io.kafka.domain;

import java.util.Objects;
import lombok.Data;

@Data
public class ElasticUser {
  private String UserId;
  private String FavoriteFlag;

  @Override
  public int hashCode() {
    return Objects.hash(FavoriteFlag, UserId);
  }

}

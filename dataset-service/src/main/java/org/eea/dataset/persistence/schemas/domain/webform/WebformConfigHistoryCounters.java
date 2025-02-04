package org.eea.dataset.persistence.schemas.domain.webform;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.Id;
import java.io.Serializable;

@Getter
@Setter
@Document(collection = "WebformConfigHistoryCounters")
public class WebformConfigHistoryCounters implements Serializable {


  private static final long serialVersionUID = 8582940742099554611L;

  @Id
  private String id;

  @Field(value = "seq")
  private long seq;
}

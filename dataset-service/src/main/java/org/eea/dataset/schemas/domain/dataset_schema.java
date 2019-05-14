package org.eea.dataset.schemas.domain;


import javax.persistence.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Mario Severa
 *
 */


@Data
@AllArgsConstructor
@NoArgsConstructor

@Document(collection = "dataset_schema")
public class dataset_schema {
  @Id
  private Long id;

  private String value;


  @Override
  public String toString() {
    return "PrimaryModel{" + "id='" + id + '\'' + ", value='" + value + '\'' + '}';
  }

}

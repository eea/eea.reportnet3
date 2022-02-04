package org.eea.dataset.persistence.schemas.domain.webform;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import javax.persistence.Id;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class WebformConfig.
 */
@Setter
@Getter
@ToString
@Document(collection = "WebformConfig")
public class WebformConfig implements Serializable {


  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -5182631387358793262L;


  /** The id. */
  @Id
  @Field(value = "_id")
  private ObjectId id;

  /** The id referenced. */
  @Field(value = "idReferenced")
  private Long idReferenced;

  /** The name. */
  @Field(value = "name")
  private String name;

  /** The file. */
  @Field(value = "file")
  Map<String, Object> file;


  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, file);
  }

  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    WebformConfig other = (WebformConfig) obj;
    return Objects.equals(id, other.id) && Objects.equals(file, other.file);
  }

}

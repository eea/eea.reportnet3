package org.eea.dataset.persistence.schemas.domain.webform;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.Id;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@Setter
@Getter
@ToString
@Document(collection = "WebformConfigHistory")
public class WebformConfigHistory implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 6920077934013205212L;


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
  private Map<String, Object> file;

  /** The idWebformConfigSchema */
  @Field(value = "idWebformConfigSchema")
  private ObjectId idWebformConfigSchema;

  /** The version */
  @Field(value = "version")
  private Long version;

  @CreatedDate
  @Field(value = "createdAt")
  private Instant createdAt;  // Automatically set timestamp

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
    WebformConfigHistory other = (WebformConfigHistory) obj;
    return Objects.equals(id, other.id) && Objects.equals(file, other.file);
  }
}

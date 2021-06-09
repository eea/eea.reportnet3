package org.eea.dataset.persistence.schemas.domain.pkcatalogue;

import java.util.List;
import java.util.Objects;
import javax.persistence.Id;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class PkCatalogueSchema.
 */
@Getter
@Setter
@ToString
@Document(collection = "PKCatalogue")
public class PkCatalogueSchema {

  /** The id pk. */
  @Id
  @Field(value = "idPk")
  private ObjectId idPk;

  /** The dataflow id. */
  @Field(value = "dataflowId")
  private Long dataflowId;

  /** The referenced. */
  @Field(value = "referencedBy")
  private List<ObjectId> referenced;


  /** The referenced by dataflow. */
  @Field(value = "referencedByDataflow")
  private List<Long> referencedByDataflow;



  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(idPk, referenced);
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
    PkCatalogueSchema other = (PkCatalogueSchema) obj;
    return Objects.equals(idPk, other.idPk) && Objects.equals(referenced, other.referenced);
  }

}

package org.eea.dataset.persistence.schemas.domain.pkcatalogue;

import java.util.List;
import java.util.Objects;
import javax.persistence.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class DataflowReferencedSchema.
 */
@Getter
@Setter
@ToString
@Document(collection = "DataflowReferenced")
public class DataflowReferencedSchema {


  /** The dataflow id. */
  @Id
  @Field(value = "dataflowId")
  private Long dataflowId;


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
    return Objects.hash(dataflowId, referencedByDataflow);
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
    DataflowReferencedSchema other = (DataflowReferencedSchema) obj;
    return Objects.equals(dataflowId, other.dataflowId)
        && Objects.equals(referencedByDataflow, other.referencedByDataflow);
  }

}

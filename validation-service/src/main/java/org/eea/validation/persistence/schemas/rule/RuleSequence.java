package org.eea.validation.persistence.schemas.rule;

import java.util.Objects;
import javax.persistence.Id;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;



/**
 * The Class RuleSequence.
 */
@Getter
@Setter
@ToString
@Document(collection = "RuleSequence")
public class RuleSequence {

  /** The rule sequence id. */
  @Id
  @Field(value = "_id")
  private ObjectId ruleSequenceId;

  /** The id rule schema. */
  @Field(value = "datasetSchemaId")
  private ObjectId datasetSchemaId;

  /** The seq. */
  @Field(value = "seq")
  private long seq;


  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(datasetSchemaId, ruleSequenceId, seq);
  }


  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RuleSequence other = (RuleSequence) obj;
    return Objects.equals(datasetSchemaId, other.datasetSchemaId)
        && Objects.equals(ruleSequenceId, other.ruleSequenceId) && seq == other.seq;
  }



}

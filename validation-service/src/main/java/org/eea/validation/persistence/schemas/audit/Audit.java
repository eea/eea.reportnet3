package org.eea.validation.persistence.schemas.audit;

import java.util.List;
import javax.persistence.Id;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class Audit.
 */
@Getter
@Setter
@ToString
@Document(collection = "Audit")
public class Audit {

  /** The id audit. */
  @Id
  @Field(value = "_id")
  private ObjectId idAudit;

  /** The dataset id. */
  @Field(value = "datasetId")
  private Long datasetId;

  /** The historic. */
  @Field(value = "historic")
  private List<RuleHistoricInfo> historic;

}

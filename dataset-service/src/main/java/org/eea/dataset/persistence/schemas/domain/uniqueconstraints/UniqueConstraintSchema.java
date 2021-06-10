package org.eea.dataset.persistence.schemas.domain.uniqueconstraints;

import java.util.List;
import javax.persistence.Id;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class UniqueConstraintSchema.
 */
@Getter
@Setter
@ToString
@Document(collection = "UniqueConstraintsCatalogue")
public class UniqueConstraintSchema {

  /** The dataset schema id. */
  @Field(value = "datasetSchemaId")
  private ObjectId datasetSchemaId;

  /** The table schema id. */
  @Field(value = "tableSchemaId")
  private ObjectId tableSchemaId;

  /** The unique id. */
  @Id
  @Field(value = "_id")
  private ObjectId uniqueId;

  /** The referenced. */
  @Field(value = "fieldSchemaIds")
  private List<ObjectId> fieldSchemaIds;

}

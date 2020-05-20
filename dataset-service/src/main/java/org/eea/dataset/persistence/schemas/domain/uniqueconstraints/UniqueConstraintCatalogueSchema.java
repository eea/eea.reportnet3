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
 * Gets the referenced.
 *
 * @return the referenced
 */
@Getter
@Setter
@ToString
@Document(collection = "UniqueConstraintsCatalogue")
public class UniqueConstraintCatalogueSchema {

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
  @Field(value = "uniquesConstraints")
  private List<ObjectId> uniquesConstraints;

}

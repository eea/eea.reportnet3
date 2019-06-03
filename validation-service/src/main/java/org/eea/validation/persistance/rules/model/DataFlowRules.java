package org.eea.validation.persistance.rules.model;

import javax.persistence.Id;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class DataSetRule.
 */
@Getter
@Setter
@ToString
@Document(collection = "DataFlow_Rule_Collection")
public class DataFlowRules {

  @Id
  @Field(value = "_id")
  private ObjectId id_Rules;

  @Field(value = "id_DataFlow")
  private Long id_DataFlow;

  @Field(value = "rulesScope")
  private Enum rulesScope;

  @Field(value = "ruleName")
  private String ruleName;

  @Field(value = "whenCondition")
  private String whenCondition;

  @Field(value = "thenCondition")
  private String thenCondition;

}

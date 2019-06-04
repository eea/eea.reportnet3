package org.eea.validation.persistence.rules.model;

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

  /** The id rules. */
  @Id
  @Field(value = "_id")
  private ObjectId id_Rules;

  /** The id data flow. */
  @Field(value = "id_DataFlow")
  private Long id_DataFlow;

  /** The rules scope. */
  @Field(value = "rulesScope")
  private RuleScope ruleScope;

  /** The rule name. */
  @Field(value = "ruleName")
  private String ruleName;

  /** The when condition. */
  @Field(value = "whenCondition")
  private String whenCondition;

  /** The then condition. */
  @Field(value = "thenCondition")
  private String thenCondition;

}

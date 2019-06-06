package org.eea.validation.persistence.rules.model;

import javax.persistence.Id;
import org.bson.types.ObjectId;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class DataSetRule.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "DataFlow_Rule_Collection")
public class DataFlowRule {

  /** The id rules. */
  @Id
  @Field(value = "_id")
  private ObjectId ruleId;

  /** The id data flow. */
  @Field(value = "id_DataFlow")
  private Long dataFlowId;

  /** The rules scope. */
  @Field(value = "rulesScope")
  private TypeEntityEnum ruleScope;

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

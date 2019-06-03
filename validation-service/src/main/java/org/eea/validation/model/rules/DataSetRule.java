package org.eea.validation.model.rules;

import javax.persistence.Id;
import org.bson.types.ObjectId;
import org.eea.validation.persistance.rules.model.RulesModel;
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
@Document(collection = "DataSet_Rule_Collection")
public class DataSetRule extends RulesModel {

  @Id
  @Field(value = "_id")
  private ObjectId id_TableRules;

  @Field(value = "id_DataFlow")
  private Long id_DataFlow;

  @Field(value = "ruleName")
  private String ruleName;

  @Field(value = "whenCondition")
  private String whenCondition;

  @Field(value = "thenCondition")
  private String thenCondition;

}

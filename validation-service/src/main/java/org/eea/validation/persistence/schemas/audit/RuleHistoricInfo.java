package org.eea.validation.persistence.schemas.audit;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Id;
import javax.persistence.Transient;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class RuleHistoricInfo.
 */
@Getter
@Setter
@ToString
public class RuleHistoricInfo implements Serializable {

  /** The Constant serialVersionUID. */
  @Transient
  private static final long serialVersionUID = -8466833976496437805L;

  /** The rule info id. */
  @Id
  @Field(value = "_id")
  private ObjectId ruleInfoId;

  /** The user. */
  @Field(value = "user")
  private String user;

  /** The timestamp. */
  @Field(value = "timestamp")
  private Date timestamp;

  /** The rule id. */
  @Field(value = "ruleId")
  private ObjectId ruleId;

  /** The metadata. */
  @Field(value = "metadata")
  private boolean metadata;

  /** The expression. */
  @Field(value = "expression")
  private boolean expression;

  /** The status. */
  @Field(value = "status")
  private boolean status;

  /** The rule before. */
  @Field(value = "ruleBefore")
  private String ruleBefore;

}

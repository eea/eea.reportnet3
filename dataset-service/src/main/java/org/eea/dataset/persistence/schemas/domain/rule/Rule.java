package org.eea.dataset.persistence.schemas.domain.rule;

import java.util.List;
import java.util.Objects;
import javax.persistence.Id;
import org.bson.types.ObjectId;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class Rule.
 */

@Getter
@Setter
@ToString
public class Rule {
  /** The rule id. */
  @Id
  @Field(value = "_id")
  private ObjectId ruleId;

  /** The dataset schem id. */
  @Field(value = "referenceId")
  private ObjectId referenceId;

  /** The reference field schema PK id. */
  @Field(value = "referenceFieldSchemaPKId")
  private ObjectId referenceFieldSchemaPKId;

  /** The rule name. */
  @Field(value = "ruleName")
  private String ruleName;

  /** The automatic. */
  @Field(value = "automatic")
  private boolean automatic;

  /** The enabled. */
  @Field(value = "enabled")
  private boolean enabled;

  /** The verified. */
  @Field(value = "verified")
  private Boolean verified;

  /** The activation_group. */
  @Field(value = "activationGroup")
  private String activationGroup;

  /** The type. */
  @Field(value = "type")
  private EntityTypeEnum type;

  /** The when condition. */
  @Field(value = "whenCondition")
  private String whenCondition;

  /**
   * The then condition. is a list with 2 redords first is a ERROR message second is a Level ERROR
   */
  @Field(value = "thenCondition")
  private List<String> thenCondition;

  /** The description. */
  @Field(value = "description")
  private String description;

  /** The short code. */
  @Field(value = "shortCode")
  private String shortCode;

  /** The unique constraint id. */
  @Field(value = "uniqueConstraintId")
  private ObjectId uniqueConstraintId;

  /** The integrity constraint id. */
  @Field(value = "integrityConstraintId")
  private ObjectId integrityConstraintId;

  /** The SQLSentence. */
  @Field(value = "sqlSentence")
  private String sqlSentence;


  /** The sql error. */
  @Field(value = "sqlError")
  private String sqlError;

  /** The expression text. */
  @Field(value = "expressionText")
  private String expressionText;


  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(ruleId, referenceId, ruleName, automatic, enabled, activationGroup, type,
        whenCondition, thenCondition, description, shortCode, uniqueConstraintId);
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
    Rule other = (Rule) obj;
    return Objects.equals(ruleId, other.ruleId) && Objects.equals(referenceId, other.referenceId)
        && Objects.equals(ruleName, other.ruleName) && Objects.equals(automatic, other.automatic)
        && Objects.equals(enabled, other.enabled)
        && Objects.equals(activationGroup, other.activationGroup)
        && Objects.equals(type, other.type) && Objects.equals(whenCondition, other.whenCondition)
        && Objects.equals(thenCondition, other.thenCondition)
        && Objects.equals(description, other.description)
        && Objects.equals(shortCode, other.shortCode)
        && Objects.equals(uniqueConstraintId, other.uniqueConstraintId);
  }
}

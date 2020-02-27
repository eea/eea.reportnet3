package org.eea.validation.persistence.schemas.rule;

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

/**
 * Gets the short code.
 *
 * @return the short code
 */
@Getter

/**
 * Sets the short code.
 *
 * @param shortCode the new short code
 */
@Setter

/**
 * To string.
 *
 * @return the java.lang. string
 */
@ToString
public class Rule {
  /** The rule id. */
  @Id
  @Field(value = "_id")
  private ObjectId ruleId;

  /** The dataset schem id. */
  @Field(value = "referenceId")
  private ObjectId referenceId;

  /** The rule name. */
  @Field(value = "ruleName")
  private String ruleName;

  /** The automatic. */
  @Field(value = "automatic")
  private Boolean automatic;

  /** The enabled. */
  @Field(value = "enabled")
  private Boolean enabled;

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

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(ruleId, referenceId, ruleName, automatic, enabled, activationGroup, type,
        whenCondition, thenCondition, description, shortCode);
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
        && Objects.equals(shortCode, other.shortCode);
  }
}

export class Validation {
  constructor({
    activationGroup,
    allExpressions,
    automatic,
    condition,
    date,
    description,
    enabled,
    entityType,
    expressions,
    id,
    isCorrect,
    levelError,
    message,
    name,
    referenceId,
    shortCode
  } = {}) {
    this.activationGroup = activationGroup;
    this.allExpressions = allExpressions;
    this.automatic = automatic;
    this.condition = condition;
    this.date = date;
    this.description = description;
    this.enabled = enabled;
    this.entityType = entityType;
    this.expressions = expressions;
    this.id = id;
    this.isCorrect = isCorrect;
    this.levelError = levelError;
    this.message = message;
    this.name = name;
    this.referenceId = referenceId;
    this.shortCode = shortCode;
  }
}

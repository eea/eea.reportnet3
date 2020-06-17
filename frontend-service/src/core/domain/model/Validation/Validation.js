export class Validation {
  constructor({
    activationGroup,
    allExpressions,
    allExpressionsIf,
    allExpressionsThen,
    automatic,
    condition,
    date,
    description,
    enabled,
    entityType,
    expressions,
    expressionsIf,
    expressionsThen,
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
    this.allExpressionsIf = allExpressionsIf;
    this.allExpressionsThen = allExpressionsThen;
    this.automatic = automatic;
    this.condition = condition;
    this.date = date;
    this.description = description;
    this.enabled = enabled;
    this.entityType = entityType;
    this.expressions = expressions;
    this.expressionsIf = expressionsIf;
    this.expressionsThen = expressionsThen;
    this.id = id;
    this.isCorrect = isCorrect;
    this.levelError = levelError;
    this.message = message;
    this.name = name;
    this.referenceId = referenceId;
    this.shortCode = shortCode;
  }
}

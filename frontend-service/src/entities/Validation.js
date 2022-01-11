export class Validation {
  constructor({
    activationGroup,
    allExpressions,
    allExpressionsIf,
    allExpressionsThen,
    automatic,
    automaticType,
    condition,
    date,
    description,
    enabled,
    entityType,
    expressions,
    expressionsIf,
    expressionsThen,
    expressionText,
    hasHistoric,
    id,
    isCorrect,
    levelError,
    message,
    name,
    referenceId,
    relations,
    ruleId,
    shortCode,
    sqlError,
    sqlSentence,
    sqlSentenceCost
  } = {}) {
    this.activationGroup = activationGroup;
    this.allExpressions = allExpressions;
    this.allExpressionsIf = allExpressionsIf;
    this.allExpressionsThen = allExpressionsThen;
    this.automatic = automatic;
    this.automaticType = automaticType;
    this.condition = condition;
    this.date = date;
    this.description = description;
    this.enabled = enabled;
    this.entityType = entityType;
    this.expressions = expressions;
    this.expressionsIf = expressionsIf;
    this.expressionsThen = expressionsThen;
    this.expressionText = expressionText;
    this.hasHistoric = hasHistoric;
    this.id = id;
    this.isCorrect = isCorrect;
    this.levelError = levelError;
    this.message = message;
    this.name = name;
    this.referenceId = referenceId;
    this.relations = relations;
    this.ruleId = ruleId;
    this.shortCode = shortCode;
    this.sqlError = sqlError;
    this.sqlSentence = sqlSentence;
    this.sqlSentenceCost = sqlSentenceCost;
  }
}

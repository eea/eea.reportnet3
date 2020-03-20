export const setValidationExpression = (expressionId, field, expressions) => {
  const [targetExpression] = expressions.filter(expression => expressionId == expression.expressionId);
  targetExpression[field.key] = field.value.value;
  return expressions;
};

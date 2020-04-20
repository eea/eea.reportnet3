import { config } from 'conf';
export const setValidationExpression = (expressionId, field, expressions) => {
  const [targetExpression] = expressions.filter(expression => expressionId == expression.expressionId);
  switch (field.key) {
    case 'expressionValue':
      targetExpression[field.key] = field.value.value;
      break;
    case 'operatorType':
      targetExpression[field.key] = field.value.value;
      targetExpression.operatorValue = '';
      targetExpression.expressionValue = '';
      break;
    default:
      targetExpression[field.key] = field.value.value;
      break;
  }

  return expressions;
};

import { config } from 'conf';
export const setValidationExpression = (expressionId, field, expressions) => {
  const [targetExpression] = expressions.filter(expression => expressionId === expression.expressionId);

  switch (field.key) {
    case 'expressionValue':
      const { value } = field;

      if (value === null) {
        targetExpression[field.key] = '';
      } else {
        targetExpression[field.key] = value;
      }
      break;

    case 'operatorType':
      targetExpression[field.key] = field.value;
      targetExpression.operatorValue = '';
      targetExpression.expressionValue = '';
      break;

    default:
      targetExpression[field.key] = field.value;
      break;
  }

  return expressions;
};

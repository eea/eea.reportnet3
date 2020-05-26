import { config } from 'conf';
export const setValidationExpression = (expressionId, field, expressions) => {
  console.log('setValidationExpression', expressionId, field, expressions);

  const [targetExpression] = expressions.filter(expression => expressionId === expression.expressionId);
  switch (field.key) {
    case 'expressionValue':
      const {
        value: { value }
      } = field;
      if (value == null) {
        targetExpression[field.key] = '';
      } else {
        targetExpression[field.key] = value;
      }
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

import { config } from 'conf';

export const getExpression = expression => {
  if (expression.operatorType == 'LEN') {
    return {
      operator: config.validations.operatorEquivalences[expression.operatorValue],
      arg1: parseInt(expression.expressionValue),
      arg2: {
        operator: 'LEN',
        arg1: 'VALUE'
      }
    };
  }
  if (expression.operatorType == 'string') {
    return {
      arg1: 'VALUE',
      operator: config.validations.stringOperatorsEquivalences[expression.operatorValue],
      arg2: expression.expressionValue
    };
  }
  return {
    arg1: 'VALUE',
    operator: config.validations.operatorEquivalences[expression.operatorValue],
    arg2: !config.validations.nonNumericOperators.includes(expression.operatorType)
      ? parseInt(expression.expressionValue)
      : expression.expressionValue
  };
};

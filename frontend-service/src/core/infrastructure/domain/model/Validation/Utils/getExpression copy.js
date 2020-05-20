import moment from 'moment';

import { config } from 'conf';

const getOperatorEquivalence = (operatorType, operatorValue) => {
  const {
    validations: { operatorEquivalences }
  } = config;
  if (operatorEquivalences[operatorType]) {
    return operatorEquivalences[operatorType][operatorValue];
  }
  return operatorEquivalences.default[operatorValue];
};

export const getExpression = expression => {
  const { operatorType, operatorValue, expressionValue } = expression;
  const {
    validations: { nonNumericOperators }
  } = config;
  if (operatorType == 'LEN') {
    return {
      operator: getOperatorEquivalence(operatorType, operatorValue),
      arg1: {
        operator: 'LEN',
        arg1: 'VALUE'
      },
      arg2: Number(expressionValue)
    };
  }
  if (operatorType == 'date') {
    return {
      arg1: 'VALUE',
      operator: getOperatorEquivalence(operatorType, operatorValue),
      arg2: moment(expressionValue).format('YYYY-MM-DD')
    };
  }
  return {
    arg1: 'VALUE',
    operator: getOperatorEquivalence(operatorType, operatorValue),
    arg2: !nonNumericOperators.includes(operatorType) ? Number(expressionValue) : expressionValue
  };
};

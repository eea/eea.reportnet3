import moment from 'moment';

import { config } from 'conf';

const getOperatorEquivalence = (operatorType, operatorValue = null) => {
  const {
    validations: { operatorEquivalences }
  } = config;
  if (isNil(operatorValue)) {
    return operatorEquivalences[operatorType].type;
  } else {
    if (operatorEquivalences[operatorType]) {
      return operatorEquivalences[operatorType][operatorValue];
    }
  }
};

export const getExpression = expression => {
  const { operatorType, operatorValue, expressionValue } = expression;
  const {
    validations: { nonNumericOperators }
  } = config;
  if (operatorType === 'LEN') {
    return {
      operator: getOperatorEquivalence(operatorType, operatorValue),
      params: [
        {
          operator: getOperatorEquivalence(operatorType),
          params: ['VALUE', operatorValue]
        }
      ]
    };
  }
  if (operatorType === 'date') {
    return {
      operator: getOperatorEquivalence(operatorType, operatorValue),
      params: ['VALUE', moment(expressionValue).format('YYYY-MM-DD')]
    };
  }
  return {
    operator: getOperatorEquivalence(operatorType, operatorValue),
    params: ['VALUE', !nonNumericOperators.includes(operatorType) ? Number(expressionValue) : expressionValue]
  };
};

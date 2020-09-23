import isNil from 'lodash/isNil';
import moment from 'moment';

import { config } from 'conf';

import { getCreationDTO } from './getCreationDTO';

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

  if (expression.expressions.length > 1) {
    return getCreationDTO(expression.expressions);
  } else {
    if (operatorType === 'LEN') {
      return {
        operator: getOperatorEquivalence(operatorType, operatorValue),
        params: [
          {
            operator: getOperatorEquivalence(operatorType),
            params: ['VALUE']
          },
          Number(expressionValue)
        ]
      };
    }

    if (operatorType === 'date') {
      return {
        operator: getOperatorEquivalence(operatorType, operatorValue),
        params: ['VALUE', moment(expressionValue).format('YYYY-MM-DD')]
      };
    }

    if (getOperatorEquivalence(operatorType, operatorValue) === 'FIELD_NUM_MATCH') {
      return {
        operator: getOperatorEquivalence(operatorType, operatorValue),
        params: ['VALUE', expressionValue]
      };
    }

    return {
      operator: getOperatorEquivalence(operatorType, operatorValue),
      params: ['VALUE', !nonNumericOperators.includes(operatorType) ? Number(expressionValue) : expressionValue]
    };
  }
};

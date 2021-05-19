import dayjs from 'dayjs';
import isNil from 'lodash/isNil';

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

  const operatorEquivalence = getOperatorEquivalence(operatorType, operatorValue);

  if (expression.expressions.length > 1) {
    return getCreationDTO(expression.expressions);
  } else {
    if (operatorValue === 'IS NULL' || operatorValue === 'IS NOT NULL') {
      return { operator: operatorEquivalence, params: ['VALUE'] };
    }

    if (operatorType === 'LEN') {
      return {
        operator: operatorEquivalence,
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
        operator: operatorEquivalence,
        params: ['VALUE', dayjs(expressionValue).format('YYYY-MM-DD')]
      };
    }

    if (operatorType === 'dateTime') {
      return {
        operator: operatorEquivalence,
        params: ['VALUE', dayjs(expressionValue).format('YYYY-MM-DD HH:mm:ss')]
      };
    }

    if (operatorEquivalence === 'FIELD_NUM_MATCH') {
      return {
        operator: operatorEquivalence,
        params: ['VALUE', expressionValue]
      };
    }

    return {
      operator: operatorEquivalence,
      params: ['VALUE', !nonNumericOperators.includes(operatorType) ? Number(expressionValue) : expressionValue]
    };
  }
};

import isNil from 'lodash/isNil';

import { config } from 'conf';

import { getCreationDTO } from './getCreationDTO';

const getOperatorEquivalence = (operatorType, operatorValue = null) => {
  const {
    validations: { comparisonOperatorEquivalences }
  } = config;
  if (isNil(operatorValue)) {
    return comparisonOperatorEquivalences[operatorType].type;
  } else {
    if (comparisonOperatorEquivalences[operatorType]) {
      return comparisonOperatorEquivalences[operatorType][operatorValue];
    }
  }
};

export const getComparisonExpression = expression => {
  const { operatorType, operatorValue, field1, field2 } = expression;
  if (expression.expressions.length > 1) {
    return getCreationDTO(expression.expressions);
  } else {
    if (operatorType === 'LEN') {
      return {
        operator: getOperatorEquivalence(operatorType, operatorValue),
        params: [
          field1,
          {
            operator: getOperatorEquivalence(operatorType),
            params: [field2]
          }
        ]
      };
    }
    return {
      operator: getOperatorEquivalence(operatorType, operatorValue),
      params: [field1, field2]
    };
  }
};

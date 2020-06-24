import isNil from 'lodash/isNil';

import { config } from 'conf';

import { getCreationComparisonDTO } from './getCreationComparisonDTO';

const getOperatorEquivalence = (valueTypeSelector, operatorType, operatorValue = null) => {
  const {
    validations: { comparisonOperatorEquivalences, comparisonValuesOperatorEquivalences }
  } = config;
  if (isNil(operatorValue)) {
    if (valueTypeSelector === 'value') return comparisonValuesOperatorEquivalences[operatorType].type;
    return comparisonOperatorEquivalences[operatorType].type;
  } else {
    if (comparisonOperatorEquivalences[operatorType]) {
      if (valueTypeSelector === 'value') return comparisonValuesOperatorEquivalences[operatorType][operatorValue];
      return comparisonOperatorEquivalences[operatorType][operatorValue];
    }
  }
};

export const getComparisonExpression = expression => {
  const { operatorType, operatorValue, field1, field2, field1Type, valueTypeSelector, field2Type } = expression;
  if (expression.expressions.length > 1) {
    return getCreationComparisonDTO(expression.expressions);
  } else {
    if (
      (operatorType === 'year' || operatorType === 'month' || operatorType === 'day') &&
      field2Type === 'NUMBER_INTEGER'
    ) {
      return {
        operator: getOperatorEquivalence(valueTypeSelector, `${operatorType}Number`, operatorValue),
        params: [field1, field2]
      };
    }
    return {
      operator: getOperatorEquivalence(valueTypeSelector, operatorType, operatorValue),
      params: [field1, field2]
    };
  }
};

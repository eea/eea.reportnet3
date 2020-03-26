import { config } from 'conf';

import uuid from 'uuid';

import isNil from 'lodash/isNil';

import { getExpressionOperatorType } from './getExpressionOperatorType';

export const parseExpressionFromDTO = expression => {
  console.log('parseExpressions', expression);

  if (!isNil(expression) && expression.operator != 'AND' && expression.operator != 'OR') {
    const expression = {
      expressionId: uuid.v4(),
      group: false,
      union: '',
      operatorType: getExpressionOperatorType(expression.operator),
      operatorValue: {
        label: config.validations.reverseEquivalences[expression.operator],
        value: config.validations.reverseEquivalences[expression.operator]
      },
      expressionValue: expression.arg2,
      expressions: []
    };
    return {
      expressions: [expression],
      allExpressions: [expression]
    };
  }
  return {
    expressions: [],
    allExpressions: []
  };
};

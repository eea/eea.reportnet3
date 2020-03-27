import { config } from 'conf';

import uuid from 'uuid';

import { getExpressionOperatorType } from './getExpressionOperatorType';
import isNil from 'lodash/isNil';

export const getExpressionFromDTO = (expression, parentUnion) => {
  console.info('getExpressionFromDTO', expression);
  const union = !isNil(parentUnion) ? parentUnion : '';
  const newExpression = {
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
    expressions: [newExpression],
    allExpressions: [newExpression]
  };
};

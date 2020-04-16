import { config } from 'conf';

import uuid from 'uuid';

import { getExpressionOperatorType } from './getExpressionOperatorType';
import isNil from 'lodash/isNil';

export const getExpressionFromDTO = (expression, allExpressions, parentUnion) => {
  const union = !isNil(parentUnion) ? parentUnion : '';
  const newExpression = {};
  newExpression.expressionId = uuid.v4();
  newExpression.group = false;
  newExpression.union = union;
  newExpression.operatorValue = config.validations.reverseEquivalences[expression.operator];
  newExpression.expressionValue = expression.arg2;
  newExpression.expressions = [];
  if (!isNil(expression.arg1) && !isNil(expression.arg1.operator) && expression.arg1.operator == 'LEN') {
    newExpression.operatorType = 'LEN';
  } else {
    newExpression.operatorType = getExpressionOperatorType(expression.operator);
  }
  if (newExpression.operatorType == 'date') {
    newExpression.expressionValue = new Date(expression.arg2);
  }
  allExpressions.push(newExpression);

  return newExpression;
};

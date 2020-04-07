import { config } from 'conf';

import uuid from 'uuid';

import { getExpressionOperatorType } from './getExpressionOperatorType';
import isNil from 'lodash/isNil';

export const getExpressionFromDTO = (expression, allExpressions, parentUnion) => {
  console.log('[getExpressionFromDTO]', expression, allExpressions, parentUnion);
  const union = !isNil(parentUnion) ? parentUnion : '';
  const newExpression = {};
  newExpression.expressionId = uuid.v4();
  newExpression.group = false;
  newExpression.union = union;
  newExpression.operatorValue = config.validations.reverseEquivalences[expression.operator];
  newExpression.expressionValue = expression.arg2;
  newExpression.expressions = [];
  if (!isNil(expression.arg2) && !isNil(expression.arg2.operator) && expression.arg2.operator == 'LEN') {
    newExpression.operatorType = 'LEN';
    newExpression.expressionValue = expression.arg1;
  } else {
    newExpression.operatorType = getExpressionOperatorType(expression.operator);
    console.log('[newEpression]', newExpression);
  }
  if (newExpression.operatorType == 'date') {
    newExpression.expressionValue = new Date(expression.arg2);
  }
  allExpressions.push(newExpression);

  return newExpression;
};

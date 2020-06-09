import isNil from 'lodash/isNil';
import isObject from 'lodash/isObject';
import uuid from 'uuid';

import { config } from 'conf';

import { getExpressionOperatorType } from './getExpressionOperatorType';

export const getExpressionFromDTO = (expression, allExpressions, parentUnion) => {
  const union = !isNil(parentUnion) ? config.validations.reverseEquivalences[parentUnion] : '';
  const newExpression = {};
  newExpression.expressionId = uuid.v4();
  newExpression.group = false;
  newExpression.union = union;
  newExpression.operatorValue = config.validations.reverseEquivalences[expression.operator];
  newExpression.expressionValue = expression.params[1];
  newExpression.expressions = [];
  newExpression.operatorType = getExpressionOperatorType(expression.operator);
  if (isObject(expression.params[0])) {
    newExpression.operatorType = getExpressionOperatorType(expression.params[0].operator);
    newExpression.expressionValue = expression.params[0].params[1];
  }

  if (newExpression.operatorType === 'date') {
    newExpression.expressionValue = new Date(expression.params[1]);
  }
  allExpressions.push(newExpression);
  return newExpression;
};

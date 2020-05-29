import isNil from 'lodash/isNil';
import isObject from 'lodash/isObject';
import uuid from 'uuid';

import { config } from 'conf';

import { getExpressionOperatorType } from './getExpressionOperatorType';

export const getRowExpressionFromDTO = (expression, allExpressions, parentUnion) => {
  const union = !isNil(parentUnion) ? config.validations.reverseEquivalences[parentUnion] : '';
  const newExpression = {};
  newExpression.expressionId = uuid.v4();
  newExpression.group = false;
  newExpression.union = union;
  newExpression.operatorValue = config.validations.reverseEquivalences[expression.operator];
  newExpression.field1 = expression.params[0];
  newExpression.field2 = expression.params[1];
  newExpression.expressions = [];
  newExpression.operatorType = getExpressionOperatorType(expression.operator, 'row');
  if (isObject(expression.params[0])) {
    newExpression.operatorType = getExpressionOperatorType(expression.params[0].operator, 'row');
    newExpression.field1 = expression.params[0];
    newExpression.field2 = expression.params[1].params[0];
  }
  allExpressions.push(newExpression);

  return newExpression;
};

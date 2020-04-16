import isNil from 'lodash/isNil';
import uuid from 'uuid';

import { selectorFromDTO } from './selectorFromDTO';

export const getGroupFromDTO = (expression, allExpressions, parentOperator) => {
  const union = !isNil(parentOperator) ? parentOperator : '';
  const newExpression = {};
  newExpression.expressionId = uuid.v4();
  newExpression.group = false;
  newExpression.union = union;
  newExpression.operator = '';
  newExpression.operatorValue = '';
  newExpression.expressionValue = '';
  //expressions
  const subExpressions = [];
  selectorFromDTO(expression.arg1, subExpressions, allExpressions);
  selectorFromDTO(expression.arg2, subExpressions, allExpressions, expression.operator);
  newExpression.expressions = subExpressions;
  allExpressions.push(newExpression);

  return newExpression;
};

import isNil from 'lodash/isNil';
import uniqueId from 'lodash/uniqueId';

import { config } from 'conf';

import { selectorFromDTO } from './selectorFromDTO';

export const getGroupFromDTO = (expression, allExpressions, parentOperator) => {
  const {
    validations: { reverseEquivalences }
  } = config;
  const union = !isNil(parentOperator) ? reverseEquivalences[parentOperator] : '';

  const newExpression = {};
  newExpression.expressionId = uniqueId;
  newExpression.group = false;
  newExpression.union = union;
  newExpression.operator = '';
  newExpression.operatorValue = '';
  newExpression.expressionValue = '';
  const subExpressions = [];

  selectorFromDTO({ operator: expression.operator, params: expression.params }, subExpressions, allExpressions);
  newExpression.expressions = subExpressions;
  allExpressions.push(newExpression);
  return newExpression;
};

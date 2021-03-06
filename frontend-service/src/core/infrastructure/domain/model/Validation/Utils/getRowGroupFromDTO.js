import isNil from 'lodash/isNil';
import uuid from 'uuid';

import { config } from 'conf';

import { selectorRowFromDTO } from './selectorRowFromDTO';

export const getRowGroupFromDTO = (expression, allExpressions, parentOperator) => {
  const {
    validations: { reverseEquivalences }
  } = config;
  const union = !isNil(parentOperator) ? reverseEquivalences[parentOperator] : '';

  const newExpression = {};
  newExpression.expressionId = uuid.v4();
  newExpression.group = false;
  newExpression.union = union;
  newExpression.operator = '';
  newExpression.operatorValue = '';
  newExpression.expressionValue = '';
  const subExpressions = [];

  selectorRowFromDTO({ operator: expression.operator, params: expression.params }, subExpressions, allExpressions);
  newExpression.expressions = subExpressions;
  allExpressions.push(newExpression);

  return newExpression;
};

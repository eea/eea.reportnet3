import isNil from 'lodash/isNil';
import isObject from 'lodash/isObject';
import uuid from 'uuid';

import { config } from 'conf';

import { getExpressionOperatorType } from './getExpressionOperatorType';

const getValueTypeSelector = operator => {
  const operatorsChunks = operator.split('_');
  const [thirdToLastChunk, secondToLastChunk, lastChunk] = operatorsChunks.slice(-3);
  if (
    lastChunk === 'RECORD' ||
    secondToLastChunk === 'RECORD' ||
    (thirdToLastChunk === 'RECORD' && operatorsChunks.length > 3)
  ) {
    return 'field';
  }
  return 'value';
};

export const getRowExpressionFromDTO = (expression, allExpressions, parentUnion) => {
  const union = !isNil(parentUnion) ? config.validations.reverseEquivalences[parentUnion] : '';
  const newExpression = {};
  newExpression.expressionId = uuid.v4();
  newExpression.group = false;
  newExpression.union = union;
  newExpression.operatorValue = config.validations.reverseEquivalences[expression.operator];
  newExpression.field1 = expression.params[0];
  newExpression.expressions = [];
  newExpression.valueTypeSelector = getValueTypeSelector(expression.operator);

  if (isObject(expression.params[1])) {
    newExpression.operatorType = getExpressionOperatorType(expression.params[1].operator, 'row');
    if (
      newExpression.operatorType === 'date' ||
      (newExpression.operatorType === 'dateTime' && newExpression.valueTypeSelector === 'value')
    ) {
      newExpression.field2 = new Date(expression.params[1].params[0]);
    } else {
      newExpression.field2 = expression.params[1].params[0];
    }
  } else {
    newExpression.operatorType = getExpressionOperatorType(expression.operator, 'row');
    if (
      newExpression.operatorType === 'date' ||
      (newExpression.operatorType === 'dateTime' && newExpression.valueTypeSelector === 'value')
    ) {
      newExpression.field2 = new Date(expression.params[1]);
    } else {
      newExpression.field2 = expression.params[1];
    }
  }
  allExpressions.push(newExpression);

  return newExpression;
};

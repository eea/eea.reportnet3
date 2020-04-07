import isNil from 'lodash/isNil';
import isEmpty from 'lodash/isEmpty';

import { getExpressionFromDTO } from './getExpressionFromDTO';
import { getGroupFromDTO } from './getGroupFromDTO';

const operatorIsAUnion = operator => {
  return operator == 'AND' || operator == 'OR';
};

export const selectorFromDTO = (expression, expressions, allExpressions, parentOperator = null) => {
  if (!operatorIsAUnion(expression.operator)) {
    expressions.push(getExpressionFromDTO(expression, allExpressions, parentOperator));
  } else {
    if (
      !isNil(expression.arg1.operator) &&
      !isNil(expression.arg2.operator) &&
      operatorIsAUnion(parentOperator) &&
      !operatorIsAUnion(expression.arg1.operator) &&
      !operatorIsAUnion(expression.arg2.operator)
    ) {
      expressions.push(getGroupFromDTO(expression, allExpressions, parentOperator));
    } else {
      if (operatorIsAUnion(expression.arg1.operator)) {
        expressions.push(getGroupFromDTO(expression.arg1, allExpressions, parentOperator));
      } else {
        selectorFromDTO(expression.arg1, expressions, allExpressions, parentOperator);
      }
      selectorFromDTO(expression.arg2, expressions, allExpressions, expression.operator);
    }
  }
};

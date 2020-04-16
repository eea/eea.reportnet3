import isNil from 'lodash/isNil';
import isEmpty from 'lodash/isEmpty';

import { getExpressionFromDTO } from './getExpressionFromDTO';
import { getGroupFromDTO } from './getGroupFromDTO';

const isUnion = operator => {
  return operator == 'AND' || operator == 'OR';
};
const isGroupOperator = operator => {
  return operator == 'OR';
};

export const selectorFromDTO = (expression, expressions, allExpressions, parentOperator = null) => {
  if (!isUnion(expression.operator)) {
    expressions.push(getExpressionFromDTO(expression, allExpressions, parentOperator));
  } else {
    if (
      !isNil(expression.arg1.operator) &&
      !isNil(expression.arg2.operator) &&
      isUnion(parentOperator) &&
      expression.operator != parentOperator &&
      !isUnion(expression.arg1.operator) &&
      isUnion(expression.arg2.operator)
    ) {
      selectorFromDTO(expression.arg1, expressions, allExpressions, parentOperator);
      expressions.push(getGroupFromDTO(expression.arg2, allExpressions, expression.operator));
    } else if (
      !isNil(expression.arg1.operator) &&
      !isNil(expression.arg2.operator) &&
      isUnion(expression.arg1.operator) &&
      isUnion(expression.arg2.operator) &&
      expression.operator != expression.arg2.operator
    ) {
      expressions.push(getGroupFromDTO(expression.arg1, allExpressions, parentOperator));
      expressions.push(getGroupFromDTO(expression.arg2, allExpressions, expression.operator));
    } else if (
      !isNil(expression.arg1.operator) &&
      !isNil(expression.arg2.operator) &&
      isUnion(expression.arg1.operator)
    ) {
      expressions.push(getGroupFromDTO(expression.arg1, allExpressions, parentOperator));
      selectorFromDTO(expression.arg2, expressions, allExpressions, expression.operator);
    } else {
      selectorFromDTO(expression.arg1, expressions, allExpressions, parentOperator);
      selectorFromDTO(expression.arg2, expressions, allExpressions, expression.operator);
    }
  }
};

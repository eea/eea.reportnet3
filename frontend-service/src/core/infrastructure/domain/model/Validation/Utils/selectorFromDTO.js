import isNil from 'lodash/isNil';
import isEmpty from 'lodash/isEmpty';

import { getExpressionFromDTO } from './getExpressionFromDTO';
import { parseExpressionFromDTO } from './parseExpressionFromDTO';

const operatorIsAUnion = operator => {
  return operator == 'AND' || operator == 'OR';
};

export const selectorFromDTO = (expression, expressions, allExpressions, parentOperator = null) => {
  console.log('[selectorFromDTO]', expression, parentOperator);
  if (isNil(parentOperator)) {
    console.log('no parent operator');
    if (!operatorIsAUnion(expression.operator)) {
      const union = !isNil(parentOperator) ? parentOperator : null;
      console.log('union', union);

      expressions.push(getExpressionFromDTO(expression, allExpressions, null));
    } else {
      selectorFromDTO(expression.arg1, expressions, allExpressions, null);
      selectorFromDTO(expression.arg2, expressions, allExpressions, expression.operator);
    }
  } else {
    if (!operatorIsAUnion(expression.operator)) {
      expressions.push(getExpressionFromDTO(expression, allExpressions, parentOperator));
    } else {
      selectorFromDTO(expression.arg1, expressions, allExpressions, parentOperator);
      selectorFromDTO(expression.arg2, expressions, allExpressions, expression.operator);
    }
  }
};

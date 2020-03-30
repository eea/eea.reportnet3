import isNil from 'lodash/isNil';

import { getExpressionFromDTO } from './getExpressionFromDTO';

export const parseExpressionFromDTO = (expression, expressionOperator = null) => {
  const expressions = [];
  const allExpressions = [];

  if (!isNil(expression)) {
    if (expression.operator != 'AND' && expression.operator != 'OR') {
      const newExpression = getExpressionFromDTO(expression, expressionOperator);
      allExpressions.push(getExpressionFromDTO(newExpression));
      return newExpression;
    } else {
      expressions.push(parseExpressionFromDTO(expression.arg1));
      expressions.push(parseExpressionFromDTO(expression.arg2, expression.operator));
    }
  }
  return {
    expressions,
    allExpressions
  };
};

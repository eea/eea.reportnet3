import isNil from 'lodash/isNil';
import isEmpty from 'lodash/isEmpty';

import { getExpressionFromDTO } from './getExpressionFromDTO';

export const parseExpressionFromDTO = (expression, expressionOperator = null) => {
  const expressions = [];
  const allExpressions = [];

  if (!isNil(expression)) {
    if (expression.operator != 'AND' && expression.operator != 'OR') {
      const newExpression = getExpressionFromDTO(expression, expressionOperator);
      allExpressions.push(newExpression);
    } else {
      expressions.push(parseExpressionFromDTO(expression.arg1));
      expressions.push(parseExpressionFromDTO(expression.arg2, expression.operator));
    }
  }
  if (isEmpty(expressions)) {
    const [newExpresion] = allExpressions;
    expressions.push(newExpresion);
  }
  return {
    expressions,
    allExpressions
  };
};

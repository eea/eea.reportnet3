import { getExpression } from './getExpression';
import { getExpressionsNode } from './getExpressionsNode';

export const getCreationDTO = (expression, index, expressions) => {
  if (expressions.length > 1) {
    return getExpressionsNode(expression, index, expressions);
  }
  if (expression.expressions.length > 1) {
    return getExpressionsNode(expression.expressions[0], 0, expression.expressions);
  }
  return getExpression(expression);
};

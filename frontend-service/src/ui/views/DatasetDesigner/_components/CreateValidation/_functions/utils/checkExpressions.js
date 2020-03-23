import isNil from 'lodash/isNil';
import isEmpty from 'lodash/isEmpty';
import last from 'lodash/last';

export const checkExpressions = expressions => {
  if (!isNil(expressions) && expressions.length > 0) {
    const lastExpression = last(expressions);
    if (lastExpression.expressions && lastExpression.expressions.length > 0) {
      return false;
    } else if (expressions.length > 1) {
      const deactivate =
        isEmpty(lastExpression.union) ||
        isEmpty(lastExpression.operatorType) ||
        isEmpty(lastExpression.operatorValue) ||
        isEmpty(lastExpression.expressionValue);
      return deactivate;
    } else {
      const deactivate =
        isEmpty(lastExpression.operatorType) ||
        isEmpty(lastExpression.operatorValue) ||
        isEmpty(lastExpression.expressionValue);
      return deactivate;
    }
  }
  return true;
};

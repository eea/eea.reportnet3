import isNil from 'lodash/isNil';
import isEmpty from 'lodash/isEmpty';
import last from 'lodash/last';

const checkLastExpressionWithUnion = expression => {
  return (
    isEmpty(expression.union) ||
    isEmpty(expression.field1) ||
    isEmpty(expression.operatorType) ||
    isEmpty(expression.operatorValue) ||
    isEmpty(expression.field2)
  );
};

export const checkComparisonExpressions = expressions => {
  if (!isNil(expressions) && expressions.length > 0) {
    const lastExpression = last(expressions);
    if (lastExpression.expressions && lastExpression.expressions.length > 0) {
      const lastSubExpression = last(lastExpression.expressions);
      return checkLastExpressionWithUnion(lastSubExpression);
    }
    if (expressions.length > 1) {
      return checkLastExpressionWithUnion(lastExpression);
    }
    return (
      isEmpty(lastExpression.field1) ||
      isEmpty(lastExpression.operatorType) ||
      isEmpty(lastExpression.operatorValue) ||
      isEmpty(lastExpression.field2)
    );
  }
  return true;
};

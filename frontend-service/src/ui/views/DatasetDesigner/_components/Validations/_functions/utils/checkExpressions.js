import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import last from 'lodash/last';

const checkLastExpressionWithUnion = expression => {
  if (expression.operatorValue === 'IS NULL' || expression.operatorValue === 'IS NOT NULL') {
    return isEmpty(expression.union) || isEmpty(expression.operatorType) || isEmpty(expression.operatorValue);
  }

  return (
    isEmpty(expression.union) ||
    isEmpty(expression.operatorType) ||
    isEmpty(expression.operatorValue) ||
    isEmpty(String(expression.expressionValue))
  );
};

export const checkExpressions = expressions => {
  if (!isNil(expressions) && expressions.length > 0) {
    const lastExpression = last(expressions);

    if (lastExpression.expressions && lastExpression.expressions.length > 0) {
      const lastSubExpression = last(lastExpression.expressions);
      return checkLastExpressionWithUnion(lastSubExpression);
    }

    if (expressions.length > 1) {
      return checkLastExpressionWithUnion(lastExpression);
    }

    if (expressions[0].operatorValue === 'IS NULL' || expressions[0].operatorValue === 'IS NOT NULL') {
      return isEmpty(expressions[0].operatorType) || isEmpty(expressions[0].operatorValue);
    }

    return (
      isEmpty(lastExpression.operatorType) ||
      isEmpty(lastExpression.operatorValue) ||
      isEmpty(String(lastExpression.expressionValue))
    );
  }
  return true;
};

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import last from 'lodash/last';

const checkLastExpressionWithUnion = expression => {
  const { valueTypeSelector, field2 } = expression;
  let cField2 = field2;
  if (valueTypeSelector === 'value') cField2 = field2.toString();
  return (
    isEmpty(expression.union) ||
    isEmpty(expression.field1) ||
    isEmpty(expression.operatorType) ||
    isEmpty(expression.operatorValue) ||
    isEmpty(cField2)
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
    const { valueTypeSelector, field2 } = lastExpression;
    let cField2 = field2;
    if (valueTypeSelector === 'value') cField2 = field2.toString();
    return (
      isEmpty(lastExpression.field1) ||
      isEmpty(lastExpression.operatorType) ||
      isEmpty(lastExpression.operatorValue) ||
      isEmpty(cField2)
    );
  }
  return true;
};

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import last from 'lodash/last';

const checkLastExpressionWithUnion = expression => {
  const { field2, operatorValue, valueTypeSelector } = expression;
  let cField2 = field2;

  if (valueTypeSelector === 'value' && field2) cField2 = field2.toString();

  if (operatorValue === 'IS NULL' || operatorValue === 'IS NOT NULL') {
    return isEmpty(expression.field1) || isEmpty(expression.operatorType) || isEmpty(expression.operatorValue);
  }

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

    const { field2, operatorValue, valueTypeSelector } = lastExpression;
    let cField2 = field2;

    if (valueTypeSelector === 'value') cField2 = !isNil(field2) ? field2.toString() : '';

    if (operatorValue === 'IS NULL' || operatorValue === 'IS NOT NULL') {
      return (
        isEmpty(lastExpression.field1) || isEmpty(lastExpression.operatorType) || isEmpty(lastExpression.operatorValue)
      );
    }

    return (
      isEmpty(lastExpression.field1) ||
      isEmpty(lastExpression.operatorType) ||
      isEmpty(lastExpression.operatorValue) ||
      isEmpty(cField2)
    );
  }
  return true;
};

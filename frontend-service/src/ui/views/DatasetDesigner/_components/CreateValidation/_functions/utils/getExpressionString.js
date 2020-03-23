import { config } from 'conf';

import camelCase from 'lodash/camelCase';
import isNil from 'lodash/isNil';
import isEmpty from 'lodash/isEmpty';

const printExpression = (expression, field) => {
  if (expression.operatorType == 'LEN') {
    return `${camelCase(field)} ${config.validations.operatorEquivalences[expression.operatorValue]} ${parseInt(
      expression.expressionValue
    )}`;
    return {
      operator: config.validations.operatorEquivalences[expression.operatorValue],
      arg1: parseInt(expression.expressionValue),
      arg2: {
        operator: 'LEN',
        arg1: 'VALUE'
      }
    };
  }
  return {
    arg1: 'VALUE',
    operator: config.validations.operatorEquivalences[expression.operatorValue],
    arg2: !config.validations.nonNumericOperators.includes(expression.operatorType)
      ? parseInt(expression.expressionValue)
      : expression.expressionValue
  };
};
const printNode = (expression, index, expressions, field) => {
  return {
    arg1: printSelector(expression, 0, []),
    operator: expressions[index + 1].union,
    arg2:
      index + 1 < expressions.length - 1
        ? printSelector(expressions[index + 1], index + 1, expressions)
        : printSelector(expressions[index + 1], 0, [])
  };
};

const printSelector = (expression, index, expressions, field) => {
  if (expressions.length > 1) {
    return printNode(expression, index, expressions);
  }
  if (expression.expressions.length > 1) {
    return printNode(expression.expressions[0], 0, expression.expressions);
  }
  return printExpression(expression);
};

export const getExpressionString = (expressions, field) => {
  let expressionString = '';
  if (!isNil(field) && expressions.length > 0) {
    const { label: fieldLabel } = field;
    expressionString = printSelector(expressions[0], 0, expressions, field);
  }
  return '';
};

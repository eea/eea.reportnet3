import { config } from 'conf';

import camelCase from 'lodash/camelCase';
import isNil from 'lodash/isNil';
import isEmpty from 'lodash/isEmpty';

const printExpression = (expression, field) => {
  if (!isNil(expression.operatorValue) && !isEmpty(expression.operatorValue)) {
    if (expression.operatorType == 'LEN') {
      return `${field}.length ${config.validations.operatorEquivalences[expression.operatorValue]} ${
        expression.expressionValue
      }`;
    }
    return `${field} ${config.validations.operatorEquivalences[expression.operatorValue]} ${
      expression.expressionValue
    }`;
  }
  return '';
};
const printNode = (expression, index, expressions, field) => {
  let expressionString = '';
  expressionString = `${printSelector(expression, 0, [], field)} ${
    !isNil(expressions[index + 1].union) ? expressions[index + 1].union : ''
  }`;
  if (expressions.length - 1 > index + 1) {
    expressionString = `${expressionString} ${printSelector(expressions[index + 1], index + 1, expressions, field)}`;
  } else {
    expressionString = `${expressionString} ${printSelector(expressions[index + 1], 0, [], field)}`;
  }
  return expressionString;
};

const printSelector = (expression, index, expressions, field) => {
  if (expressions.length > 1) {
    return printNode(expression, index, expressions, field);
  }
  if (expression.expressions.length > 1) {
    return `( ${printNode(expression.expressions[0], 0, expression.expressions, field)} )`;
  }
  return printExpression(expression, field);
};

export const getExpressionString = (expressions, field) => {
  let expressionString = '';
  if (!isNil(field) && expressions.length > 0) {
    const { label: fieldLabel } = field;
    expressionString = printSelector(expressions[0], 0, expressions, camelCase(field.label));
  }
  return expressionString;
};

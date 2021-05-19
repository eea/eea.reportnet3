import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import dayjs from 'dayjs';

const printExpression = (expression, field) => {
  if (!isNil(expression.operatorValue) && !isEmpty(expression.operatorValue)) {
    if (expression.operatorType === 'LEN') {
      return `( LEN( ${field} ) ${expression.operatorValue} ${expression.expressionValue} )`;
    }

    if (expression.operatorType === 'date') {
      return `( ${field} ${expression.operatorValue} ${dayjs(expression.expressionValue).format('YYYY-MM-DD')} )`;
    }
    if (expression.operatorType === 'dateTime') {
      return `( ${field} ${expression.operatorValue} ${dayjs(expression.expressionValue).format(
        'YYYY-MM-DD HH:mm:ss'
      )} )`;
    }

    if (expression.operatorValue === 'IS NULL' || expression.operatorValue === 'IS NOT NULL') {
      return `( ${field} ${expression.operatorValue} )`;
    }

    return `( ${field} ${expression.operatorValue} ${expression.expressionValue} )`;
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

export const getFieldExpressionString = (expressions, field) => {
  let expressionString = '';

  if (!isNil(field) && expressions.length > 0) {
    expressionString = printSelector(expressions[0], 0, expressions, field.label);
  }
  return expressionString;
};

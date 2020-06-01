import { config } from 'conf';

import camelCase from 'lodash/camelCase';
import isNil from 'lodash/isNil';
import isEmpty from 'lodash/isEmpty';
import moment from 'moment';

const printExpression = expression => {
  if (!isNil(expression.operatorValue) && !isEmpty(expression.operatorValue)) {
    if (expression.operatorType === 'LEN') {
      return `( LEN( ${expression.field1.label} ) ${expression.operatorValue} ${expression.field2.label} )`;
    }
    // if (expression.operatorType === 'date') {
    //   return `( ${field} ${expression.operatorValue} ${moment(expression.expressionValue).format('YYYY-MM-DD')} )`;
    // }
    return `( ${expression.field1.label} ${expression.operatorValue} ${expression.field2.label} )`;
  }
  return '';
};
const printNode = (expression, index, expressions) => {
  let expressionString = '';
  expressionString = `${printSelector(expression, 0, [])} ${
    !isNil(expressions[index + 1].union) ? expressions[index + 1].union : ''
  }`;
  if (expressions.length - 1 > index + 1) {
    expressionString = `${expressionString} ${printSelector(expressions[index + 1], index + 1, expressions)}`;
  } else {
    expressionString = `${expressionString} ${printSelector(expressions[index + 1], 0, [])}`;
  }
  return expressionString;
};

const printSelector = (expression, index, expressions) => {
  if (expressions.length > 1) {
    return printNode(expression, index, expressions);
  }
  if (expression.expressions.length > 1) {
    return `( ${printNode(expression.expressions[0], 0, expression.expressions)} )`;
  }
  return printExpression(expression);
};

export const getComparisonsExpressionString = (expressions, field) => {
  let expressionString = '';
  if (!isNil(field) && expressions.length > 0) {
    const { label: fieldLabel } = field;
    expressionString = printSelector(expressions[0], 0, expressions);
  }
  return expressionString;
};

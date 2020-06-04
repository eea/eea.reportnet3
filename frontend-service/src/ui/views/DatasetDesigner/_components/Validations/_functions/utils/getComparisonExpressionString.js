import isNil from 'lodash/isNil';
import isEmpty from 'lodash/isEmpty';
import { getSelectedFieldById } from './getSelectedFieldById';

const printExpression = (expression, tabs) => {
  if (!isNil(expression.operatorValue) && !isEmpty(expression.operatorValue) && !isEmpty(expression.field2)) {
    if (expression.operatorType === 'LEN') {
      return `( LEN( ${getSelectedFieldById(expression.field1, tabs).label} ) ${expression.operatorValue} ${
        getSelectedFieldById(expression.field2, tabs).label
      } )`;
    }
    return `( ${getSelectedFieldById(expression.field1, tabs).label} ${expression.operatorValue} ${
      getSelectedFieldById(expression.field2, tabs).label
    } )`;
  }
  return '';
};
const printNode = (expression, index, expressions, tabs) => {
  let expressionString = '';
  expressionString = `${printSelector(expression, 0, [], tabs)} ${
    !isNil(expressions[index + 1].union) ? expressions[index + 1].union : ''
  }`;
  if (expressions.length - 1 > index + 1) {
    expressionString = `${expressionString} ${printSelector(expressions[index + 1], index + 1, expressions, tabs)}`;
  } else {
    expressionString = `${expressionString} ${printSelector(expressions[index + 1], 0, [], tabs)}`;
  }
  return expressionString;
};

const printSelector = (expression, index, expressions, tabs) => {
  if (expressions.length > 1) {
    return printNode(expression, index, expressions, tabs);
  }
  if (expression.expressions.length > 1) {
    return `( ${printNode(expression.expressions[0], 0, expression.expressions, tabs)} )`;
  }
  return printExpression(expression, tabs);
};

export const getComparisonExpressionString = (expressions, tabs) => {
  let expressionString = '';
  if (expressions.length > 0) {
    expressionString = printSelector(expressions[0], 0, expressions, tabs);
  }
  return expressionString;
};

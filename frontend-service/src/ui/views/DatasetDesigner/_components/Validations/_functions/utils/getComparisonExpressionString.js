import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import dayjs from 'dayjs';

import { getSelectedFieldById } from './getSelectedFieldById';

const printExpression = (expression, tabs) => {
  if (
    !isNil(expression.operatorValue) &&
    !isEmpty(expression.operatorValue) &&
    !isNil(expression.field2) &&
    expression.field2 !== ''
  ) {
    if (expression.operatorType === 'LEN') {
      if (expression.valueTypeSelector !== 'value') {
        return `( LEN( ${getSelectedFieldById(expression.field1, tabs).label} ) ${expression.operatorValue} ${
          getSelectedFieldById(expression.field2, tabs).label
        } )`;
      }
      return `( LEN( ${getSelectedFieldById(expression.field1, tabs).label} ) ${expression.operatorValue} ${
        expression.field2
      } )`;
    }

    if (expression.valueTypeSelector !== 'value') {
      return `( ${getSelectedFieldById(expression.field1, tabs).label} ${expression.operatorValue} ${
        getSelectedFieldById(expression.field2, tabs).label
      } )`;
    }

    let field2Value = expression.field2;
    if (expression.operatorType === 'date') {
      field2Value = dayjs(expression.field2).format('YYYY-MM-DD');
    } else if (expression.operatorType === 'dateTime') {
      field2Value = dayjs(expression.field2).format('YYYY-MM-DD HH:mm:ss');
    }

    return `( ${getSelectedFieldById(expression.field1, tabs).label} ${expression.operatorValue} ${field2Value} )`;
  } else if (
    !isNil(expression.operatorValue) &&
    !isEmpty(expression.operatorValue) &&
    (expression.operatorValue === 'IS NULL' || expression.operatorValue === 'IS NOT NULL')
  ) {
    return `( ${getSelectedFieldById(expression.field1, tabs).label} ${expression.operatorValue}  )`;
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
  if (expressions.length > 0) {
    return printSelector(expressions[0], 0, expressions, tabs);
  }
  return '';
};

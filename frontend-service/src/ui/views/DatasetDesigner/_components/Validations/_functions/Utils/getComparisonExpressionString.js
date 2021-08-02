import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import dayjs from 'dayjs';

import { getSelectedFieldById } from './getSelectedFieldById';

const getDateFormattedField = (field, format) => {
  return dayjs(field).format(format);
};
const getPrefixedExpression = ({ expression, tabs, prefix, expressionEnd }) => {
  return `( ${prefix}( ${getSelectedFieldById(expression.field1, tabs).label} ) ${
    expression.operatorValue
  } ${expressionEnd} )`;
};

const getExpressionString = ({ expression, tabs, expressionEnd = '' }) => {
  return `( ${getSelectedFieldById(expression.field1, tabs).label} ${expression.operatorValue} ${expressionEnd} )`;
};

const printExpression = (expression, tabs) => {
  if (
    !isNil(expression.operatorValue) &&
    !isEmpty(expression.operatorValue) &&
    !isNil(expression.field2) &&
    expression.field2 !== ''
  ) {
    if (expression.valueTypeSelector === 'value') {
      const expressionParameters = {
        expression,
        tabs,
        expressionEnd: expression.field2
      };
      switch (expression.operatorType) {
        case 'LEN':
          return getPrefixedExpression({ ...expressionParameters, prefix: 'LEN' });
        case 'date':
          return getExpressionString({
            ...expressionParameters,
            expressionEnd: getDateFormattedField(expression.field2, 'YYYY-MM-DD')
          });
        case 'year':
          return getPrefixedExpression({ ...expressionParameters, prefix: 'Year' });
        case 'month':
          return getPrefixedExpression({ ...expressionParameters, prefix: 'Month' });
        case 'day':
          return getPrefixedExpression({ ...expressionParameters, prefix: 'Day' });
        case 'dateTime':
          return getExpressionString({
            ...expressionParameters,
            expressionEnd: getDateFormattedField(expression.field2, 'YYYY-MM-DD HH:mm:ss')
          });
        case 'yearDateTime':
          return getPrefixedExpression({ ...expressionParameters, prefix: 'Year' });
        case 'monthDateTime':
          return getPrefixedExpression({ ...expressionParameters, prefix: 'Month' });
        case 'dayDateTime':
          return getPrefixedExpression({ ...expressionParameters, prefix: 'Day' });
        default:
          return getExpressionString({ ...expressionParameters });
      }
    } else {
      const expressionParameters = {
        expression,
        tabs,
        expressionEnd: getSelectedFieldById(expression.field2, tabs).label
      };
      switch (expression.operatorType) {
        case 'LEN':
          return getPrefixedExpression({ ...expressionParameters, prefix: 'LEN' });
        case 'year':
          return getPrefixedExpression({ ...expressionParameters, prefix: 'Year' });
        case 'month':
          return getPrefixedExpression({ ...expressionParameters, prefix: 'Month' });
        case 'day':
          return getPrefixedExpression({ ...expressionParameters, prefix: 'Day' });
        case 'yearDateTime':
          return getPrefixedExpression({ ...expressionParameters, prefix: 'Year' });
        case 'monthDateTime':
          return getPrefixedExpression({ ...expressionParameters, prefix: 'Month' });
        case 'dayDateTime':
          return getPrefixedExpression({ ...expressionParameters, prefix: 'Day' });
        default:
          return getExpressionString({ ...expressionParameters });
      }
    }
  } else if (
    !isNil(expression.operatorValue) &&
    !isEmpty(expression.operatorValue) &&
    (expression.operatorValue === 'IS NULL' || expression.operatorValue === 'IS NOT NULL')
  ) {
    return getExpressionString({ expression, tabs });
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

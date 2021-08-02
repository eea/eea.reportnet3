import dayjs from 'dayjs';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

const getDateFormattedExpression = ({ expression, field, format }) => {
  return `( ${field} ${expression.operatorValue} ${dayjs(expression.expressionValue).format(format)} )`;
};
const getPrefixedFieldExpression = ({ expression, field, prefix }) => {
  return `( ${prefix}( ${field} ) ${expression.operatorValue} ${expression.expressionValue} )`;
};

const printExpression = (expression, field) => {
  if (!isNil(expression.operatorValue) && !isEmpty(expression.operatorValue)) {
    if (expression.operatorValue === 'IS NULL' || expression.operatorValue === 'IS NOT NULL') {
      return `( ${field} ${expression.operatorValue} )`;
    } else {
      switch (expression.operatorType) {
        case 'LEN':
          return getPrefixedFieldExpression({ expression, field, prefix: 'LEN' });
        case 'date':
          return getDateFormattedExpression({ expression, field, format: 'YYYY-MM-DD' });
        case 'year':
          return getPrefixedFieldExpression({ expression, field, prefix: 'Year' });
        case 'month':
          return getPrefixedFieldExpression({ expression, field, prefix: 'Month' });
        case 'day':
          return getPrefixedFieldExpression({ expression, field, prefix: 'Day' });
        case 'dateTime':
          return getDateFormattedExpression({ expression, field, format: 'YYYY-MM-DD HH:mm:ss' });
        case 'yearDateTime':
          return getPrefixedFieldExpression({ expression, field, prefix: 'Year' });
        case 'monthDateTime':
          return getPrefixedFieldExpression({ expression, field, prefix: 'Month' });
        case 'dayDateTime':
          return getPrefixedFieldExpression({ expression, field, prefix: 'Day' });
        default:
          return `( ${field} ${expression.operatorValue} ${expression.expressionValue} )`;
      }
    }
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

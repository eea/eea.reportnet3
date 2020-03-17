import isNil from 'lodash/isNil';

export const getExpressionString = (expressions, field) => {
  let expressionString = '';
  if (!isNil(field) && expressions.length > 0) {
    const { label: fieldLabel } = field;
    expressions.forEach((expression, i) => {
      const { union: unionValue, operatorValue: operator, expressionValue, expressions } = expression;
      if (!isNil(operator) && !isNil(expressionValue)) {
        const expressionLeft = `${fieldLabel} ${operator} ${expressionValue}`;
        if (i == 0) {
          expressionString = `${expressionString} ${expressionLeft}`;
        } else {
          if (!isNil(unionValue)) {
            expressionString = `${expressionString} ${unionValue} ${expressionLeft}`;
          }
        }
      }
    });
  }
  return expressionString;
};

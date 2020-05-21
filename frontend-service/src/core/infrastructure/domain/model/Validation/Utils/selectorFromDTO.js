import isNil from 'lodash/isNil';
import isEmpty from 'lodash/isEmpty';
import isObject from 'lodash/isObject';

import { getExpressionFromDTO } from './getExpressionFromDTO';
import { getGroupFromDTO } from './getGroupFromDTO';
import { getExpression } from './getExpression';

export const selectorFromDTO = (expression, expressions, allExpressions) => {
  console.log('selectorFromDTO: ', expression);
  //una expression simple
  if (!isObject(expression.params[0])) {
    expressions.push(getExpressionFromDTO(expression, allExpressions, null));
  } else if (isObject(expression.params[0]) && expression.params[0].operator === 'FIELD_LEN') {
    expressions.push(getExpressionFromDTO(expression, allExpressions, null));
  } else {
    expression.params.map(param => {
      expressions.push(getExpressionFromDTO(param, allExpressions, expression.operator));
    });
  }
};

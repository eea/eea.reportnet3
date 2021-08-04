import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';

import { getExpression } from './getExpression';

export const getCreationDTO = expressions => {
  if (!isEmpty(expressions)) {
    if (expressions.length > 1) {
      const params = [];
      let operator = '';

      expressions.forEach((expression, index) => {
        if (index === 0) {
          operator = expressions[index + 1].union;

          if (expression.expressions.length > 0) {
            params.push(getCreationDTO(expression.expressions));
          } else {
            params.push(getExpression(expression));
          }

          if (!isNil(expressions[index + 2])) {
            const nextExpressions = expressions.slice(index + 1);
            params.push(getCreationDTO(nextExpressions));
          } else {
            if (expressions[index + 1].expressions.length > 0) {
              params.push(getCreationDTO(expressions[index + 1].expressions));
            } else {
              params.push(getExpression(expressions[index + 1]));
            }
          }
        }
      });

      return {
        operator: config.validations.operatorEquivalences.logicalOperators[operator],
        params
      };
    }
  }
  const [expression] = expressions;
  return getExpression(expression);
};

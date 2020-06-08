import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniq from 'lodash/uniq';

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
          params.push(getExpression(expression));
          if (!isNil(expressions[index + 2])) {
            const nextExpressions = expressions.slice(index + 1);
            params.push(getCreationDTO(nextExpressions));
          } else {
            params.push(getExpression(expressions[index + 1]));
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

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';

import { getComparisonExpression } from './getComparisonExpression';

export const getCreationComparisonDTO = expressions => {
  if (!isEmpty(expressions)) {
    if (expressions.length > 1) {
      const params = [];
      let operator = '';

      expressions.forEach((expression, index) => {
        if (index === 0) {
          operator = expressions[index + 1].union;
          if (expression.expressions.length > 0) {
            params.push(getCreationComparisonDTO(expression.expressions));
          } else {
            params.push(getComparisonExpression(expression));
          }

          if (!isNil(expressions[index + 2])) {
            const nextExpressions = expressions.slice(index + 1);
            params.push(getCreationComparisonDTO(nextExpressions));
          } else {
            if (expressions[index + 1].expressions.length > 0) {
              params.push(getCreationComparisonDTO(expressions[index + 1].expressions));
            } else {
              params.push(getComparisonExpression(expressions[index + 1]));
            }
          }
        }
      });

      return {
        operator: config.validations.comparisonOperatorEquivalences.logicalOperators[operator],
        params
      };
    }
    const [expression] = expressions;
    return getComparisonExpression(expression);
  }
};

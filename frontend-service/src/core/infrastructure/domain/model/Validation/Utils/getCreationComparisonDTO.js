import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniq from 'lodash/uniq';

import { config } from 'conf';

import { getComparisonExpression } from './getComparisonExpression';

export const getCreationComparisonDTO = expressions => {
  if (!isEmpty(expressions)) {
    if (expressions.length > 1) {
      const unions = expressions.filter(expression => expression.union !== '').map(expression => expression.union);
      if (uniq(unions).length === 1) {
        const [union] = unions;
        return {
          operator: config.validations.comparisonOperatorEquivalences.logicalOperators[union],
          params: expressions.map(expression => getComparisonExpression(expression))
        };
      } else {
        const ORExpressions = expressions.filter(expression => expression.union === 'OR');
        const params = [];
        expressions.forEach((expression, index) => {
          if (!isNil(expressions[index + 1]) && ORExpressions.includes(expressions[index + 1])) {
            params.push({
              operator:
                config.validations.comparisonOperatorEquivalences.logicalOperators[expressions[index + 1].union],
              params: [getComparisonExpression(expression), getComparisonExpression(expressions[index + 1])]
            });
          } else if (
            !ORExpressions.includes(expression) &&
            !isNil(expressions[index + 1]) &&
            !ORExpressions.includes(expressions[index + 1])
          ) {
            params.push(getComparisonExpression(expression));
          }
        });
        return {
          operator: config.validations.comparisonOperatorEquivalences.logicalOperators['AND'],
          params
        };
      }
    }
    const [expression] = expressions;
    return getComparisonExpression(expression);
  }
};

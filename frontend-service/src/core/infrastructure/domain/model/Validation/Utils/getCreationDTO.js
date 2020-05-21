import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniq from 'lodash/uniq';

import { config } from 'conf';

import { getExpression } from './getExpression';

export const getCreationDTO = expressions => {
  if (!isEmpty(expressions)) {
    if (expressions.length > 1) {
      const unions = expressions.filter(expression => expression.union !== '').map(expression => expression.union);
      if (uniq(unions).length === 1) {
        const [union] = unions;
        return {
          operator: config.validations.operatorEquivalences.logicalOperators[union],
          params: expressions.map(expression => getExpression(expression))
        };
      } else {
        const ORExpressions = expressions.filter(expression => expression.union === 'OR');
        const params = [];
        expressions.forEach((expression, index) => {
          if (!isNil(expressions[index + 1]) && ORExpressions.includes(expressions[index + 1])) {
            params.push({
              operator: config.validations.operatorEquivalences.logicalOperators[expressions[index + 1].union],
              params: [getExpression(expression), getExpression(expressions[index + 1])]
            });
          } else if (
            !ORExpressions.includes(expression) &&
            !isNil(expressions[index + 1]) &&
            !ORExpressions.includes(expressions[index + 1])
          ) {
            params.push(getExpression(expression));
          }
        });
        return {
          operator: config.validations.operatorEquivalences.logicalOperators['AND'],
          params
        };
      }
    }
    const [expression] = expressions;
    return getExpression(expression);
  }
};

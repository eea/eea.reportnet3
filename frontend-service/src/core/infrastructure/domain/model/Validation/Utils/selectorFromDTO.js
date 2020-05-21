import isObject from 'lodash/isObject';

import { config } from 'conf';

import { getExpressionFromDTO } from './getExpressionFromDTO';
import { getGroupFromDTO } from './getGroupFromDTO';

export const selectorFromDTO = (expression, expressions, allExpressions) => {
  const {
    validations: { logicalOperatorFromDTO }
  } = config;
  if (!isObject(expression.params[0])) {
    expressions.push(getExpressionFromDTO(expression, allExpressions, null));
  } else if (isObject(expression.params[0]) && expression.params[0].operator === 'FIELD_LEN') {
    expressions.push(getExpressionFromDTO(expression, allExpressions, null));
  } else {
    expression.params.map((param, index) => {
      let operator = expression.operator;
      if (index === 0) {
        operator = null;
      }
      if (logicalOperatorFromDTO.includes(param.operator)) {
        expressions.push(getGroupFromDTO(param, allExpressions, expression.operator));
      } else {
        expressions.push(getExpressionFromDTO(param, allExpressions, operator));
      }
    });
  }
};

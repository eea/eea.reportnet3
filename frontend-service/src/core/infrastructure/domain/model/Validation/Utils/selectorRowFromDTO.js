import isObject from 'lodash/isObject';

import { config } from 'conf';

import { getRowExpressionFromDTO } from './getRowExpressionFromDTO';
import { getRowGroupFromDTO } from './getRowGroupFromDTO';

export const selectorRowFromDTO = (expression, expressions, allExpressions) => {
  const {
    validations: { logicalRowOperatorFromDTO }
  } = config;
  if (!isObject(expression.params[0])) {
    expressions.push(getRowExpressionFromDTO(expression, allExpressions, null));
  } else if (isObject(expression.params[0]) && expression.params[0].operator === 'RECORD_LEN') {
    expressions.push(getRowExpressionFromDTO(expression, allExpressions, null));
  } else {
    expression.params.map((param, index) => {
      let operator = expression.operator;
      if (index === 0) {
        operator = null;
      }
      if (logicalRowOperatorFromDTO.includes(param.operator)) {
        expressions.push(getRowGroupFromDTO(param, allExpressions, expression.operator));
      } else {
        expressions.push(getRowExpressionFromDTO(param, allExpressions, operator));
      }
    });
  }
};

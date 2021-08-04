import { config } from 'conf';

import { getRowExpressionFromDTO } from './getRowExpressionFromDTO';
import { getRowGroupFromDTO } from './getRowGroupFromDTO';

export const selectorRowFromDTO = (expression, expressions, allExpressions, parentOperator = null) => {
  const {
    validations: { logicalRowOperatorFromDTO }
  } = config;
  const [firstParam, secondParam] = expression.params;
  const { operator } = expression;

  if (logicalRowOperatorFromDTO.includes(operator)) {
    if (logicalRowOperatorFromDTO.includes(firstParam.operator)) {
      expressions.push(getRowGroupFromDTO(firstParam, allExpressions, parentOperator));
    } else {
      expressions.push(getRowExpressionFromDTO(firstParam, allExpressions, parentOperator));
    }

    if (logicalRowOperatorFromDTO.includes(secondParam.operator)) {
      selectorRowFromDTO(secondParam, expressions, allExpressions, operator);
    } else {
      expressions.push(getRowExpressionFromDTO(secondParam, allExpressions, operator));
    }
  } else {
    expressions.push(getRowExpressionFromDTO(expression, allExpressions, null));
  }
};

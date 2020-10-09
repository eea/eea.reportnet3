import { config } from 'conf';

import { getExpressionFromDTO } from './getExpressionFromDTO';
import { getGroupFromDTO } from './getGroupFromDTO';

export const selectorFromDTO = (expression, expressions, allExpressions, parentOperator = null) => {
  const {
    validations: { logicalOperatorFromDTO }
  } = config;
  const [firstParam, secondParam] = expression.params;
  const { operator } = expression;

  if (logicalOperatorFromDTO.includes(operator)) {
    if (logicalOperatorFromDTO.includes(firstParam.operator)) {
      expressions.push(getGroupFromDTO(firstParam, allExpressions, parentOperator));
    } else {
      expressions.push(getExpressionFromDTO(firstParam, allExpressions, parentOperator));
    }

    if (logicalOperatorFromDTO.includes(secondParam.operator)) {
      selectorFromDTO(secondParam, expressions, allExpressions, operator);
    } else {
      expressions.push(getExpressionFromDTO(secondParam, allExpressions, operator));
    }
  } else {
    expressions.push(getExpressionFromDTO(expression, allExpressions, null));
  }
};

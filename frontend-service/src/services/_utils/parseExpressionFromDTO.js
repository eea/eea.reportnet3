import isNil from 'lodash/isNil';

import { selectorFromDTO } from './selectorFromDTO';

export const parseExpressionFromDTO = expression => {
  const expressions = [];
  const allExpressions = [];

  if (!isNil(expression)) {
    selectorFromDTO(expression, expressions, allExpressions);
  }

  return {
    expressions,
    allExpressions
  };
};

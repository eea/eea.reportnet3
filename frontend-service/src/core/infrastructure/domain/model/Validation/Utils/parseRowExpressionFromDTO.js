import isNil from 'lodash/isNil';

import { selectorRowFromDTO } from './selectorRowFromDTO';

export const parseRowExpressionFromDTO = expression => {
  const expressions = [];
  const allExpressions = [];
  if (!isNil(expression)) {
    selectorRowFromDTO(expression, expressions, allExpressions);
  }
  return {
    expressions,
    allExpressions
  };
};

import isNil from 'lodash/isNil';

import { selectorFromDTO } from './selectorFromDTO';

export const parseExpressionFromDTO = expression => {
  const expressions = [];
  const allExpressions = [];

  //if arg1 is an object and its operator is and then is a group
  //if arg1 is an object but its operator is not an and is a expression
  // if arg 2 operator is an and
  //if arg2 operator is a and and arg1 is an object but its operator != and AND arg2 idem is a group
  // if arg2 has not a and is an expression
  if (!isNil(expression)) {
    selectorFromDTO(expression, expressions, allExpressions);
  }

  console.log('parseExpressionFromDTO', expressions, allExpressions);

  return {
    expressions,
    allExpressions
  };
};

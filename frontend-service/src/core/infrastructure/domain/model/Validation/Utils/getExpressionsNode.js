import { getCreationDTO } from './getCreationDTO';

export const getExpressionsNode = (expression, index, expressions) => {
  return {
    operator: expressions[index + 1].union,
    arg1: getCreationDTO(expression, 0, []),
    arg2:
      index + 1 < expressions.length - 1
        ? getCreationDTO(expressions[index + 1], index + 1, expressions)
        : getCreationDTO(expressions[index + 1], 0, [])
  };
};

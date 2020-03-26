import { getCreationDTO } from './getCreationDTO';

export const getExpressionsNode = (expression, index, expressions) => {
  return {
    arg1: getCreationDTO(expression, 0, []),
    operator: expressions[index + 1].union,
    arg2:
      index + 1 < expressions.length - 1
        ? getCreationDTO(expressions[index + 1], index + 1, expressions)
        : getCreationDTO(expressions[index + 1], 0, [])
  };
};

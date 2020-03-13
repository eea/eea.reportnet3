import pullAt from 'lodash/pullAt';

export const deleteExpresionRecursivily = (expresionId, expresionColection) => {
  expresionColection.forEach((expresion, i) => {
    if (expresion.expresionId == expresionId && expresionColection.length > 1) {
      pullAt(expresionColection, i);
    } else {
      if (expresion.expresions.length > 0) {
        deleteExpresionRecursivily(expresionId, expresion.expresions);
      }
    }
  });
  return expresionColection;
};

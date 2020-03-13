import isNil from 'lodash/isNil';
import isEmpty from 'lodash/isEmpty';
import last from 'lodash/last';

export const checkExpresions = expresions => {
  if (!isNil(expresions) && expresions.length > 0) {
    const lastExpresion = last(expresions);
    if (lastExpresion.expresions && lastExpresion.expresions.length > 0) {
      return false;
    } else if (expresions.length > 1) {
      const deactivate =
        isEmpty(lastExpresion.union) ||
        isEmpty(lastExpresion.operatorType) ||
        isEmpty(lastExpresion.operatorValue) ||
        isEmpty(lastExpresion.expresionValue);
      return deactivate;
    } else {
      const deactivate =
        isEmpty(lastExpresion.operatorType) ||
        isEmpty(lastExpresion.operatorValue) ||
        isEmpty(lastExpresion.expresionValue);
      return deactivate;
    }
  }
  return true;
};

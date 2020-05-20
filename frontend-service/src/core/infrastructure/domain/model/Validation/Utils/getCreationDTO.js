import isEmpty from 'lodash/isEmpty';

import { getExpression } from './getExpression';
import { getExpressionsNode } from './getExpressionsNode';

export const getCreationDTO = expressions => {
  if (!isEmpty(expressions)) {
    // comprobar que haya más de una
    if (expressions.length > 1) {
      // si todos los operadores lógicos son iguales
      //
    }
    // comprobar si solo hay una
  }
};

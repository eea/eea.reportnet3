import isNil from 'lodash/isNil';

export const getExpresionString = (expresions, field) => {
  let expresionString = '';
  if (!isNil(field) && expresions.length > 0) {
    const { label: fieldLabel } = field;
    expresions.forEach((expresion, i) => {
      const { union: unionValue, operatorValue: operator, expresionValue, expresions } = expresion;
      if (!isNil(operator) && !isNil(expresionValue)) {
        const expresionLeft = `${fieldLabel} ${operator} ${expresionValue}`;
        if (i == 0) {
          expresionString = `${expresionString} ${expresionLeft}`;
        } else {
          if (!isNil(unionValue)) {
            expresionString = `${expresionString} ${unionValue} ${expresionLeft}`;
          }
        }
      }
    });
  }
  return expresionString;
};

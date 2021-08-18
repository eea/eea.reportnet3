import uniqueId from 'lodash/uniqueId';

export const getEmptyExpression = () => {
  const expressionId = uniqueId();
  return {
    expressionId,
    group: false,
    union: '',
    operatorType: '',
    operatorValue: '',
    expressionValue: '',
    expressions: [],
    expressionsIf: [],
    expressionsThen: []
  };
};

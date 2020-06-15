import uuid from 'uuid';

export const getEmptyExpression = () => {
  const expressionId = uuid.v4();
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

import uuid from 'uuid';

export const getEmptyExpresion = () => {
  const expresionId = uuid.v4();
  return {
    expresionId,
    group: false,
    union: '',
    operatorType: '',
    operatorValue: '',
    expresionValue: '',
    expresions: []
  };
};

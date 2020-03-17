import { getEmptyExpression } from './getEmptyExpression';

export const resetValidationRuleCreation = () => {
  return {
    table: undefined,
    field: undefined,
    shortCode: '',
    description: '',
    errorMessage: '',
    errorLevel: undefined,
    active: false,
    expressions: [getEmptyExpression()]
  };
};

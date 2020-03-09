import { getEmptyExpresion } from './getEmptyExpresion';

export const resetValidationRuleCreation = () => {
  return {
    table: undefined,
    field: undefined,
    shortCode: '',
    description: '',
    errorMessage: '',
    errorLevel: undefined,
    active: false,
    expresions: [getEmptyExpresion()]
  };
};

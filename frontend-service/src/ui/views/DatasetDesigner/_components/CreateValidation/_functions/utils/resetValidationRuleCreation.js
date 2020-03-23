import { getEmptyExpression } from './getEmptyExpression';

export const resetValidationRuleCreation = () => {
  const newExpression = getEmptyExpression();
  return {
    table: undefined,
    field: undefined,
    shortCode: '',
    name: '',
    description: '',
    errorMessage: '',
    errorLevel: undefined,
    active: false,
    expressions: [newExpression],
    allExpressions: [newExpression]
  };
};

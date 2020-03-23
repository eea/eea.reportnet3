import { getEmptyExpression } from './getEmptyExpression';

export const resetValidationRuleCreation = () => {
  const newExpression = getEmptyExpression();
  return {
    table: undefined,
    field: undefined,
    shortCode: '',
    description: '',
    errorMessage: '',
    errorLevel: undefined,
    active: false,
    expressions: [newExpression],
    allExpressions: [newExpression]
  };
};

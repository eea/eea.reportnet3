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
    active: true,
    expressions: [newExpression],
    allExpressions: [newExpression]
  };
};

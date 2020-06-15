import { getEmptyExpression } from './getEmptyExpression';

export const resetValidationRuleCreation = () => {
  const newExpression = getEmptyExpression();
  const newExpressionIf = getEmptyExpression();
  const newExpressionThen = getEmptyExpression();
  return {
    active: true,
    allExpressions: [newExpression],
    allExpressionsIf: [newExpressionIf],
    allExpressionsThen: [newExpressionThen],
    description: '',
    errorLevel: undefined,
    errorMessage: '',
    expressions: [newExpression],
    expressionsIf: [newExpressionIf],
    expressionsThen: [newExpressionThen],
    field: undefined,
    name: '',
    shortCode: '',
    table: undefined
  };
};

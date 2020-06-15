import { getEmptyExpression } from './getEmptyExpression';

export const initValidationRuleCreation = rawTables => {
  const tables = rawTables
    .filter(table => !table.addTab)
    .map(table => {
      return { label: table.header, code: table.tableSchemaId };
    });
  const newExpression = getEmptyExpression();
  const newExpressionIf = getEmptyExpression();
  const newExpressionThen = getEmptyExpression();
  return {
    tables,
    candidateRule: {
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
    }
  };
};

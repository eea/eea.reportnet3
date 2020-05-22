import { getEmptyExpression } from './getEmptyExpression';

export const initValidationRuleCreation = rawTables => {
  const tables = rawTables
    .filter(table => !table.addTab)
    .map(table => {
      return { label: table.header, code: table.tableSchemaId };
    });
  const newExpression = getEmptyExpression();
  return {
    tables,
    candidateRule: {
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
    }
  };
};

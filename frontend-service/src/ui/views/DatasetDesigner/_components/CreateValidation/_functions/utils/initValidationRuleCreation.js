import { config } from 'conf/';

import { getEmptyExpression } from './getEmptyExpression';

export const initValidationRuleCreation = rawTables => {
  rawTables.pop();
  const tables = rawTables.map(table => {
    return { label: table.tableSchemaName, code: table.recordSchemaId };
  });

  const errorLevels = config.validations.errorLevels;
  const newExpression = getEmptyExpression();
  return {
    tables,
    errorLevels,
    candidateRule: {
      table: undefined,
      field: undefined,
      shortCode: '',
      description: '',
      errorMessage: '',
      errorLevel: undefined,
      active: false,
      expressions: [newExpression],
      allExpressions: [newExpression]
    }
  };
};

import { config } from 'conf/';

import { getEmptyExpression } from './getEmptyExpression';

export const initValidationRuleCreation = rawTables => {
  const tables = rawTables
    .filter(table => !table.addTab)
    .map(table => {
      return { label: table.header, code: table.recordSchemaId };
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

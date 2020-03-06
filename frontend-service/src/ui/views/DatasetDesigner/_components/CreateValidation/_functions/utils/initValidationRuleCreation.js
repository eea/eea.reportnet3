import { config } from 'conf/';

import { getEmptyExpresion } from './getEmptyExpresion';

export const initValidationRuleCreation = rawTables => {
  rawTables.pop();
  const tables = rawTables.map(table => {
    return { label: table.tableSchemaName, code: table.recordSchemaId };
  });

  const errorLevels = config.validations.errorLevels;
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
      expresions: [getEmptyExpresion()]
    }
  };
};

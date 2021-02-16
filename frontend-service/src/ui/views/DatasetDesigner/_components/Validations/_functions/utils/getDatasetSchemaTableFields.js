import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';

export const getDatasetSchemaTableFields = (candidateTable, tables) => {
  const [selectedTable] = tables.filter(table => table.tableSchemaId === candidateTable.code);
  const {
    validations: {
      bannedTypes: { sqlHelp, nonSql }
    }
  } = config;
  let bannedFields;

  if (!isNil(selectedTable) && !isEmpty(selectedTable) && !isNil(selectedTable.records)) {
    const fields = { tableSqlFields: [], tableNonSqlFields: [] };
    fields.tableSqlFields = selectedTable.records[0].fields
      .filter(field => !sqlHelp.includes(field.type.toLowerCase()))
      .map(field => ({
        label: field.name,
        code: field.fieldId
      }));
    fields.tableNonSqlFields = selectedTable.records[0].fields
      .filter(field => !nonSql.includes(field.type.toLowerCase()))
      .map(field => ({
        label: field.name,
        code: field.fieldId
      }));
    return fields;
  }
  return [];
};

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';

export const getDatasetSchemaTableFields = (candidateTable, tables) => {
  const [selectedTable] = tables.filter(table => table.tableSchemaId === candidateTable.code);

  if (!isNil(selectedTable) && !isEmpty(selectedTable) && !isNil(selectedTable.records)) {
    return selectedTable.records[0].fields
      .filter(field => !config.validations.bannedFields.includes(field.type.toLowerCase()))
      .map(field => ({
        label: field.name,
        code: field.fieldId
      }));
  }
  return [];
};

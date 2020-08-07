import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';

export const getDatasetSchemaTableFields = (candidateTable, tables) => {
  const [selectedTable] = tables.filter(table => table.tableSchemaId === candidateTable.code);
  const fields =
    !isNil(selectedTable) && !isEmpty(selectedTable) && !isNil(selectedTable.records)
      ? selectedTable.records[0].fields
          .filter(field => !config.validations.bannedFields.includes(field.name))
          .map(field => ({
            label: field.name,
            code: field.fieldId
          }))
      : [];
  console.log('fields', fields);
  return fields;
};

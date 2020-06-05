import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

export const getDatasetSchemaTableFields = (candidateTable, tables) => {
  const [selectedTable] = tables.filter(table => table.tableSchemaId === candidateTable.code);
  const fields =
    !isNil(selectedTable) && !isEmpty(selectedTable) && !isNil(selectedTable.records)
      ? selectedTable.records[0].fields.map(field => ({
          label: field.name,
          code: field.fieldId,
          type: field.type
        }))
      : [];
  return fields;
};

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

export const getSelectedTableByFieldId = (fieldId, tables) => {
  let selectedTable = {};
  tables.forEach(table => {
    if (!isNil(table.records)) {
      if (table.records[0].fields.filter(field => field.fieldId === fieldId)) {
        if (!isEmpty(table.records[0].fields.filter(field => field.fieldId === fieldId))) {
          selectedTable = { label: table.tableSchemaName, code: table.tableSchemaId };
        }
      }
    }
  });
  return selectedTable;
};

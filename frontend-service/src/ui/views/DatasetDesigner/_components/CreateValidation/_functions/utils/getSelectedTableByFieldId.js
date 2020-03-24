import isNil from 'lodash/isNil';
import isEmpty from 'lodash/isEmpty';

export const getSelectedTableByFieldId = (fieldId, tables) => {
  let selectedTable = {};
  tables.forEach(table => {
    if (!isNil(table.records)) {
      if (!isEmpty(table.records[0].fields.filter(field => field.fieldId == fieldId))) {
        selectedTable = { label: table.tableSchemaName, code: table.recordSchemaId };
      }
    }
  });
  return selectedTable;
};

export const getSelectedTableByRecordId = (recordSchemaId, tables) => {
  let selectedTable = null;
  const [selectedTableFromRecord] = tables.filter(table => table.recordSchemaId === recordSchemaId);

  const [selectedTableFromTableSchema] = tables.filter(table => table.tableSchemaId === recordSchemaId);

  selectedTable = selectedTableFromRecord || selectedTableFromTableSchema;
  return { label: selectedTable.header, code: selectedTable.tableSchemaId };
};

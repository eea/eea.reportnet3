export const getSelectedTableByRecordId = (recordSchemaId, tables) => {
  const [selectedTable] = tables.filter(table => table.recordSchemaId === recordSchemaId);
  return { label: selectedTable.header, code: selectedTable.tableSchemaId };
};

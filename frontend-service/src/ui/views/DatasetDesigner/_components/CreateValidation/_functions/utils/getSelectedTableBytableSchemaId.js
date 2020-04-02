export const getSelectedTableBytableSchemaId = (tableSchemaId, tables) => {
  const [selectedTable] = tables.filter(table => table.tableSchemaId == tableSchemaId);
  return { label: selectedTable.header, code: tableSchemaId };
};

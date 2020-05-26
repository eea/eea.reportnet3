export const getFieldType = (table, field, tables) => {
  const [selectedTable] = tables.filter(fTable => fTable.tableSchemaId === table.code);
  const [selectedField] = selectedTable.records[0].fields.filter(fField => fField.fieldId === field.code);
  return selectedField.type;
};

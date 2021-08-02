import { parseFields } from './parseFields';

export const parseTables = rawTables => {
  const tablesOptions = [];
  const tables = [];
  for (const table of rawTables) {
    const { tableSchemaName, tableSchemaId } = table;
    tablesOptions.push({
      label: table.tableSchemaName,
      value: table.tableSchemaId
    });
    const [record] = table.records;
    const fields = parseFields(record.fields);
    tables.push({
      tableSchemaName,
      tableSchemaId,
      ...fields
    });
  }
  return { tables, tablesOptions };
};

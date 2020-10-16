import { parseTables } from './parseTables.js';

export const parseDatasetSchema = rawDatasetSchema => {
  const { datasetSchemaId, datasetSchemaName, tables: rawTables } = rawDatasetSchema;
  const tables = parseTables(rawTables);
  return { datasetSchemaId, datasetSchemaName, ...tables };
};

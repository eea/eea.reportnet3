import { parseTables } from './parseTables.js';

export const parseDatasetSchema = rawDatasetSchema => {
  const { datasetId, datasetSchemaName, tables: rawTables } = rawDatasetSchema;
  const tables = parseTables(rawTables);
  return { datasetId, datasetSchemaName, ...tables };
};

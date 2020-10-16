import { parseDatasetSchema } from './parseDatasetSchema';

export const parseDatasetSchemas = rowDatasetSchemas => {
  const datasetSchemas = {
    datasetSchemaOptions: [],
    datasetSchemas: []
  };
  for (const rowDatasetSchema of rowDatasetSchemas) {
    const option = { label: rowDatasetSchema.datasetSchemaName, value: rowDatasetSchema.datasetSchemaId };
    const datasetSchema = parseDatasetSchema(rowDatasetSchema);
    datasetSchemas.datasetSchemaOptions.push(option);
    datasetSchemas.datasetSchemas.push(datasetSchema);
  }
  return datasetSchemas;
};

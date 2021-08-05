import { parseDatasetSchema } from './parseDatasetSchema';

export const parseDatasetSchemas = rowDatasetSchemas => {
  const datasetSchemas = {
    datasetSchemaOptions: [],
    datasetSchemas: []
  };
  const { dataflowDetails, designDatasets } = rowDatasetSchemas;
  for (const rowDatasetSchema of dataflowDetails) {
    const dataset = designDatasets.find(
      designDataset => designDataset.datasetSchemaId === rowDatasetSchema.datasetSchemaId
    );
    const option = { label: rowDatasetSchema.datasetSchemaName, value: dataset.datasetId };
    const datasetSchema = parseDatasetSchema({ ...rowDatasetSchema, datasetId: dataset.datasetId });
    datasetSchemas.datasetSchemaOptions.push(option);
    datasetSchemas.datasetSchemas.push(datasetSchema);
  }
  return datasetSchemas;
};

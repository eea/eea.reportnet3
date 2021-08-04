export const CreateDatasetSchema = ({ dataflowRepository }) => async (dataflowId, datasetSchemaName) =>
  dataflowRepository.newEmptyDatasetSchema(dataflowId, datasetSchemaName);

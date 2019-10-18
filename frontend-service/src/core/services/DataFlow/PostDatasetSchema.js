export const PostDatasetSchema = ({ dataflowRepository }) => async (dataflowId, datasetSchemaName) =>
  dataflowRepository.newEmptyDatasetSchema(dataflowId, datasetSchemaName);

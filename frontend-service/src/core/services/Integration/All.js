export const All = ({ integrationRepository }) => async (dataflowId, datasetSchemaId) =>
  integrationRepository.all(dataflowId, datasetSchemaId);

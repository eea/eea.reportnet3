export const AllExtensionsOperations = ({ integrationRepository }) => async (dataflowId, datasetSchemaId) =>
  integrationRepository.allExtensionsOperations(dataflowId, datasetSchemaId);

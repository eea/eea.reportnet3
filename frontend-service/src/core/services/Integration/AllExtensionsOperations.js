export const AllExtensionsOperations = ({ integrationRepository }) => async datasetSchemaId =>
  integrationRepository.allExtensionsOperations(datasetSchemaId);

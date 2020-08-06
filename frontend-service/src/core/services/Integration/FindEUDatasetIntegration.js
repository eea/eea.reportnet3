export const FindEUDatasetIntegration = ({ integrationRepository }) => async datasetSchemaId =>
  integrationRepository.findEUDatasetIntegration(datasetSchemaId);

export const FindEUDatasetIntegration = ({ integrationRepository }) => async datasetId =>
  integrationRepository.findEUDatasetIntegration(datasetId);

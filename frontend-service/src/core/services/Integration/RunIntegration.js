export const RunIntegration = ({ integrationRepository }) => async (integrationId, datasetId) =>
  integrationRepository.runIntegration(integrationId, datasetId);

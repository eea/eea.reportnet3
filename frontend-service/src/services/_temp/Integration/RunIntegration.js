export const RunIntegration = ({ integrationRepository }) => async (integrationId, datasetId, replaceData) =>
  integrationRepository.runIntegration(integrationId, datasetId, replaceData);

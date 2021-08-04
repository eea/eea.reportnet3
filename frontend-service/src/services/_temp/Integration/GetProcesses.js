export const GetProcesses = ({ integrationRepository }) => async (repository, datasetId) =>
  integrationRepository.getProcesses(repository, datasetId);

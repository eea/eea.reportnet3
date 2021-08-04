export const GetRepositories = ({ integrationRepository }) => async datasetId =>
  integrationRepository.getRepositories(datasetId);
